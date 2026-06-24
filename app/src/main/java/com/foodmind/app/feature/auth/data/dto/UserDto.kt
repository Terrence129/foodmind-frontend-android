package com.foodmind.app.feature.auth.data.dto

data class UserDto(
    val id: Long,
    val email: String,
    val username: String,
    val avatarUrl: String?,
    val profileCompleted: Boolean
)
