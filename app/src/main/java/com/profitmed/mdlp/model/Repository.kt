package com.profitmed.mdlp.model

import android.util.Log
import com.profitmed.mdlp.ui.MainActivity
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
        Log.d("XXX", "Отправляем данные")
        /*RestApi.api.importkiz(
            MainActivity.DEF_DID,
            kiz,
            MainActivity.DEF_LID800,
            MainActivity.DEF_EID,
            MainActivity.DEF_LID4000,
            MainActivity.DEF_VAR1,
            MainActivity.DEF_VAR2
        ).enqueue(callback)*/

        var body = RequestImportKiz(
            MainActivity.DEF_DID,
            kiz,
            MainActivity.DEF_LID800,
            MainActivity.DEF_EID,
            MainActivity.DEF_LID4000,
            MainActivity.DEF_VAR1,
            MainActivity.DEF_VAR2)

        Log.d("XXX", body.convertToJson())
        RestApi.api.importkizPOST(body)
        Log.d("XXX", "Отправлено")
    }
}