package com.profitmed.mdlp.model

interface IResMsg {
    val RES: Int
    val MSG: String

    fun toResponseResMsg(): ResponseResMsg
}