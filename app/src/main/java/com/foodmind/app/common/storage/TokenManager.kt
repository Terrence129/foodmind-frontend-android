package com.foodmind.app.common.storage

import android.content.Context

class TokenManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "foodmind_session",
        Context.MODE_PRIVATE
    )

    fun saveToken(token: String) {
        preferences.edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun getToken(): String? =
        preferences.getString(KEY_TOKEN, null)

    fun clearToken() {
        preferences.edit().remove(KEY_TOKEN).apply()
    }

    private companion object {
        const val KEY_TOKEN = "jwt_token"
    }
}
