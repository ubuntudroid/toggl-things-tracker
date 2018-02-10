package io.github.ubuntudroid.toggltracker.network

import io.github.ubuntudroid.toggltracker.network.model.Summary
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * See https://github.com/toggl/toggl_api_docs for a description of the endpoints.
 */
interface TogglReportService {
    /**
     * Requests the aggregated time entries data for the given timeframe from [since] to [until]
     * provided in UTC and with format YYYY-MM-DD.
     */
    @GET("summary")
    fun getSummary(
            @Query("since") since: String, @Query("until") until: String,
            @Query("user_ids") userIds: List<String>
    ): Deferred<Summary>
}