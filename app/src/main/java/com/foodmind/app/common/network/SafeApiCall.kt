package com.foodmind.app.common.network

import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException

suspend fun <T : Any> safeApiCall(
    call: suspend () -> Response<ApiResponse<T>>
): NetworkResult<T> {
    return try {
        val response = call()

        if (response.isSuccessful) {
            val body = response.body()
            val data = body?.data

            if (body?.success == true && data != null) {
                NetworkResult.Success(data, body.message)
            } else {
                NetworkResult.ServerError(
                    body?.message ?: "Invalid server response."
                )
            }
        } else {
            val error = runCatching {
                Gson().fromJson(
                    response.errorBody()?.string(),
                    ApiErrorResponse::class.java
                )
            }.getOrNull()

            val message = error?.message ?: "Request failed."

            when (response.code()) {
                400 -> NetworkResult.ValidationError(
                    message = message,
                    fields = error?.details
                        ?.associate { it.field to it.message }
                        .orEmpty()
                )

                401 -> NetworkResult.Unauthorized
                409 -> NetworkResult.Conflict(message)
                else -> NetworkResult.ServerError(message)
            }
        }
    } catch (_: IOException) {
        NetworkResult.NetworkError(
            "No internet connection. Check your network and try again."
        )
    } catch (_: Exception) {
        NetworkResult.ServerError(
            "Something went wrong. Please try again."
        )
    }
}