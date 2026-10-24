package com.example.taskboard.data

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    /** "10.0.2.2:8000" -> "http://10.0.2.2:8000/" (Retrofit needs a scheme and a trailing slash). */
    fun normalizeBaseUrl(raw: String): String {
        val trimmed = raw.trim()
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "http://$trimmed"
        return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
    }

    fun create(baseUrl: String, tokenProvider: () -> String?, onUnauthorized: () -> Unit): ApiService {
        val authInterceptor = Interceptor { chain ->
            val builder = chain.request().newBuilder()
            tokenProvider()?.let { builder.header("Authorization", "Bearer $it") }
            val response = chain.proceed(builder.build())
            val path = chain.request().url.encodedPath
            // an expired token on a protected endpoint means: back to the login screen
            if (response.code == 401 && !path.endsWith("/auth/login") && !path.endsWith("/auth/register")) {
                onUnauthorized()
            }
            response
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        // serializeNulls: PUT must be able to send "due_date": null / "assignee_id": null
        val gson = GsonBuilder().serializeNulls().create()
        return Retrofit.Builder()
            .baseUrl(normalizeBaseUrl(baseUrl))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
