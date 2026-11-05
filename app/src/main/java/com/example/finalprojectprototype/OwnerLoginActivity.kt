package com.example.finalprojectprototype

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalprojectprototype.data.DatabaseHelper

class OwnerLoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etLoginUsername: EditText
    private lateinit var etLoginPassword: EditText
    private lateinit var btnQuickFillLogin: Button
    private lateinit var btnSubmitLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_login)

        dbHelper = DatabaseHelper(this)

        etLoginUsername = findViewById(R.id.etLoginUsername)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        btnQuickFillLogin = findViewById(R.id.btnQuickFillLogin)
        btnSubmitLogin = findViewById(R.id.btnSubmitLogin)

        btnQuickFillLogin.setOnClickListener {
            etLoginUsername.setText("markjaysontala")
            etLoginPassword.setText("legacy")
        }

        btnSubmitLogin.setOnClickListener {
            val username = etLoginUsername.text.toString().trim()
            val password = etLoginPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isValid = dbHelper.loginOwner(username, password)
            if (isValid) {
                Toast.makeText(this, "Login successful! Welcome $username", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, OwnerDashboardActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials. Please check username or password.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
