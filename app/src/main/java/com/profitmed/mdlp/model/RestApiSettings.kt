package com.profitmed.mdlp.model

import com.profitmed.mdlp.BuildConfig
import java.util.*

object RestApiSettings {
    object PmSettings {
        private const val ADDRESS_BASE_API = "http://${BuildConfig.REST_API_HOST}/api/"
        private const val ENDPOINT = "v1"
        const val ADDRESS = "$ADDRESS_BASE_API$ENDPOINT/"
    }
}