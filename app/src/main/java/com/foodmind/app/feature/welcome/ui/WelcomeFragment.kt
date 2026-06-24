package com.foodmind.app.feature.welcome.ui

import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import com.foodmind.app.R
import com.foodmind.app.databinding.FragmentWelcomeBinding
import com.google.android.material.snackbar.Snackbar

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWelcomeBinding.bind(view)

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

        listOf(
            binding.welcomeMessageText
        ).forEachIndexed { index, animatedView ->
            animatedView.alpha = 0f
            animatedView.translationY = 24f
            animatedView.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(120L + index * 80L)
                .setDuration(420L)
                .start()
        }
    }

    private fun bindIconClick() {
        binding.appIconContainer.setOnClickListener {
            Snackbar.make(binding.root, R.string.welcome_continue_pending, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
