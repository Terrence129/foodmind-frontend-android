package com.foodmind.app.feature.home.ui

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.foodmind.app.FoodMindApplication
import com.foodmind.app.R
import com.foodmind.app.databinding.ActivityHomeBinding
import com.foodmind.app.feature.addfood.ui.AddFoodActivity
import com.foodmind.app.feature.favorites.ui.FavoritesActivity
import com.foodmind.app.feature.userprofile.ui.UserProfileActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var currentRecommendationIndex = 0
    private var lastNavigationAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        renderGreeting()
        setupNavigation()
        startRecommendationCarousel()
    }

    private fun renderGreeting() {
        val username = (application as FoodMindApplication)
            .tokenManager
            .getUsername()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: getString(R.string.home_default_username)

        binding.textGreetingUsername.text = username
    }

    private fun setupNavigation() = with(binding) {
        buttonFavorites.setOnClickListener {
            openOnce(FavoritesActivity::class.java)
        }

        buttonAddFood.setOnClickListener {
            openOnce(AddFoodActivity::class.java)
        }

        buttonUserProfile.setOnClickListener {
            openOnce(UserProfileActivity::class.java)
        }
    }

    private fun startRecommendationCarousel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    delay(CAROUSEL_INTERVAL_MS)
                    scrollToNextRecommendation()
                }
            }
        }
    }

    private fun scrollToNextRecommendation() {
        val cards = binding.recommendationCards
        val cardCount = cards.childCount
        if (cardCount == 0) return

        currentRecommendationIndex =
            (currentRecommendationIndex + 1) % cardCount

        val target = cards.getChildAt(currentRecommendationIndex)
        val centeredLeft = target.left -
                (binding.recommendationScroll.width - target.width) / 2
        binding.recommendationScroll.smoothScrollTo(
            centeredLeft.coerceAtLeast(0),
            0
        )
    }

    private fun openOnce(destination: Class<out AppCompatActivity>) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastNavigationAt < NAVIGATION_DEBOUNCE_MS) return

        lastNavigationAt = now
        startActivity(Intent(this, destination))
    }

    private companion object {
        const val CAROUSEL_INTERVAL_MS = 2200L
        const val NAVIGATION_DEBOUNCE_MS = 600L
    }
}
