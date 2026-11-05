package com.example.finalprojectprototype

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.finalprojectprototype.data.CartManager
import com.example.finalprojectprototype.data.DatabaseHelper
import java.util.Locale

class CartCheckoutActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvOrderTypeHeader: TextView
    private lateinit var btnClearCart: Button
    private lateinit var containerCartItems: LinearLayout
    private lateinit var rgPaymentMethod: RadioGroup
    private lateinit var tvCheckoutGrandTotal: TextView
    private lateinit var btnConfirmOrder: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_checkout)

        dbHelper = DatabaseHelper(this)

        tvOrderTypeHeader = findViewById(R.id.tvOrderTypeHeader)
        btnClearCart = findViewById(R.id.btnClearCart)
        containerCartItems = findViewById(R.id.containerCartItems)
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod)
        tvCheckoutGrandTotal = findViewById(R.id.tvCheckoutGrandTotal)
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder)

        tvOrderTypeHeader.text = "Order Type: ${CartManager.orderType}"

        btnClearCart.setOnClickListener {
            CartManager.clearCart()
            Toast.makeText(this, "Cart cleared", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnConfirmOrder.setOnClickListener {
            processCheckout()
        }

        renderCartItems()
    }

    private fun renderCartItems() {
        val cartItems = CartManager.getItems()
        containerCartItems.removeAllViews()

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val inflater = LayoutInflater.from(this)
        for (cartItem in cartItems) {
            val itemView = inflater.inflate(R.layout.item_cart_checkout, containerCartItems, false)

            val tvCartItemName: TextView = itemView.findViewById(R.id.tvCartItemName)
            val tvCartUnitPrice: TextView = itemView.findViewById(R.id.tvCartUnitPrice)
            val tvCartItemTotal: TextView = itemView.findViewById(R.id.tvCartItemTotal)
            val tvCartAddOns: TextView = itemView.findViewById(R.id.tvCartAddOns)
            val btnCartMinus: Button = itemView.findViewById(R.id.btnCartMinus)
            val tvCartQty: TextView = itemView.findViewById(R.id.tvCartQty)
            val btnCartPlus: Button = itemView.findViewById(R.id.btnCartPlus)
            val btnCartRemove: Button = itemView.findViewById(R.id.btnCartRemove)

            tvCartItemName.text = cartItem.foodItem.name
            tvCartUnitPrice.text = String.format(Locale.US, "Unit Price: ₱%.2f", cartItem.unitPriceWithAddOns)
            tvCartItemTotal.text = String.format(Locale.US, "₱%.2f", cartItem.totalPrice)
            tvCartQty.text = "Qty: ${cartItem.quantity}"

            if (cartItem.selectedAddOns.isNotEmpty()) {
                tvCartAddOns.visibility = View.VISIBLE
                tvCartAddOns.text = "Add-ons: ${cartItem.selectedAddOns.joinToString(", ")}"
            } else {
                tvCartAddOns.visibility = View.GONE
            }

            btnCartMinus.setOnClickListener {
                CartManager.updateQuantity(cartItem, cartItem.quantity - 1)
                renderCartItems()
            }

            btnCartPlus.setOnClickListener {
                if (cartItem.quantity < cartItem.foodItem.quantity) {
                    CartManager.updateQuantity(cartItem, cartItem.quantity + 1)
                    renderCartItems()
                } else {
                    Toast.makeText(this, "Stock limit reached (${cartItem.foodItem.quantity})", Toast.LENGTH_SHORT).show()
                }
            }

            btnCartRemove.setOnClickListener {
                CartManager.removeItem(cartItem)
                renderCartItems()
            }

            containerCartItems.addView(itemView)
        }

        val grandTotal = CartManager.getTotalAmount()
        tvCheckoutGrandTotal.text = String.format(Locale.US, "₱%.2f", grandTotal)
    }

    private fun processCheckout() {
        val selectedRadioId = rgPaymentMethod.checkedRadioButtonId
        if (selectedRadioId == -1) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadioButton = findViewById<RadioButton>(selectedRadioId)
        val paymentMethod = selectedRadioButton.text.toString()

        val cartItems = CartManager.getItems()
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val (success, message) = dbHelper.placeOrder(
            orderType = CartManager.orderType,
            paymentMethod = paymentMethod,
            cartItems = cartItems
        )

        if (success) {
            val receiptBuilder = StringBuilder()
            receiptBuilder.append("Order Status: SUCCESS\n")
            receiptBuilder.append("Order Type: ${CartManager.orderType}\n")
            receiptBuilder.append("Payment Method: $paymentMethod\n\n")
            receiptBuilder.append("Items Purchased:\n")

            for (item in cartItems) {
                receiptBuilder.append("• ${item.foodItem.name} x${item.quantity} - ₱${String.format(Locale.US, "%.2f", item.totalPrice)}\n")
                if (item.selectedAddOns.isNotEmpty()) {
                    receiptBuilder.append("   Extra: ${item.selectedAddOns.joinToString(", ")}\n")
                }
            }

            receiptBuilder.append("\nTotal Paid: ₱${String.format(Locale.US, "%.2f", CartManager.getTotalAmount())}\n")
            receiptBuilder.append("\nThank you for ordering at QuickBite!")

            AlertDialog.Builder(this)
                .setTitle("Order Placed Successfully!")
                .setMessage(receiptBuilder.toString())
                .setPositiveButton("Done / Back to Main") { _, _ ->
                    CartManager.clearCart()
                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                .setCancelable(false)
                .show()

        } else {
            AlertDialog.Builder(this)
                .setTitle("Order Failed")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
