package com.foodmind.app.feature.auth.data

import com.foodmind.app.feature.auth.data.dto.AuthResponse
import com.foodmind.app.feature.auth.data.dto.LoginRequest
import com.foodmind.app.feature.auth.data.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>
}
