package io.github.ubuntudroid.toggltracker.di

import dagger.Module
import dagger.Provides
import io.github.ubuntudroid.toggltracker.di.qualifiers.Jira
import io.github.ubuntudroid.toggltracker.network.jira.JiraService
import retrofit2.Retrofit

@Module
class JiraModule {

    @Provides
    fun provideJiraService(@Jira retrofit: Retrofit): JiraService =
            retrofit.create(JiraService::class.java)
}