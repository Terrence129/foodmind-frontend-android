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

    fun saveSession(token: String, username: String) {
        preferences.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun getToken(): String? =
        preferences.getString(KEY_TOKEN, null)

    fun getUsername(): String? =
        preferences.getString(KEY_USERNAME, null)

    fun clearToken() {
        preferences.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USERNAME)
            .apply()
    }

    private companion object {
        const val KEY_TOKEN = "jwt_token"
        const val KEY_USERNAME = "username"
    }
}
