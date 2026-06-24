package com.foodmind.app

import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.foodmind.app.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        playEntryAnimation()
        bindIconClick()
    }

    private fun playEntryAnimation() {
        binding.appIconContainer.apply {
            alpha = 0f
            scaleX = 0.62f
            scaleY = 0.62f
            translationY = 28f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(560L)
                .setInterpolator(OvershootInterpolator(1.4f))
                .start()
        }

        binding.welcomeMessageText.apply {
            alpha = 0f
            translationY = 24f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(120L)
                .setDuration(420L)
                .start()
        }
    }

    private fun bindIconClick() {
        binding.appIconContainer.setOnClickListener {
            Snackbar.make(binding.root, R.string.welcome_continue_pending, Snackbar.LENGTH_SHORT).show()
        }
    }
}
