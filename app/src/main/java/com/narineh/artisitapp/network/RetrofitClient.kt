package com.narineh.artisitapp.network

import android.content.Context
import retrofit2.Retrofit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitClient {
//    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val BASE_URL = "https://artist-vault.uw.r.appspot.com/"
    private lateinit var cookieJar: ClearableCookieJar

    fun initCookieJar(context: Context) {
        cookieJar = PersistentCookieJar(
            SetCookieCache(),
            SharedPrefsCookiePersistor(context))
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val favoriteService: FavoriteService by lazy {
        retrofit.create(FavoriteService::class.java)
    }

    val artistService: ArtistService by lazy {
        retrofit.create(ArtistService::class.java)
    }
    // Clear cookies
    fun clearJar() {
        cookieJar.clear()
    }
}