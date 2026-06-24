package com.foodmind.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.foodmind.app.databinding.ActivityMainBinding
import com.foodmind.app.feature.auth.ui.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        playEntryAnimation()
        scheduleLoginNavigation()
    }

    private fun playEntryAnimation() {
        binding.welcomeImage.apply {
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(420L)
                .start()
        }
    }

    private fun scheduleLoginNavigation() {
        lifecycleScope.launch {
            delay(LOGIN_NAVIGATION_DELAY_MS)
            openLogin()
        }
    }

    private fun openLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    private companion object {
        const val LOGIN_NAVIGATION_DELAY_MS = 900L
    }
}
