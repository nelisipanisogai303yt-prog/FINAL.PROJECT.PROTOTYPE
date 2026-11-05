package com.example.finalprojectprototype

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalprojectprototype.data.DatabaseHelper

class WelcomeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvOwnerStatus: TextView
    private lateinit var btnOrderAsGuest: Button
    private lateinit var btnOwnerLogin: Button
    private lateinit var btnOwnerRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        dbHelper = DatabaseHelper(this)

        tvOwnerStatus = findViewById(R.id.tvOwnerStatus)
        btnOrderAsGuest = findViewById(R.id.btnOrderAsGuest)
        btnOwnerLogin = findViewById(R.id.btnOwnerLogin)
        btnOwnerRegister = findViewById(R.id.btnOwnerRegister)

        btnOrderAsGuest.setOnClickListener {
            val intent = Intent(this, CustomerMenuActivity::class.java)
            startActivity(intent)
        }

        btnOwnerLogin.setOnClickListener {
            val intent = Intent(this, OwnerLoginActivity::class.java)
            startActivity(intent)
        }

        btnOwnerRegister.setOnClickListener {
            if (dbHelper.hasOwnerAccount()) {
                Toast.makeText(
                    this,
                    "Owner account already exists! Registration is locked.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val intent = Intent(this, OwnerRegisterActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateOwnerStatus()
    }

    private fun updateOwnerStatus() {
        val hasOwner = dbHelper.hasOwnerAccount()
        if (hasOwner) {
            tvOwnerStatus.text = "Owner Account: Registered & Configured\n(Registration Disabled)"
            tvOwnerStatus.setTextColor(getColor(R.color.accent_green))
            btnOwnerRegister.alpha = 0.5f
        } else {
            tvOwnerStatus.text = "Owner Account: Setup Required\n(Initial Owner Account Pending)"
            tvOwnerStatus.setTextColor(getColor(R.color.primary_red))
            btnOwnerRegister.alpha = 1.0f
        }
    }
}
