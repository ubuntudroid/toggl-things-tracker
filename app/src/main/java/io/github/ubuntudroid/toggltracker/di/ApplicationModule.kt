package io.github.ubuntudroid.toggltracker.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.github.ubuntudroid.toggltracker.main.MainActivity

@Module
abstract class ApplicationModule {

    @ContributesAndroidInjector(modules = [TogglModule::class, JiraModule::class, NetworkModule::class])
    abstract fun contributeActivityInjector(): MainActivity
}