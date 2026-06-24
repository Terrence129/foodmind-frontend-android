package com.foodmind.app.feature.auth.data.dto

data class AuthResponse(
    val token: String,
    val user: UserDto
)
