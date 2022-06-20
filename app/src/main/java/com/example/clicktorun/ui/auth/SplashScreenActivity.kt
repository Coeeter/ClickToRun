package com.example.clicktorun.ui.auth

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Pair
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.clicktorun.R
import com.example.clicktorun.databinding.ActivitySplashScreenBinding
import com.example.clicktorun.ui.MainActivity
import com.example.clicktorun.utils.ACTION_ANIMATE_LOGIN_PAGE
import com.example.clicktorun.utils.isNightModeEnabled
import com.example.clicktorun.utils.startActivityWithAnimation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(mainLooper).postDelayed({
            CoroutineScope(Dispatchers.Main).launch {
                val state = authViewModel.getCurrentUserState()
                Log.d("poly", state.toString())
                if (state["firebaseUser"] != null && state["firestoreUser"] == null)
                    return@launch Intent(
                        this@SplashScreenActivity,
                        UserDetailsActivity::class.java
                    ).run {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivityWithAnimation(this)
                        finish()
                    }
                if (state["firebaseUser"] != null)
                    return@launch Intent(this@SplashScreenActivity, MainActivity::class.java).run {
                        startActivity(this)
                        finish()
                        overridePendingTransition(0, 0)
                    }
                with(binding) {
                    val isDarkMode = this@SplashScreenActivity.isNightModeEnabled()
                    background.setBackgroundColor(
                        if (isDarkMode)
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
                            it.action = ACTION_ANIMATE_LOGIN_PAGE
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
            }
        }, 1500)
    }
}