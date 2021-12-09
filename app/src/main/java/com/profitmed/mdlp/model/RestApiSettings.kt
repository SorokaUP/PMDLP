package com.profitmed.mdlp.model

import com.profitmed.mdlp.BuildConfig
import java.util.*

object RestApiSettings {
    object PmSettings {
        const val ADDRESS_BASE_API = "http://${BuildConfig.REST_API_HOST}/api/"
    }
}