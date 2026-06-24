package com.foodmind.app.common.network

data class ApiErrorResponse(
    val success: Boolean = false,
    val message: String = "Request failed",
    val errorCode: String? = null,
    val details: List<FieldError>? = null,
    val errors: Map<String, String>? = null
)
