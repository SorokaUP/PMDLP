package com.profitmed.mdlp.model

import retrofit2.Call
import retrofit2.http.*

interface IRestApiRetrofit {
    @Headers("Content-Type: application/json")
    @POST("importkiz")
    fun importkiz(
        @Query("did") did: String,
        @Query("kiz") kiz: String,
        @Query("lid800") lid800: String,
        @Query("eid") eid: String,
        @Query("lid4000") lid4000: String,
        @Query("var1") var1: String,
        @Query("var2") var2: String,
    ): Call<ResponseIdResMsg>

    @Headers("Content-Type: application/json")
    @POST("importkiz")
    fun importkizPOST(@Body body: RequestImportKiz): Call<ResponseIdResMsg>
}