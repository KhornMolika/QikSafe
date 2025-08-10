package com.example.testfeatureofqiksafe.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.testfeatureofqiksafe.R
import com.example.testfeatureofqiksafe.ui.viewmodel.AuthViewModel
import com.example.testfeatureofqiksafe.util.SharedPrefHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var logoImage: ImageView
    private lateinit var appNameText: TextView
    private val viewModel = AuthViewModel()

    // Splash delay in milliseconds
    private val splashDelay = 2500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize views
        logoImage = findViewById(R.id.logoImage)
        appNameText = findViewById(R.id.appNameText)

        // Apply animation
        val fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_up)
        logoImage.startAnimation(fadeInAnim)
        appNameText.startAnimation(fadeInAnim)

        // Start splash coroutine
        lifecycleScope.launch {
            delay(splashDelay)
            navigateBasedOnAuth()
        }
    }

    private fun navigateBasedOnAuth() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val savedUserId = SharedPrefHelper.getUserId(this)
        val rememberMe = SharedPrefHelper.isRememberMeEnabled(this)

        if (firebaseUser != null && firebaseUser.isEmailVerified && savedUserId != null && rememberMe) {
            // ✅ User is logged in and verified
            navigateToMain()
        } else {
            // User not logged in or email not verified
            viewModel.logout(this)
            navigateToLogin()
        }

        Log.d("SplashActivity", "Firebase user: $firebaseUser, UID in SharedPrefs: $savedUserId")
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
