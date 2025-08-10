package com.example.testfeatureofqiksafe.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.testfeatureofqiksafe.R
import com.example.testfeatureofqiksafe.ui.viewmodel.AuthViewModel
import com.example.testfeatureofqiksafe.util.SharedPrefHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        val rememberMeCheckBox = findViewById<CheckBox>(R.id.cbRememberMe)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmailLogin).text.toString()
            val password = findViewById<EditText>(R.id.etPasswordLogin).text.toString()
            val rememberMe = rememberMeCheckBox.isChecked

            viewModel.loginWithEmail(this, email, password) { success, error ->
                if (success) {
                    // ✅ Save Remember Me flag
                    SharedPrefHelper.setRememberMe(this, rememberMe)

                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    if (error?.contains("verify your email", ignoreCase = true) == true) {
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, EmailVerificationActivity::class.java))
                    } else {
                        Toast.makeText(this, error ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
        findViewById<Button>(R.id.btnToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
