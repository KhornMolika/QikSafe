package com.example.testfeatureofqiksafe.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.testfeatureofqiksafe.R
import com.example.testfeatureofqiksafe.ui.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val name = findViewById<EditText>(R.id.etNameRegister)
        val phone = findViewById<EditText>(R.id.etPhoneRegister)
        val email = findViewById<EditText>(R.id.etEmailRegister)
        val password = findViewById<EditText>(R.id.etPasswordRegister)

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val nameText = name.text.toString().trim()
            val phoneText = phone.text.toString().trim()
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()

            // Validation
            if (!Patterns.PHONE.matcher(phoneText).matches() || phoneText.length < 9) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (nameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (passwordText.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.registerUser(nameText, phoneText, emailText, passwordText)
        }

        viewModel.registerResult.observe(this) { (success, error) ->
            if (success) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, EmailVerificationActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, error ?: "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnToLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

}

