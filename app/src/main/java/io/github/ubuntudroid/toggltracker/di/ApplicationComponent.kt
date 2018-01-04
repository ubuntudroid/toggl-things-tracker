package io.github.ubuntudroid.toggltracker.di

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import io.github.ubuntudroid.toggltracker.TogglTrackerApplication

@Component(modules = [AndroidInjectionModule::class, ApplicationModule::class])
interface ApplicationComponent: AndroidInjector<TogglTrackerApplication>