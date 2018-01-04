package io.github.ubuntudroid.toggltracker.main

import android.app.Activity

import android.os.Bundle
import android.util.Log
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.pwmspeaker.Speaker
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
            Speaker(BoardDefaults.speakerPwmPin).apply { stop() }
        } catch (e: IOException) {
            Log.e(TAG, "Error initialising speaker", e)
            Log.d(TAG, "Disabling speaker")
            null
        }

        mainPresenter.start(display, speaker)
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
    }
}
