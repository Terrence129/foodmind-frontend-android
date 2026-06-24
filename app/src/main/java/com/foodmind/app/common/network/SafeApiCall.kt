package com.foodmind.app.common.network

import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException

suspend fun <T : Any> safeApiCall(
    call: suspend () -> Response<ApiResponse<T>>
): NetworkResult<T> {
    return executeApiCall(call) { response ->
        val body = response.body()
        val data = body?.data

        if (body?.success == true && data != null) {
            NetworkResult.Success(data, body.message)
        } else {
            NetworkResult.ServerError(
                body?.message ?: "Invalid server response."
            )
        }
    }
}

suspend fun <T : Any> safeDirectApiCall(
    call: suspend () -> Response<T>
): NetworkResult<T> {
    return executeApiCall(call) { response ->
        val body = response.body()

        if (body != null) {
            NetworkResult.Success(body, "OK")
        } else {
            NetworkResult.ServerError("Invalid server response.")
        }
    }
}

private suspend fun <R : Any, T : Any> executeApiCall(
    call: suspend () -> Response<R>,
    handleSuccess: (Response<R>) -> NetworkResult<T>
): NetworkResult<T> {
    return try {
        val response = call()

        if (response.isSuccessful) {
            handleSuccess(response)
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
                    fields = error.fieldErrors()
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

private fun ApiErrorResponse?.fieldErrors(): Map<String, String> {
    val detailErrors = this?.details
        ?.associate { it.field to it.message }
        .orEmpty()

    return if (detailErrors.isNotEmpty()) {
        detailErrors
    } else {
        this?.errors.orEmpty()
    }
}
