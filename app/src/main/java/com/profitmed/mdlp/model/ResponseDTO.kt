package com.profitmed.mdlp.model

import com.google.gson.annotations.SerializedName

data class ResponseResMsg(
    @SerializedName("res")
    val RES: Int,
    @SerializedName("msg")
    val MSG: String
)

data class ResponseIdResMsg(
    @SerializedName("id")
    val ID: Int,
    @SerializedName("res")
    override val RES: Int,
    @SerializedName("msg")
    override val MSG: String
): IResMsg {
    override fun toResponseResMsg(): ResponseResMsg {
        return ResponseResMsg(RES, MSG)
    }
}

data class ResponseCheckDid(
    @SerializedName("res")
    override val RES: Int,
    @SerializedName("msg")
    override val MSG: String,
    @SerializedName("did")
    val DID: Int,
    @SerializedName("concept")
    val CONCEPT: Int
): IResMsg {
    override fun toResponseResMsg(): ResponseResMsg {
        return ResponseResMsg(RES, MSG)
    }
}