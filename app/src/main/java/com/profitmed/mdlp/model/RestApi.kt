package com.profitmed.mdlp.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.GsonBuilder
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.profitmed.mdlp.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.FormBody

import okhttp3.RequestBody

import okhttp3.OkHttpClient




object RestApi {
    val api: IRestApiRetrofit = Retrofit.Builder()
        .baseUrl(RestApiSettings.ADDRESS)
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().setLenient().create()
            )
        )
        .client(okHttpClient())
        .build().create(IRestApiRetrofit::class.java)

    private fun okHttpClient() : OkHttpClient {
        Log.d("XXX", "okHttpClient")
        Log.d("XXX", RestApiSettings.ADDRESS)
/*
        val client = OkHttpClient()

        val formBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("did", "did")
            .addFormDataPart("kiz", "kiz")
            .addFormDataPart("lid800", "lid800")
            .addFormDataPart("eid", "eid")
            .addFormDataPart("lid4000", "lid4000")
            .addFormDataPart("var1", "var1")
            .addFormDataPart("var2", "var2")
            .build()
        val request: Request = Request.Builder()
            .url("http://192.168.127.78:8085/api/v1/importkiz?api_key=877b3c35d95795cdf2010ceb8390573e")
            .post(formBody)
            .build()

        val response = client.newCall(request).execute()
        return client
*/

        /*val res = okhttp3.OkHttpClient.Builder()
        res.connectTimeout(10, TimeUnit.SECONDS)
        res.readTimeout(10, TimeUnit.SECONDS)
        res.writeTimeout(10, TimeUnit.SECONDS)
        res.addInterceptor { chain ->
            Log.d("XXX", "1")
            val original: Request = chain.request()
            Log.d("XXX", "2")
            val requestBuilder: Request.Builder = original.newBuilder()
                .url(
                    original.url().newBuilder()
                        .addQueryParameter("api_key", BuildConfig.REST_API_KEY)
                        .build())
                .method(original.method(), original.body())
            Log.d("XXX", "3")
            val request: Request = requestBuilder.build()
            Log.d("XXX", request.url().toString())
            chain.proceed(request)
        }
        return res.build()*/

        return OkHttpClient.Builder().let {
            it.connectTimeout(10, TimeUnit.SECONDS)
            it.readTimeout(10, TimeUnit.SECONDS)
            it.writeTimeout(10, TimeUnit.SECONDS)
            it.addInterceptor { chain ->
                Log.d("XXX", "1")
                val original: Request = chain.request()
                Log.d("XXX", "2")
                val requestBuilder: Request.Builder = original.newBuilder()
                    .url(
                        original.url().newBuilder()
                            .addQueryParameter("api_key", BuildConfig.REST_API_KEY)
                            .build())
                    .method(original.method(), original.body())
                Log.d("XXX", "3")
                val request: Request = requestBuilder.build()
                Log.d("XXX", request.url().toString())
                chain.proceed(request)
            }
        }.build()
    }
}