package com.profitmed.mdlp.presenter

import com.profitmed.mdlp.model.ResponseIdResMsg

sealed class AppState {
    data class Success(val res: ResponseIdResMsg) : AppState()
    data class Error(val error: Throwable) : AppState()
    object Loading : AppState()
}