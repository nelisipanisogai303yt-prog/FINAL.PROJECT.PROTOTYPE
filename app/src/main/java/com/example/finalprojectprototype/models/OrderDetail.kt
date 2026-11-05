package com.example.finalprojectprototype.models

data class OrderDetail(
    val id: Long = 0,
    val orderId: Long,
    val foodId: Long,
    val foodName: String,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double,
    val customizations: String = ""
)
