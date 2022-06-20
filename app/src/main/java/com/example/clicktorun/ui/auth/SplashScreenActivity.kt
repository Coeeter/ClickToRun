package com.example.clicktorun.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.clicktorun.databinding.ActivitySplashScreenBinding
import com.example.clicktorun.ui.MainActivity
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
            binding.brand.animate().translationX(2500f).setDuration(500L).start()
            Handler(mainLooper).postDelayed({
                binding.appName.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.Main).launch {
                    val state = authViewModel.getCurrentUserState()
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
                        return@launch Intent(
                            this@SplashScreenActivity,
                            MainActivity::class.java
                        ).run {
                            startActivity(this)
                            finish()
                            overridePendingTransition(0, 0)
                        }
                    Intent(this@SplashScreenActivity, LoginActivity::class.java).run {
                        startActivity(this)
                        finish()
                        overridePendingTransition(0, 0)
                    }
                }
            }, 100)
        }, 2000)
    }
}