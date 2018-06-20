package io.github.ubuntudroid.toggltracker.main

import android.app.Activity
import android.databinding.DataBindingUtil
import android.graphics.Color

import android.os.Bundle
import android.util.Log
import com.google.android.things.contrib.driver.apa102.Apa102
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.pwmspeaker.Speaker
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.android.things.pio.Gpio
import dagger.android.AndroidInjection
import io.github.ubuntudroid.toggltracker.R
import io.github.ubuntudroid.toggltracker.databinding.ActivityMainBinding
import java.io.IOException
import javax.inject.Inject

private const val TAG = "MainActivity"

class MainActivity : Activity() {

    @Inject
    lateinit var mainViewModel: MainViewModel

    private var display: AlphanumericDisplay? = null
    private var speaker: Speaker? = null
    private var led: Gpio? = null
    private var ledStrip: Apa102? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.vm = mainViewModel

        display = try {
            RainbowHat.openDisplay().apply {
                setEnabled(true)
                clear()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error initialising display", e)
            Log.d(TAG, "Disabling display")
            null
        }

        speaker = try {
            RainbowHat.openPiezo()
                    .apply { stop() }
        } catch (e: IOException) {
            Log.e(TAG, "Error initialising speaker", e)
            Log.d(TAG, "Disabling speaker")
            null
        }

        led = try {
            RainbowHat.openLedRed()
                    .apply { setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW) }
        } catch (e: IOException) {
            Log.e(TAG, "Error initialising LED", e)
            Log.d(TAG, "Disabling LED")
            null
        }

        ledStrip = try {
            RainbowHat.openLedStrip()
                    .apply {
                        brightness = 0
                        val colors = IntArray(7)
                        colors.fill(Color.TRANSPARENT) // color doesn't matter here
                        write(colors)
                    }
        } catch (e: IOException) {
            Log.e(TAG, "Error initialising LED strip", e)
            Log.d(TAG, "Disabling LED strip")
            null
        }

        mainViewModel.start(display, speaker, led, ledStrip)
    }

    override fun onDestroy() {
        super.onDestroy()

        mainViewModel.stop()

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
