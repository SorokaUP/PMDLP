package com.profitmed.mdlp.model

import retrofit2.Call
import retrofit2.http.*

interface IRestApiRetrofit {
    @POST("importkiz")
    fun importkiz(@Body body: RequestImportKiz): Call<ResponseIdResMsg>
}