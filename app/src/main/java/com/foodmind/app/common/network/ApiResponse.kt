package com.foodmind.app.common.network

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
    val errorCode: String? = null,
    val details: List<FieldError>? = null
)