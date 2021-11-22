package com.profitmed.mdlp.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class ResponseIdResMsg(
    val id: Int,
    val res: Int,
    val msg: String
)

data class RequestImportKiz(
    @SerializedName("did")
    val did: String,
    @SerializedName("kiz")
    val kiz: String,
    @SerializedName("lid800")
    val lid800: String,
    @SerializedName("eid")
    val eid: String,
    @SerializedName("lid4000")
    val lid4000: String,
    @SerializedName("var1")
    val var1: String,
    @SerializedName("var2")
    val var2: String
) {
    override fun toString(): String {
        return "did:$did;kiz:$kiz;lid800:$lid800;eid:$eid;lid4000:$lid4000;var1:$var1;var2:$var2"
    }

    fun convertToJson(): String {
        return "{\"did\":\"$did\", \"kiz\":\"kiz\", \"lid800\":\"$lid800\", \"eid\":\"$eid\", \"lid4000\":\"$lid4000\", \"var1\":\"$var1\", \"var2\":\"$var2\"}"
    }
}