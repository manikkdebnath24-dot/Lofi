package com.lauda.lofi.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {
    private val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build()

    fun create(baseUrl: String): SaavnApi =
        Retrofit.Builder()
            .baseUrl(baseUrl.trimEnd('/') + "/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(SaavnApi::class.java)
}
