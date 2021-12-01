package com.profitmed.mdlp.viewmodel

import com.profitmed.mdlp.model.ResponseIdResMsg

sealed class AppState {
    data class Success(val res: ResponseIdResMsg) : AppState()
    data class Error(val error: Throwable) : AppState()
    object Loading : AppState()
    object Next : AppState()
    object Idle : AppState()
}