package com.example.clicktorun.ui.auth

import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Pair
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.clicktorun.R
import com.example.clicktorun.databinding.ActivitySplashScreenBinding
import com.example.clicktorun.ui.MainActivity
import com.example.clicktorun.utils.ACTION_ANIMATE_LOGIN_PAGE
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(mainLooper).postDelayed({
            authViewModel.getCurrentUser()?.let {
                return@postDelayed Intent(this, MainActivity::class.java).run {
                    action = ACTION_ANIMATE_LOGIN_PAGE
                    startActivity(this)
                    finish()
                    overridePendingTransition(0, 0)
                }
            }
            with(binding) {
                val isDarkMode =
                    when (this@SplashScreenActivity.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                        Configuration.UI_MODE_NIGHT_YES -> true
                        Configuration.UI_MODE_NIGHT_NO -> false
                        else -> null
                    }
                background.setBackgroundColor(
                    if (isDarkMode == true)
                        Color.BLACK
                    else
                        Color.WHITE
                )
                brand.apply {
                    setImageResource(R.mipmap.ic_launcher_round_foreground)
                    animate().translationY(100F).setDuration(500).start()
                }
                Handler(mainLooper).postDelayed({
                    Intent(this@SplashScreenActivity, LoginActivity::class.java).also {
                        startActivity(
                            it,
                            ActivityOptions.makeSceneTransitionAnimation(
                                this@SplashScreenActivity,
                                Pair.create(brand, "brand")
                            ).toBundle()
                        )
                        finish()
                    }
                }, 500)
            }
        }, 1500)
    }
}