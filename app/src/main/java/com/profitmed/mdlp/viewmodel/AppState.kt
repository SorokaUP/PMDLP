package com.profitmed.mdlp.viewmodel

import com.profitmed.mdlp.model.*

sealed class AppState {
    data class Success(val res: ResponseResMsg) : AppState()
    data class Error(val error: Throwable) : AppState()
    object Loading : AppState()
    object Idle : AppState()
}