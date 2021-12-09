package com.profitmed.mdlp.model

import android.support.v4.media.session.MediaSessionCompat
import com.google.gson.annotations.SerializedName

data class ResponseResMsg(
    @SerializedName("RES")
    val RES: Int,
    @SerializedName("MSG")
    val MSG: String
)

data class ResponseIdResMsg(
    @SerializedName("ID")
    val ID: Int,
    @SerializedName("RES")
    override val RES: Int,
    @SerializedName("MSG")
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