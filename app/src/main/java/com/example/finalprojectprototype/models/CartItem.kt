package com.example.finalprojectprototype.models

data class CartItem(
    val foodItem: FoodItem,
    var quantity: Int,
    val selectedAddOns: List<String> = emptyList(),
    val addOnExtraCost: Double = 0.0
) {
    val unitPriceWithAddOns: Double
        get() = foodItem.price + addOnExtraCost

    val totalPrice: Double
        get() = unitPriceWithAddOns * quantity
}
