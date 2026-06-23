package com.foodmind.app.feature.auth.ui

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.foodmind.app.FoodMindApplication
import com.foodmind.app.R
import com.foodmind.app.databinding.ActivityRegisterBinding
import com.foodmind.app.feature.profile.ui.ProfileSetupActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createViewModel()
        setupInputs()
        setupActions()
        observeViewModel()
    }

    private fun createViewModel() {
        val application = application as FoodMindApplication

        val factory = AuthViewModelFactory(
            application.authRepository
        )

        viewModel = ViewModelProvider(this, factory)
            .get(AuthViewModel::class.java)
    }

    private fun setupInputs() = with(binding) {
        editTextUsername.doAfterTextChanged {
            viewModel.updateUsername(it?.toString().orEmpty())
        }

        editTextEmail.doAfterTextChanged {
            viewModel.updateEmail(it?.toString().orEmpty())
        }

        editTextPassword.doAfterTextChanged {
            viewModel.updatePassword(it?.toString().orEmpty())
        }

        editTextConfirmPassword.doAfterTextChanged {
            viewModel.updateConfirmPassword(it?.toString().orEmpty())
        }

        editTextConfirmPassword.setOnEditorActionListener {
                _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.register()
                true
            } else {
                false
            }
        }
    }

    private fun setupActions() = with(binding) {
        buttonRegister.setOnClickListener {
            viewModel.register()
        }

        buttonBackToLogin.setOnClickListener {
            if (!viewModel.uiState.value.isSubmitting) {
                finish()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect(::render)
                }

                launch {
                    viewModel.events.collect(::handleEvent)
                }
            }
        }
    }

    private fun render(state: AuthUiState) = with(binding) {
        inputLayoutUsername.error = state.usernameError
        inputLayoutEmail.error = state.emailError
        inputLayoutPassword.error = state.passwordError
        inputLayoutConfirmPassword.error =
            state.confirmPasswordError

        progressRegister.isVisible = state.isSubmitting

        buttonRegister.isEnabled = !state.isSubmitting
        buttonRegister.text = if (state.isSubmitting) {
            ""
        } else {
            getString(R.string.register)
        }

        buttonBackToLogin.isEnabled = !state.isSubmitting
        editTextUsername.isEnabled = !state.isSubmitting
        editTextEmail.isEnabled = !state.isSubmitting
        editTextPassword.isEnabled = !state.isSubmitting
        editTextConfirmPassword.isEnabled =
            !state.isSubmitting
    }

    private fun handleEvent(event: AuthUiEvent) {
        when (event) {
            AuthUiEvent.RegisterSuccess -> {
                openProfileSetup()
            }

            is AuthUiEvent.ShowMessage -> {
                Snackbar.make(
                    binding.root,
                    event.message,
                    Snackbar.LENGTH_LONG
                ).show()
            }

            is AuthUiEvent.LoginSuccess -> Unit
        }
    }

    private fun openProfileSetup() {
        val intent = Intent(
            this,
            ProfileSetupActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
    }
}