package com.profitmed.mdlp.model

import retrofit2.Callback

class RepositoryProfitMed: IRepositoryProfitMed {
    override fun inputKiz(
        callback: Callback<ResponseIdResMsg>,
        did: String,
        kiz: String,
        lid800: String,
        eid: String,
        lid4000: String,
        var1: String,
        var2: String
    ) {
        val body = RequestImportKiz(did, kiz, lid800, eid, lid4000, var1, var2)
        RestApi.profitMed.importkiz(body)
            .enqueue(callback)
    }

    override fun checkDid(
        callback: Callback<ResponseCheckDid>,
        did: String
    ) {
        RestApi.profitMed.checkDid(did)
            .enqueue(callback)
    }
}