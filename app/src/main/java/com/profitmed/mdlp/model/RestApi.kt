package com.profitmed.mdlp.model

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.profitmed.mdlp.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

object RestApi {
    private const val LOG_TAG = "RestApi"

    val pm: IRestApiRetrofit = Retrofit.Builder()
        .baseUrl(RestApiSettings.PmSettings.ADDRESS)
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().create()
            )
        )
        .client(okHttpClient())
        .build().create(IRestApiRetrofit::class.java)

    private fun okHttpClient() : OkHttpClient {
        val res = OkHttpClient.Builder()
        res.connectTimeout(10, TimeUnit.SECONDS)
        res.readTimeout(10, TimeUnit.SECONDS)
        res.writeTimeout(10, TimeUnit.SECONDS)

        res.addInterceptor{ chain ->
            val original: Request = chain.request()
            val requestBuilder: Request.Builder = original.newBuilder()
                .url(
                    original.url().newBuilder()
                        .addQueryParameter("api_key", BuildConfig.REST_API_KEY)
                        .build())
                .method(original.method(), original.body())
            val request: Request = requestBuilder.build()
            Log.d(LOG_TAG, request.url().toString())
            chain.proceed(request)
        }
        return res.build()
    }
}