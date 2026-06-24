package com.foodmind.app.feature.auth.data

import com.foodmind.app.common.network.NetworkResult
import com.foodmind.app.common.network.safeDirectApiCall
import com.foodmind.app.common.storage.TokenManager
import com.foodmind.app.feature.auth.data.dto.AuthResponse
import com.foodmind.app.feature.auth.data.dto.LoginRequest
import com.foodmind.app.feature.auth.data.dto.RegisterRequest

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {

    suspend fun login(
        email: String,
        password: String
    ): NetworkResult<AuthResponse> {
        val result = safeDirectApiCall {
            authApi.login(
                LoginRequest(
                    email = email,
                    password = password
                )
            )
        }

        saveTokenOnSuccess(result)
        return result
    }

    suspend fun register(
        username: String,
        email: String,
        password: String
    ): NetworkResult<AuthResponse> {
        val result = safeDirectApiCall {
            authApi.register(
                RegisterRequest(
                    username = username,
                    email = email,
                    password = password
                )
            )
        }

        saveTokenOnSuccess(result)
        return result
    }

    private fun saveTokenOnSuccess(
        result: NetworkResult<AuthResponse>
    ) {
        if (result is NetworkResult.Success) {
            tokenManager.saveToken(result.data.token)
        }
    }
}
