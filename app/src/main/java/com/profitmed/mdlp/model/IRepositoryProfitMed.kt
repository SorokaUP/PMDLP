package com.profitmed.mdlp.model

import retrofit2.Callback

interface IRepositoryProfitMed {
    fun inputKiz(
        callback: Callback<ResponseIdResMsg>,
        did: String,
        kiz: String,
        lid800: String,
        eid: String,
        lid4000: String,
        var1: String,
        var2: String)

    fun checkDid(
        callback: Callback<ResponseCheckDid>,
        did: String
    )
}