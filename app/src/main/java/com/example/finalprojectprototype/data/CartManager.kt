package com.example.finalprojectprototype.data

import com.example.finalprojectprototype.models.CartItem
import com.example.finalprojectprototype.models.FoodItem

object CartManager {
    private val items = mutableListOf<CartItem>()
    var orderType: String = "Dine-in"

    fun getItems(): List<CartItem> = items.toList()

    fun addItem(foodItem: FoodItem, quantity: Int, addOns: List<String>, addOnExtraCost: Double) {
        val existing = items.find { 
            it.foodItem.id == foodItem.id && it.selectedAddOns.sorted() == addOns.sorted() 
        }
        if (existing != null) {
            existing.quantity += quantity
        } else {
            items.add(CartItem(foodItem, quantity, addOns, addOnExtraCost))
        }
    }

    fun removeItem(cartItem: CartItem) {
        items.remove(cartItem)
    }

    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            items.remove(cartItem)
        } else {
            cartItem.quantity = newQuantity
        }
    }

    fun clearCart() {
        items.clear()
        orderType = "Dine-in"
    }

    fun getTotalAmount(): Double {
        return items.sumOf { it.totalPrice }
    }

    fun getItemCount(): Int {
        return items.sumOf { it.quantity }
    }
}
