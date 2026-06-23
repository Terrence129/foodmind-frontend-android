package com.foodmind.app.feature.auth.ui

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodmind.app.common.network.NetworkResult
import com.foodmind.app.feature.auth.data.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val eventChannel = Channel<AuthUiEvent>()
    val events = eventChannel.receiveAsFlow()

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            emailError = null
        )
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            passwordError = null
        )
    }

    fun updateUsername(value: String) {
        _uiState.value = _uiState.value.copy(
            username = value,
            usernameError = null
        )
    }

    fun updateConfirmPassword(value: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = value,
            confirmPasswordError = null
        )
    }

    fun login() {
        val state = _uiState.value
        if (state.isSubmitting || !validateLogin(state)) return

        viewModelScope.launch {
            setSubmitting(true)

            when (
                val result = repository.login(
                    state.email.trim(),
                    state.password
                )
            ) {
                is NetworkResult.Success -> {
                    setSubmitting(false)
                    eventChannel.send(
                        AuthUiEvent.LoginSuccess(
                            result.data.user.profileCompleted
                        )
                    )
                }

                NetworkResult.Unauthorized -> {
                    setSubmitting(false)
                    eventChannel.send(
                        AuthUiEvent.ShowMessage(
                            "Email or password is incorrect."
                        )
                    )
                }

                is NetworkResult.NetworkError ->
                    showError(result.message)

                is NetworkResult.ServerError ->
                    showError(result.message)

                else -> showError("Login failed.")
            }
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.isSubmitting || !validateRegister(state)) return

        viewModelScope.launch {
            setSubmitting(true)

            when (
                val result = repository.register(
                    username = state.username.trim(),
                    email = state.email.trim(),
                    password = state.password
                )
            ) {
                is NetworkResult.Success -> {
                    setSubmitting(false)
                    eventChannel.send(AuthUiEvent.RegisterSuccess)
                }

                is NetworkResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        usernameError = result.fields["username"],
                        emailError = result.fields["email"],
                        passwordError = result.fields["password"]
                    )
                }

                is NetworkResult.Conflict -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        emailError = result.message
                    )
                }

                is NetworkResult.NetworkError ->
                    showError(result.message)

                is NetworkResult.ServerError ->
                    showError(result.message)

                else -> showError("Registration failed.")
            }
        }
    }

    private fun validateLogin(state: AuthUiState): Boolean {
        val emailError = when {
            state.email.isBlank() -> "Email is required."
            !Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches() ->
                "Enter a valid email address."
            else -> null
        }

        val passwordError =
            if (state.password.isBlank()) "Password is required." else null

        _uiState.value = state.copy(
            emailError = emailError,
            passwordError = passwordError
        )

        return emailError == null && passwordError == null
    }

    private fun validateRegister(state: AuthUiState): Boolean {
        val usernameError = when {
            state.username.isBlank() -> "Username is required."
            state.username.trim().length !in 2..80 ->
                "Username must contain 2 to 80 characters."
            else -> null
        }

        val emailError = when {
            state.email.isBlank() -> "Email is required."
            !Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches() ->
                "Enter a valid email address."
            else -> null
        }

        val passwordError = when {
            state.password.isBlank() -> "Password is required."
            state.password.length < 8 ->
                "Password must contain at least 8 characters."
            else -> null
        }

        val confirmError = when {
            state.confirmPassword.isBlank() ->
                "Confirm your password."
            state.confirmPassword != state.password ->
                "Passwords do not match."
            else -> null
        }

        _uiState.value = state.copy(
            usernameError = usernameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmError
        )

        return listOf(
            usernameError,
            emailError,
            passwordError,
            confirmError
        ).all { it == null }
    }

    private fun setSubmitting(value: Boolean) {
        _uiState.value =
            _uiState.value.copy(isSubmitting = value)
    }

    private suspend fun showError(message: String) {
        setSubmitting(false)
        eventChannel.send(AuthUiEvent.ShowMessage(message))
    }
}
