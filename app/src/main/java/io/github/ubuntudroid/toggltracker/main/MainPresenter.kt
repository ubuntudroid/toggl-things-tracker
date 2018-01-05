package io.github.ubuntudroid.toggltracker.main

import android.os.CountDownTimer
import android.util.Log
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.pwmspeaker.Speaker
import com.google.android.things.pio.Gpio
import io.github.ubuntudroid.toggltracker.TogglRepository
import kotlinx.coroutines.experimental.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val WORK_DAY_HOURS = 8
private const val REFRESH_RATE_MS = 60*1000L
private const val TAG = "MainPresenter"

class MainPresenter @Inject constructor(private val togglRepository: TogglRepository) {

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
                display?.display(update.message)
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
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting data from toggl", e)
        }
    }

    private suspend fun getUpdate(): Update {
        val summary = togglRepository.getSummaryForToday()
        var totalGrand = 0L

        val currentTimeEntry = togglRepository.getCurrentTimeEntry().await()
        currentTimeEntry.data?.let {
            if (it.stop == null) {
                totalGrand += System.currentTimeMillis() + (currentTimeEntry.data.duration) * 1000
            }
        }
        totalGrand += summary.await().totalGrand

        val totalGrandHours = TimeUnit.MILLISECONDS.toHours(totalGrand)
        val totalGrandMinutes = TimeUnit.MILLISECONDS.toMinutes(totalGrand) % 60

        return Update(String.format("%02d.%02d", totalGrandHours, totalGrandMinutes), totalGrandHours >= WORK_DAY_HOURS)
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
        val message: String,
        val overtime: Boolean
)