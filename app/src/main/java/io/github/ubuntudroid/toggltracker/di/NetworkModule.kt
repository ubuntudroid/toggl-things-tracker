package io.github.ubuntudroid.toggltracker.di

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import io.github.ubuntudroid.toggltracker.BuildConfig
import io.github.ubuntudroid.toggltracker.di.qualifiers.Jira
import io.github.ubuntudroid.toggltracker.di.qualifiers.Reports
import io.github.ubuntudroid.toggltracker.di.qualifiers.TimeEntries
import io.github.ubuntudroid.toggltracker.di.qualifiers.Toggl
import io.github.ubuntudroid.toggltracker.network.toggl.AuthenticationInterceptor
import io.github.ubuntudroid.toggltracker.network.toggl.TogglMandatoryQueryParameterInterceptor
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class NetworkModule {

    @Provides
    @Toggl
    fun provideTogglAuthenticationInterceptor(): AuthenticationInterceptor = AuthenticationInterceptor(
            Credentials.basic(BuildConfig.TOGGL_TOKEN, "api_token")
    )

    @Provides
    @Jira
    fun provideJiraAuthenticationInterceptor(): AuthenticationInterceptor = AuthenticationInterceptor(
            Credentials.basic(BuildConfig.JIRA_USER_ID, BuildConfig.JIRA_TOKEN)
    )

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    @Provides
    @Toggl
    fun provideTogglOkHttpClient(
            @Toggl authenticationInterceptor: AuthenticationInterceptor,
            loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(authenticationInterceptor)
            .addInterceptor(TogglMandatoryQueryParameterInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Jira
    fun provideJiraOkHttpClient(
            @Jira authenticationInterceptor: AuthenticationInterceptor,
            loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(authenticationInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Reports
    fun provideReportsRetrofit(
            @Toggl okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.TOGGL_REPORTS_API_BASE_URL)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()


    @Provides
    @TimeEntries
    fun provideTimeEntriesRetrofit(
            @Toggl okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.TOGGL_TIME_ENTRIES_API_BASE_URL)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @Provides
    @Jira
    fun provideJiraRetrofit(
            @Jira okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.JIRA_BASE_URL)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

}