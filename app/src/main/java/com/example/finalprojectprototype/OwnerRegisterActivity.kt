package com.example.finalprojectprototype

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalprojectprototype.data.DatabaseHelper

class OwnerRegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etRegUsername: EditText
    private lateinit var etRegPassword: EditText
    private lateinit var etRegConfirmPassword: EditText
    private lateinit var btnQuickFillOwner: Button
    private lateinit var btnSubmitRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_register)

        dbHelper = DatabaseHelper(this)

        if (dbHelper.hasOwnerAccount()) {
            Toast.makeText(this, "An owner account is already registered!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        etRegUsername = findViewById(R.id.etRegUsername)
        etRegPassword = findViewById(R.id.etRegPassword)
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword)
        btnQuickFillOwner = findViewById(R.id.btnQuickFillOwner)
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister)

        // Quick fill button for mandatory specified owner credentials
        btnQuickFillOwner.setOnClickListener {
            etRegUsername.setText("markjaysontala")
            etRegPassword.setText("legacy")
            etRegConfirmPassword.setText("legacy")
        }

        btnSubmitRegister.setOnClickListener {
            val username = etRegUsername.text.toString().trim()
            val password = etRegPassword.text.toString().trim()
            val confirm = etRegConfirmPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill all registration fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 4) {
                Toast.makeText(this, "Password must be at least 4 characters long", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val success = dbHelper.registerOwner(username, password)
            if (success) {
                Toast.makeText(this, "Owner account successfully created!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, OwnerDashboardActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to register owner (Registration locked or duplicate)", Toast.LENGTH_LONG).show()
            }
        }
    }
}
