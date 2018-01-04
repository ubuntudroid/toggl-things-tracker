package io.github.ubuntudroid.toggltracker.di

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import io.github.ubuntudroid.toggltracker.BuildConfig
import io.github.ubuntudroid.toggltracker.di.qualifiers.Reports
import io.github.ubuntudroid.toggltracker.di.qualifiers.TimeEntries
import io.github.ubuntudroid.toggltracker.network.AuthenticationInterceptor
import io.github.ubuntudroid.toggltracker.network.MandatoryQueryParameterInterceptor
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class NetworkModule {

    @Provides
    fun provideAuthenticationInterceptor(): AuthenticationInterceptor {
        return AuthenticationInterceptor(
                Credentials.basic(BuildConfig.TOGGL_TOKEN, "api_token")
        )
    }

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    @Provides
    fun provideOkHttpClient(
            authenticationInterceptor: AuthenticationInterceptor,
            loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(authenticationInterceptor)
                .addInterceptor(MandatoryQueryParameterInterceptor())
                .addInterceptor(loggingInterceptor)
                .build()
    }

    @Provides
    @Reports
    fun provideReportsRetrofit(
            okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.TOGGL_REPORTS_API_BASE_URL)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
    }


    @Provides
    @TimeEntries
    fun provideTimeEntriesRetrofit(
            okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.TOGGL_TIME_ENTRIES_API_BASE_URL)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
    }
}