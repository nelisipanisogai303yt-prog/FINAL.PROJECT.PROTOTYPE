package com.example.finalprojectprototype.models

data class Order(
    val id: Long = 0,
    val orderType: String,
    val paymentMethod: String,
    val totalAmount: Double,
    val orderDate: String,
    val status: String = "Completed"
)
