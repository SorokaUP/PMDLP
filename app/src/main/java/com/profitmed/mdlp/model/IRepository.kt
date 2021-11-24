package com.profitmed.mdlp.model

interface IRepository {
    fun putInputKiz(callback: retrofit2.Callback<ResponseIdResMsg>, did: String, kiz: String, lid800: String, eid: String, lid4000: String, var1: String, var2: String)
}