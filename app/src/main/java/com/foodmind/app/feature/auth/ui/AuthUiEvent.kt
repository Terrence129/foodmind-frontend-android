package com.foodmind.app.feature.auth.ui

sealed interface AuthUiEvent {

    data object LoginSuccess : AuthUiEvent

    data object RegisterSuccess : AuthUiEvent

    data class ShowMessage(
        val message: String
    ) : AuthUiEvent
}
