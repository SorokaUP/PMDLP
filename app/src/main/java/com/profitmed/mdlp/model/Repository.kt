package com.profitmed.mdlp.model

import retrofit2.Callback

class Repository: IRepository {
    override fun putInputKiz(
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
        RestApi.pm.importkiz(body)
            .enqueue(callback)
    }
}