package io.github.ubuntudroid.toggltracker.di

import dagger.Module
import dagger.Provides
import io.github.ubuntudroid.toggltracker.di.qualifiers.Reports
import io.github.ubuntudroid.toggltracker.di.qualifiers.TimeEntries
import io.github.ubuntudroid.toggltracker.network.TogglReportService
import io.github.ubuntudroid.toggltracker.network.TogglTimeEntriesService
import retrofit2.Retrofit

@Module
class TogglModule {

    @Provides
    fun provideTogglReportService(@Reports retrofit: Retrofit): TogglReportService =
        retrofit.create(TogglReportService::class.java)

    @Provides
    fun provideTogglTimeEntriesService(@TimeEntries retrofit: Retrofit): TogglTimeEntriesService =
        retrofit.create(TogglTimeEntriesService::class.java)
}