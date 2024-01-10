package com.example.weatherapp.data.network

import com.example.weatherapp.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.Locale

object ApiFactory {

    private const val BASE_URL = "https://api.weatherapi.com/v1/"
    private const val KEY = "key"
    private const val PARAM_LANG = "lang"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->

            val origRequest = chain.request()

            val newUrl = origRequest
                .url()
                .newBuilder()
                .addQueryParameter(KEY, BuildConfig.WEATHER_API_KEY)
                .addQueryParameter(PARAM_LANG, Locale.getDefault().language)
                .build()

            val newRequest = origRequest
                .newBuilder()
                .url(newUrl)
                .build()

            chain.proceed(newRequest)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    val apiService: ApiService = retrofit.create()
}