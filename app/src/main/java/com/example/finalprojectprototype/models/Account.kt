package com.example.finalprojectprototype.models

data class Account(
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: String = ""
)
