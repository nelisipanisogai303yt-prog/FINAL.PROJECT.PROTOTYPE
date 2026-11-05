package com.example.finalprojectprototype.models

data class FoodItem(
    val id: Long = 0,
    val name: String,
    val category: String,
    val price: Double,
    var quantity: Int,
    var isAvailable: Boolean = true,
    val imageResName: String = "ic_food_placeholder"
)
