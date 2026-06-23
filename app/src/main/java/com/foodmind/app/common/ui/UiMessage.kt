package com.foodmind.app.common.ui

sealed class UiMessage {
    data class Toast(val message: String) : UiMessage()
    data class SnackBar(val message: String, val action: String? = null) : UiMessage()
    data class Dialog(val title: String, val message: String) : UiMessage()
}
