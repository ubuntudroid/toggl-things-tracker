package io.github.ubuntudroid.toggltracker.network.toggl

import io.github.ubuntudroid.toggltracker.network.toggl.model.TimeEntry
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.GET

interface TogglTimeEntriesService {

    @GET("current")
    fun getCurrentTimeEntry(): Deferred<TimeEntry>
}