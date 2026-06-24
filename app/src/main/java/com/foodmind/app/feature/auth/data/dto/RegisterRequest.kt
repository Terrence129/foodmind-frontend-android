package com.foodmind.app.feature.auth.data.dto

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)
