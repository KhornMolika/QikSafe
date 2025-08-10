package com.example.testfeatureofqiksafe.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testfeatureofqiksafe.R
import com.google.firebase.auth.FirebaseAuth

class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var btnResendEmail: Button
    private lateinit var btnCheckVerification: Button
    private lateinit var txtInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        auth = FirebaseAuth.getInstance()

        btnResendEmail = findViewById(R.id.btnResendEmail)
        btnCheckVerification = findViewById(R.id.btnCheckVerification)
        txtInfo = findViewById(R.id.txtInfo)

        txtInfo.text = "A verification link has been sent to your email. \nPlease verify it to continue."

        btnResendEmail.setOnClickListener {
            val user = auth.currentUser
            user?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        btnCheckVerification.setOnClickListener {
            auth.currentUser?.reload()?.addOnCompleteListener {
                if (auth.currentUser?.isEmailVerified == true) {
                    Toast.makeText(this, "Email verified!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Email not yet verified.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
