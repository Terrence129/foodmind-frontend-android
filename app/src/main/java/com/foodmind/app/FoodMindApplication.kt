package com.foodmind.app

import android.app.Application
import com.foodmind.app.common.network.ApiClient
import com.foodmind.app.common.storage.TokenManager
import com.foodmind.app.feature.auth.data.AuthApi
import com.foodmind.app.feature.auth.data.AuthRepository

class FoodMindApplication : Application() {

    lateinit var tokenManager: TokenManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()

        tokenManager = TokenManager(this)

        val authApi = ApiClient.publicRetrofit
            .create(AuthApi::class.java)

        authRepository = AuthRepository(
            authApi = authApi,
            tokenManager = tokenManager
        )
    }
}
