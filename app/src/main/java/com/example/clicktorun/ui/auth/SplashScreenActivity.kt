package com.example.clicktorun.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.clicktorun.R
import com.example.clicktorun.databinding.ActivitySplashScreenBinding
import com.example.clicktorun.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity(), Animation.AnimationListener {
    private lateinit var binding: ActivitySplashScreenBinding

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000L)
            val animation = AnimationUtils.loadAnimation(
                this@SplashScreenActivity,
                R.anim.slide_out_right
            )
            animation.setAnimationListener(this@SplashScreenActivity)
            binding.brand.startAnimation(animation)
            delay(150L)
            binding.appName.visibility = View.VISIBLE
            val intent = handleUserState(authViewModel.getCurrentUserState())
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }
    }

    private fun handleUserState(state: Map<String, Any?>): Intent {
        if (state["firebaseUser"] == null)
            return Intent(this, LoginActivity::class.java)
        if (state["firestoreUser"] == null)
            return Intent(this, UserDetailsActivity::class.java)
        return Intent(this, MainActivity::class.java)
    }

    override fun onAnimationStart(animation: Animation?) {}
    override fun onAnimationRepeat(animation: Animation?) {}
    override fun onAnimationEnd(animation: Animation?) {
        binding.brand.visibility = View.GONE
    }

}