package com.profitmed.mdlp.model

import retrofit2.Call
import retrofit2.http.*

interface IRestApiRetrofitProfitMed {
    @POST("v1/importkiz")
    fun importkiz(@Body body: RequestImportKiz): Call<ResponseIdResMsg>
    @GET("v1/checkdoc")
    fun checkDid(@Query("did") did: String): Call<ResponseCheckDid>
}