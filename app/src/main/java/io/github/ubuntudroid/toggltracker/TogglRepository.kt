package io.github.ubuntudroid.toggltracker

import io.github.ubuntudroid.toggltracker.network.model.Summary
import io.github.ubuntudroid.toggltracker.network.TogglReportService
import io.github.ubuntudroid.toggltracker.network.TogglTimeEntriesService
import io.github.ubuntudroid.toggltracker.network.model.TimeEntry
import kotlinx.coroutines.experimental.Deferred
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class TogglRepository @Inject constructor(
        private val togglNetworkService: TogglReportService,
        private val togglTimeEntriesService: TogglTimeEntriesService
) {

    fun getSummaryForToday(): Deferred<Summary> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .format(Date())
        return togglNetworkService.getSummary(today, today, arrayListOf(BuildConfig.TOGGL_USER_ID))
    }

    fun getCurrentTimeEntry(): Deferred<TimeEntry> {
        return togglTimeEntriesService.getCurrentTimeEntry()
    }
}