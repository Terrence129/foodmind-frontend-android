package com.foodmind.app.feature.auth.ui

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.ViewModelProvider
import com.foodmind.app.FoodMindApplication
import com.foodmind.app.databinding.ActivityLoginBinding
import com.foodmind.app.feature.home.ui.HomeActivity
import com.foodmind.app.feature.profile.ui.ProfileSetupActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
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
        editTextEmail.doAfterTextChanged {
            viewModel.updateEmail(it?.toString().orEmpty())
        }

        editTextPassword.doAfterTextChanged {
            viewModel.updatePassword(it?.toString().orEmpty())
        }

        editTextPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.login()
                true
            } else {
                false
            }
        }
    }

    private fun setupActions() = with(binding) {
        buttonLogin.setOnClickListener {
            viewModel.login()
        }

        buttonCreateAccount.setOnClickListener {
            if (!viewModel.uiState.value.isSubmitting) {
                startActivity(
                    Intent(
                        this@LoginActivity,
                        RegisterActivity::class.java
                    )
                )
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
        inputLayoutEmail.error = state.emailError
        inputLayoutPassword.error = state.passwordError

        progressLogin.isVisible = state.isSubmitting

        buttonLogin.isEnabled = !state.isSubmitting
        buttonLogin.text = if (state.isSubmitting) {
            ""
        } else {
            getString(com.foodmind.app.R.string.login)
        }

        buttonCreateAccount.isEnabled = !state.isSubmitting
        editTextEmail.isEnabled = !state.isSubmitting
        editTextPassword.isEnabled = !state.isSubmitting
    }

    private fun handleEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.LoginSuccess -> {
                val destination = if (event.profileCompleted) {
                    HomeActivity::class.java
                } else {
                    ProfileSetupActivity::class.java
                }

                openAuthenticatedPage(destination)
            }

            is AuthUiEvent.ShowMessage -> {
                Snackbar.make(
                    binding.root,
                    event.message,
                    Snackbar.LENGTH_LONG
                ).show()
            }

            AuthUiEvent.RegisterSuccess -> Unit
        }
    }

    private fun openAuthenticatedPage(
        destination: Class<out AppCompatActivity>
    ) {
        val intent = Intent(this, destination).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
    }
}