package com.foodmind.app.common.network

sealed interface NetworkResult<out T> {

    data class Success<T>(
        val data: T,
        val message: String
    ) : NetworkResult<T>

    data class ValidationError(
        val message: String,
        val fields: Map<String, String>
    ) : NetworkResult<Nothing>

    data object Unauthorized : NetworkResult<Nothing>

    data class Conflict(
        val message: String
    ) : NetworkResult<Nothing>

    data class NetworkError(
        val message: String
    ) : NetworkResult<Nothing>

    data class ServerError(
        val message: String
    ) : NetworkResult<Nothing>
}