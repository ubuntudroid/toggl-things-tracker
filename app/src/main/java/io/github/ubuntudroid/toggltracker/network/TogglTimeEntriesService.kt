package io.github.ubuntudroid.toggltracker.network

import io.github.ubuntudroid.toggltracker.network.model.TimeEntry
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.GET

interface TogglTimeEntriesService {

    @GET("current")
    fun getCurrentTimeEntry(): Deferred<TimeEntry>
}