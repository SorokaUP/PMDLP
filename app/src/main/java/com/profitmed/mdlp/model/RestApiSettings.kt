package com.profitmed.mdlp.model

object RestApiSettings {
    object PmSettings {
        private const val ADDRESS_BASE_API = "http://192.168.127.78:8085/api/"
        private const val ENDPOINT = "v1"
        const val ADDRESS = "$ADDRESS_BASE_API$ENDPOINT/"
    }
}