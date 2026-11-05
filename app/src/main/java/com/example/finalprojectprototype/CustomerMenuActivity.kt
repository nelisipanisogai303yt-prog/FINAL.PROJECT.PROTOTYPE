package com.example.finalprojectprototype

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalprojectprototype.data.CartManager
import com.example.finalprojectprototype.data.DatabaseHelper
import java.util.Locale

class CustomerMenuActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvCartCount: TextView
    private lateinit var tvBottomCartTotal: TextView
    private lateinit var btnViewCart: FrameLayout
    private lateinit var btnProceedCheckout: Button
    private lateinit var containerMenuList: LinearLayout

    private lateinit var rgDiningOption: RadioGroup
    private lateinit var rbDineIn: RadioButton
    private lateinit var rbTakeout: RadioButton

    private lateinit var cbExtraCheese: CheckBox
    private lateinit var cbExtraSauce: CheckBox
    private lateinit var cbCrispyBacon: CheckBox
    private lateinit var cbSpicyPowder: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_menu)

        dbHelper = DatabaseHelper(this)

        tvCartCount = findViewById(R.id.tvCartCount)
        tvBottomCartTotal = findViewById(R.id.tvBottomCartTotal)
        btnViewCart = findViewById(R.id.btnViewCart)
        btnProceedCheckout = findViewById(R.id.btnProceedCheckout)
        containerMenuList = findViewById(R.id.containerMenuList)

        rgDiningOption = findViewById(R.id.rgDiningOption)
        rbDineIn = findViewById(R.id.rbDineIn)
        rbTakeout = findViewById(R.id.rbTakeout)

        cbExtraCheese = findViewById(R.id.cbExtraCheese)
        cbExtraSauce = findViewById(R.id.cbExtraSauce)
        cbCrispyBacon = findViewById(R.id.cbCrispyBacon)
        cbSpicyPowder = findViewById(R.id.cbSpicyPowder)

        rgDiningOption.setOnCheckedChangeListener { _, checkedId ->
            CartManager.orderType = if (checkedId == R.id.rbTakeout) "Takeout" else "Dine-in"
        }

        val openCartClickListener = View.OnClickListener {
            if (CartManager.getItems().isEmpty()) {
                Toast.makeText(this, "Your cart is currently empty. Add items first!", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, CartCheckoutActivity::class.java)
                startActivity(intent)
            }
        }

        btnViewCart.setOnClickListener(openCartClickListener)
        btnProceedCheckout.setOnClickListener(openCartClickListener)

        loadMenuData()
    }

    override fun onResume() {
        super.onResume()
        updateCartSummary()
        loadMenuData()
    }

    private fun updateCartSummary() {
        val itemCount = CartManager.getItemCount()
        val totalAmount = CartManager.getTotalAmount()

        tvCartCount.text = itemCount.toString()
        tvBottomCartTotal.text = String.format(Locale.US, "₱%.2f", totalAmount)
    }

    private fun loadMenuData() {
        val availableItems = dbHelper.getAvailableInventory()
        containerMenuList.removeAllViews()

        if (availableItems.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No food items currently available in inventory."
                textSize = 15f
                setTextColor(getColor(R.color.text_secondary))
                setPadding(16, 40, 16, 40)
            }
            containerMenuList.addView(emptyTv)
            return
        }

        val inflater = LayoutInflater.from(this)
        for (item in availableItems) {
            val itemView = inflater.inflate(R.layout.item_customer_food, containerMenuList, false)

            val tvCustItemName: TextView = itemView.findViewById(R.id.tvCustItemName)
            val tvCustItemCategory: TextView = itemView.findViewById(R.id.tvCustItemCategory)
            val tvCustItemStock: TextView = itemView.findViewById(R.id.tvCustItemStock)
            val tvCustItemPrice: TextView = itemView.findViewById(R.id.tvCustItemPrice)
            val btnCustMinus: Button = itemView.findViewById(R.id.btnCustMinus)
            val tvCustQty: TextView = itemView.findViewById(R.id.tvCustQty)
            val btnCustPlus: Button = itemView.findViewById(R.id.btnCustPlus)
            val btnCustAddToCart: Button = itemView.findViewById(R.id.btnCustAddToCart)

            tvCustItemName.text = item.name
            tvCustItemCategory.text = "Category: ${item.category}"
            tvCustItemStock.text = "Available Stock: ${item.quantity} units"
            tvCustItemPrice.text = String.format(Locale.US, "₱%.2f", item.price)

            var currentQty = 1
            tvCustQty.text = "1"

            btnCustMinus.setOnClickListener {
                if (currentQty > 1) {
                    currentQty--
                    tvCustQty.text = currentQty.toString()
                }
            }

            btnCustPlus.setOnClickListener {
                if (currentQty < item.quantity) {
                    currentQty++
                    tvCustQty.text = currentQty.toString()
                } else {
                    Toast.makeText(this, "Maximum stock reached (${item.quantity})", Toast.LENGTH_SHORT).show()
                }
            }

            btnCustAddToCart.setOnClickListener {
                val addOnsList = mutableListOf<String>()
                var extraCost = 0.0

                if (cbExtraCheese.isChecked) {
                    addOnsList.add("Extra Cheese (+₱20)")
                    extraCost += 20.0
                }
                if (cbExtraSauce.isChecked) {
                    addOnsList.add("Extra Sauce (+₱15)")
                    extraCost += 15.0
                }
                if (cbCrispyBacon.isChecked) {
                    addOnsList.add("Crispy Bacon (+₱35)")
                    extraCost += 35.0
                }
                if (cbSpicyPowder.isChecked) {
                    addOnsList.add("Spicy Flakes (+₱10)")
                    extraCost += 10.0
                }

                val existingInCart = CartManager.getItems()
                    .filter { it.foodItem.id == item.id }
                    .sumOf { it.quantity }

                if (existingInCart + currentQty > item.quantity) {
                    Toast.makeText(
                        this,
                        "Cannot add $currentQty more. Stock limit: ${item.quantity} (In cart: $existingInCart)",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                CartManager.addItem(item, currentQty, addOnsList, extraCost)
                Toast.makeText(this, "Added $currentQty x ${item.name} to cart!", Toast.LENGTH_SHORT).show()
                updateCartSummary()
            }

            containerMenuList.addView(itemView)
        }
    }
}
