package io.github.ubuntudroid.toggltracker.main

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.pwmspeaker.Speaker
import com.google.android.things.pio.Gpio
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

class MainViewModel @Inject constructor(private val togglRepository: TogglRepository) {

    var currentEntry: ObservableField<String> = ObservableField("No tracking at the moment...")
    var weekTotal: ObservableField<String> = ObservableField("Retrieving week total...")
    var eodGoal: ObservableField<String> = ObservableField("Calculating EOD goal...")

    private val refreshTimer = RefreshTimer()

    private var display: AlphanumericDisplay? = null
    private var speaker: Speaker? = null
    private var led: Gpio? = null

    private var playedAlarm = false

    fun start(display: AlphanumericDisplay?, speaker: Speaker?, led: Gpio?) {
        this.display = display
        this.speaker = speaker
        this.led = led
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

            if (!TextUtils.isEmpty(update.currentEntryDescription)) {
                currentEntry.set(update.currentEntryDescription)
            } else {
                currentEntry.set("No tracking at the moment...")
            }
            weekTotal.set(update.weekTime)
            eodGoal.set(update.eodGoal + "h")
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting data from toggl", e)
        }
    }

    private suspend fun getUpdate(): Update {
        val summaryToday = togglRepository.getSummaryForToday()
        val summaryWeek = togglRepository.getSummaryForThisWeek()
        var totalGrandToday = 0L
        var totalGrandWeek = 0L

        val currentTimeEntry = togglRepository.getCurrentTimeEntry().await()
        currentTimeEntry.data?.let {
            if (it.stop == null) {
                val currentEntry = System.currentTimeMillis() + (currentTimeEntry.data.duration) * 1000
                totalGrandToday += currentEntry
                totalGrandWeek += currentEntry
            }
        }
        totalGrandToday += summaryToday.await().totalGrand
        totalGrandWeek += summaryWeek.await().totalGrand

        val totalGrandTodayHours = TimeUnit.MILLISECONDS.toHours(totalGrandToday)
        val totalGrandTodayMinutes = TimeUnit.MILLISECONDS.toMinutes(totalGrandToday) % 60
        val totalGrandWeekHours = TimeUnit.MILLISECONDS.toHours(totalGrandWeek)
        val totalGrandWeekMinutes = TimeUnit.MILLISECONDS.toMinutes(totalGrandWeek) % 60

        return Update(
                String.format("%02d.%02d", totalGrandTodayHours, totalGrandTodayMinutes),
                String.format("%02d:%02d", totalGrandWeekHours, totalGrandWeekMinutes),
                totalGrandTodayHours >= WORK_DAY_HOURS,
                currentTimeEntry.data?.description,
                getEodGoal()
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
        val eodGoal: String
)