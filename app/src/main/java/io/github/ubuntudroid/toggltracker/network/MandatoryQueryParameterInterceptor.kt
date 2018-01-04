package io.github.ubuntudroid.toggltracker.network

import io.github.ubuntudroid.toggltracker.BuildConfig
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class MandatoryQueryParameterInterceptor: Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalHttpUrl = original.url()

        val url = originalHttpUrl.newBuilder()
                .addQueryParameter("user_agent", "sven.bendel@stanwood.de")
                .addQueryParameter("workspace_id", BuildConfig.TOGGL_WORKSPACE_ID)
                .build()

        // Request customization: add request headers
        val requestBuilder = original.newBuilder()
                .url(url)

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}