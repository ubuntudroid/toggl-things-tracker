package io.github.ubuntudroid.toggltracker.main

import android.app.Activity

import android.os.Bundle
import android.util.Log
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.pwmspeaker.Speaker
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import dagger.android.AndroidInjection
import io.github.ubuntudroid.toggltracker.R
import io.github.ubuntudroid.toggltracker.iot.BoardDefaults
import java.io.IOException
import javax.inject.Inject

private const val TAG = "MainActivity"

class MainActivity : Activity() {

    @Inject
    lateinit var mainPresenter: MainPresenter

    private var display: AlphanumericDisplay? = null
    private var speaker: Speaker? = null
    private var led: Gpio? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = try {
            AlphanumericDisplay(BoardDefaults.i2cBus).apply {
                setEnabled(true)
                clear()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error initialising display", e)
            Log.d(TAG, "Disabling display")
            null
        }

        speaker = try {
            Speaker(BoardDefaults.speakerPwmPin)
                    .apply { stop() }
        } catch (e: IOException) {
            Log.e(TAG, "Error initialising speaker", e)
            Log.d(TAG, "Disabling speaker")
            null
        }

        val pioService = PeripheralManagerService()
        led = try {
            pioService.openGpio(BoardDefaults.ledGpioPin)
                    .apply { setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW) }
        } catch (e: IOException) {
            Log.e(TAG, "Error initialising LED", e)
            Log.d(TAG, "Disabling LED")
            null
        }

        mainPresenter.start(display, speaker, led)
    }

    override fun onDestroy() {
        super.onDestroy()

        mainPresenter.stop()

        display?.apply {
            try {
                clear()
                setEnabled(false)
                close()
            } catch (e: IOException) {
                Log.e(TAG, "Error disabling display", e)
            } finally {
                display = null
            }
        }

        speaker?.apply {
            try {
                stop()
                close()
            } catch (e: IOException) {
                Log.e(TAG, "Error disabling speaker", e)
            } finally {
                speaker = null
            }
        }

        led?.apply {
            try {
                close()
            } catch (e: IOException) {
                Log.e(TAG, "Error disabling LED", e)
            } finally {
                led = null
            }
        }
    }
}
