package com.foodmind.app.feature.auth.ui

sealed interface AuthUiEvent {

    data class LoginSuccess(
        val profileCompleted: Boolean
    ) : AuthUiEvent

    data object RegisterSuccess : AuthUiEvent

    data class ShowMessage(
        val message: String
    ) : AuthUiEvent
}