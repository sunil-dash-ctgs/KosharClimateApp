package com.kosherclimate.userapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {

    companion object {

     //   private val BASE_URL: String = "https://kc.carbonintellix.com/api/"  // AWS (Live)
     //   private const val BASE_URL: String = "https://ks.cropintellix.com/api/"  // C Panel (Test)
     private const val BASE_URL: String = "https://kctest.carbonintellix.com/api/"  // C Panel (Test)


        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        val client: OkHttpClient = OkHttpClient.Builder().apply {
            this.addInterceptor(interceptor)
        }.connectTimeout(500, TimeUnit.SECONDS).build()

        fun getRetrofitInstance(): Retrofit {
            return Retrofit.Builder().baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}

