package io.github.ubuntudroid.toggltracker.network.model

import java.util.Date

data class TimeEntry(
        val data: Data?
)

data class Data(
        /**
         * time entry ID
         */
        val id: Long = 0,

        /**
         * workspace ID
         */
        val wid: Long = 0,

        /**
         * time entry start time
         */
        val start: Date?,

        /**
         * time entry stop time, can be not set
         */
        val stop: Date?,

        /**
         * time entry duration in seconds.
         *
         * If the time entry is currently running, the duration attribute contains a negative value,
         * denoting the start of the time entry in seconds since epoch (Jan 1 1970). The correct
         * duration can be calculated as current_time + duration, where current_time is the current
         * time in seconds since epoch.
         */
        val duration: Long = 0,

        /**
         * An optional task subscription
         */
        val description: String?
)