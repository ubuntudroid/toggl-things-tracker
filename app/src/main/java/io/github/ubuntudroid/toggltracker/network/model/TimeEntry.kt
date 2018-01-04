package io.github.ubuntudroid.toggltracker.network.model

import java.util.Date

data class TimeEntry(
        val data: Data?
)

data class Data(
        val id: Long = 0,
        val wid: Long = 0,
        val start: Date?,
        val stop: Date?,
        val duration: Long = 0,
        val description: String = ""
)