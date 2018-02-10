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
        val today = Date().toUTCString()
        return togglNetworkService.getSummary(today, today, arrayListOf(BuildConfig.TOGGL_USER_ID))
    }

    fun getSummaryForThisWeek(): Deferred<Summary> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)

        // get start of this week in milliseconds
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)

        return togglNetworkService.getSummary(cal.time.toUTCString(), Date().toUTCString(), arrayListOf(BuildConfig.TOGGL_USER_ID))
    }

    fun getCurrentTimeEntry(): Deferred<TimeEntry> {
        return togglTimeEntriesService.getCurrentTimeEntry()
    }

    private fun Date.toUTCString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .format(this)
    }
}