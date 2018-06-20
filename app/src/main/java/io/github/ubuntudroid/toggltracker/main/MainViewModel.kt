package io.github.ubuntudroid.toggltracker.main

import android.databinding.ObservableField
import android.graphics.Color
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import com.google.android.things.contrib.driver.apa102.Apa102
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.pwmspeaker.Speaker
import com.google.android.things.pio.Gpio
import io.github.ubuntudroid.toggltracker.JiraRepository
import io.github.ubuntudroid.toggltracker.TogglRepository
import kotlinx.coroutines.experimental.*
import java.io.IOException
import java.util.*
import java.util.Calendar.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val WORK_DAY_HOURS = 8
private const val REFRESH_RATE_MS = 60*1000L
private const val TAG = "MainViewModel"
private val issueIdRegex = """((?<!([A-Za-z]{1,10})-?)[A-Z]+-\d+)""".toRegex()
private const val LED_COUNT = 7

class MainViewModel @Inject constructor(
        private val togglRepository: TogglRepository,
        private val jiraRepository: JiraRepository
) {

    private val LIGHT_GREEN = Color.parseColor("lime")

    var currentEntry: ObservableField<String> = ObservableField("No tracking at the moment...")
    var weekTotal: ObservableField<String> = ObservableField("Retrieving week total...")
    var eodGoal: ObservableField<String> = ObservableField("Calculating EOD goal...")
    var currentSpent: ObservableField<String> = ObservableField("0:00")
    var currentEstimate: ObservableField<String> = ObservableField("0:00")

    private val refreshTimer = RefreshTimer()

    private var display: AlphanumericDisplay? = null
    private var speaker: Speaker? = null
    private var led: Gpio? = null
    private var ledStrip: Apa102? = null

    private var playedAlarm = false

    fun start(display: AlphanumericDisplay?, speaker: Speaker?, led: Gpio?, ledStrip: Apa102?) {
        this.display = display
        this.speaker = speaker
        this.led = led
        this.ledStrip = ledStrip
        refreshTimer.start()
    }

    fun stop() {
        this.display = null
        this.speaker = null
        this.led = null
        refreshTimer.cancel()
    }

    private suspend fun refresh() {
        try {
            val update = getUpdate()
            try {
                display?.display(update.todayTime)
            } catch (e: IOException) {
                Log.e(TAG, "Error displaying $update on display", e)
            }

            if (update.overtime) {
                led?.value = true

                if (!playedAlarm) {
                    playedAlarm = true
                    try {
                        playChime()
                    } catch (e: IOException) {
                        Log.e(TAG, "Error playing sound on speaker", e)
                    }
                }
            } else {
                led?.value = false
                playedAlarm = false
            }

            if (update.originalEstimate == "00:00") {
                ledStrip?.apply {
                    // TODO refactor into helper class and also use in MainActivity
                    brightness = 0
                    val colors = IntArray(LED_COUNT)
                    colors.fill(Color.TRANSPARENT) // color doesn't matter, we just need to write
                    write(colors)
                }
            } else {
                ledStrip?.apply {
                    // TODO refactor into helper class
                    brightness = 1
                    val colors = IntArray(LED_COUNT)
                    val progressLedIndex = Math.min(Math.round(LED_COUNT * update.currentCompletionRatio), LED_COUNT)
                    colors.fill(LIGHT_GREEN, 0, progressLedIndex)
                    if (progressLedIndex < LED_COUNT) {
                        colors.fill(Color.TRANSPARENT, progressLedIndex, LED_COUNT)
                    }
                    write(colors)
                }
            }

            if (!TextUtils.isEmpty(update.currentEntryDescription)) {
                currentEntry.set(update.currentEntryDescription)
            } else {
                currentEntry.set("No tracking at the moment...")
            }
            weekTotal.set(update.weekTime)
            eodGoal.set(update.eodGoal + "h")
            currentEstimate.set(update.originalEstimate)
            currentSpent.set(update.timeSpent)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting data from toggl", e)
        }
    }

    private suspend fun getUpdate(): Update {
        val summaryToday = togglRepository.getSummaryForToday()
        val summaryWeek = togglRepository.getSummaryForThisWeek()
        var totalGrandToday = 0L
        var totalGrandWeek = 0L
        var timeSpent = 0L

        val currentTimeEntry = togglRepository.getCurrentTimeEntry().await()
        currentTimeEntry.data?.let {
            if (it.stop == null) {
                val currentEntry = System.currentTimeMillis() + (currentTimeEntry.data.duration) * 1000
                totalGrandToday += currentEntry
                totalGrandWeek += currentEntry
                timeSpent += currentEntry / 1000
            }
        }
        totalGrandToday += summaryToday.await().totalGrand
        totalGrandWeek += summaryWeek.await().totalGrand

        val issueId = issueIdRegex.find(currentTimeEntry.data?.description.toString())

        val currentIssue = issueId?.let { jiraRepository.getIssue(it.value).await() }
        val aggregateTimeSpent = currentIssue?.fields?.aggregatetimespent?.toLong() ?: 0L
        val aggregateTimeOriginalEstimate = currentIssue?.fields?.aggregatetimeoriginalestimate?.toLong() ?: 0L

        timeSpent += aggregateTimeSpent

        val totalGrandTodayHours = TimeUnit.MILLISECONDS.toHours(totalGrandToday)
        val totalGrandTodayMinutes = TimeUnit.MILLISECONDS.toMinutes(totalGrandToday) % 60
        val totalGrandWeekHours = TimeUnit.MILLISECONDS.toHours(totalGrandWeek)
        val totalGrandWeekMinutes = TimeUnit.MILLISECONDS.toMinutes(totalGrandWeek) % 60
        val currentCompletionRatio = aggregateTimeSpent / aggregateTimeOriginalEstimate.toFloat()
        val currentIssueEstimatedHours = TimeUnit.SECONDS.toHours(aggregateTimeOriginalEstimate)
        val currentIssueEstimatedMinutes = TimeUnit.SECONDS.toMinutes(aggregateTimeOriginalEstimate) % 60

        val currentIssueSpentHours = TimeUnit.SECONDS.toHours(timeSpent)
        val currentIssueSpentMinutes = TimeUnit.SECONDS.toMinutes(timeSpent) % 60

        return Update(
                String.format("%02d.%02d", totalGrandTodayHours, totalGrandTodayMinutes),
                String.format("%02d:%02d", totalGrandWeekHours, totalGrandWeekMinutes),
                totalGrandTodayHours >= WORK_DAY_HOURS,
                currentTimeEntry.data?.description,
                getEodGoal(),
                String.format("%02d:%02d", currentIssueEstimatedHours, currentIssueEstimatedMinutes),
                String.format("%02d:%02d", currentIssueSpentHours, currentIssueSpentMinutes),
                currentCompletionRatio
        )
    }

    private fun getEodGoal(): String {
        Calendar.getInstance().apply {
            time = Date()
        }.let {
            val dayOfWeek = it.get(Calendar.DAY_OF_WEEK) // starts with SUNDAY = 1

            // calculate the daily goal by week days - e.g on Tuesday that would be 2 * WORK_DAY_HOURS
            val eodGoalHours = when (dayOfWeek) {
                SATURDAY -> WORK_DAY_HOURS * (FRIDAY - 1)
                MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> WORK_DAY_HOURS * (dayOfWeek - 1)
                SUNDAY -> if (it.firstDayOfWeek == SUNDAY) 0 else WORK_DAY_HOURS * (FRIDAY - 1)
                else -> -1
            }

            return "$eodGoalHours"
        }
    }

    private fun playChime() {
        speaker?.apply {
            launch {
                val chime = arrayOf(415.30, 329.63, 369.99, 246.94, 246.94, 369.99, 415.30, 329.63)
                chime.forEach {
                    play(it)
                    delay(400)
                }
                stop()
            }
        }
    }

    inner class RefreshTimer : CountDownTimer(Long.MAX_VALUE, REFRESH_RATE_MS) {
        private var async: Deferred<Unit>? = null

        override fun onFinish() {
            async?.cancel()
        }

        override fun onTick(p0: Long) {
            async = async {
                refresh()
            }
        }

    }

}

private data class Update(
        val todayTime: String,
        val weekTime: String,
        val overtime: Boolean,
        val currentEntryDescription: String?,
        val eodGoal: String,
        val originalEstimate: String,
        val timeSpent: String,
        val currentCompletionRatio: Float
)