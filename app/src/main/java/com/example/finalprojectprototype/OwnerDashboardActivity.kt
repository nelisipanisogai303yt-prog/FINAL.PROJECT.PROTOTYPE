package com.example.finalprojectprototype

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.finalprojectprototype.data.DatabaseHelper
import com.example.finalprojectprototype.models.FoodItem
import java.util.Locale

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvTotalItems: TextView
    private lateinit var tvInStockItems: TextView
    private lateinit var tvOutOfStockItems: TextView
    private lateinit var btnAddFoodItem: Button
    private lateinit var btnOwnerLogout: Button
    private lateinit var containerInventoryList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        dbHelper = DatabaseHelper(this)

        tvTotalItems = findViewById(R.id.tvTotalItems)
        tvInStockItems = findViewById(R.id.tvInStockItems)
        tvOutOfStockItems = findViewById(R.id.tvOutOfStockItems)
        btnAddFoodItem = findViewById(R.id.btnAddFoodItem)
        btnOwnerLogout = findViewById(R.id.btnOwnerLogout)
        containerInventoryList = findViewById(R.id.containerInventoryList)

        btnAddFoodItem.setOnClickListener {
            showAddFoodDialog()
        }

        btnOwnerLogout.setOnClickListener {
            Toast.makeText(this, "Logged out from Owner Dashboard", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        loadInventoryData()
    }

    override fun onResume() {
        super.onResume()
        loadInventoryData()
    }

    private fun loadInventoryData() {
        val inventory = dbHelper.getAllInventory()

        val total = inventory.size
        val inStock = inventory.count { it.isAvailable && it.quantity > 0 }
        val outOfStock = inventory.count { !it.isAvailable || it.quantity <= 0 }

        tvTotalItems.text = total.toString()
        tvInStockItems.text = inStock.toString()
        tvOutOfStockItems.text = outOfStock.toString()

        containerInventoryList.removeAllViews()

        if (inventory.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No inventory items found. Tap '+ Add New Food Item' above."
                textSize = 14f
                setTextColor(getColor(R.color.text_secondary))
                setPadding(16, 32, 16, 32)
            }
            containerInventoryList.addView(emptyTv)
            return
        }

        val inflater = LayoutInflater.from(this)
        for (item in inventory) {
            val itemView = inflater.inflate(R.layout.item_food_inventory, containerInventoryList, false)

            val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
            val tvItemCategory: TextView = itemView.findViewById(R.id.tvItemCategory)
            val tvItemPrice: TextView = itemView.findViewById(R.id.tvItemPrice)
            val tvItemStock: TextView = itemView.findViewById(R.id.tvItemStock)
            val tvItemAvailability: TextView = itemView.findViewById(R.id.tvItemAvailability)
            val btnEditItem: Button = itemView.findViewById(R.id.btnEditItem)

            tvItemName.text = item.name
            tvItemCategory.text = "Category: ${item.category}"
            tvItemPrice.text = String.format(Locale.US, "₱%.2f", item.price)
            tvItemStock.text = "Stock: ${item.quantity} units"

            if (item.quantity > 5) {
                tvItemStock.setTextColor(getColor(R.color.accent_green))
            } else {
                tvItemStock.setTextColor(getColor(R.color.primary_red))
            }

            if (item.isAvailable && item.quantity > 0) {
                tvItemAvailability.text = "AVAILABLE"
                tvItemAvailability.setTextColor(getColor(R.color.accent_green))
            } else {
                tvItemAvailability.text = "OUT OF STOCK"
                tvItemAvailability.setTextColor(getColor(R.color.primary_red))
            }

            btnEditItem.setOnClickListener {
                showEditFoodDialog(item)
            }

            containerInventoryList.addView(itemView)
        }
    }

    private fun showAddFoodDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val etName = EditText(this).apply { hint = "Food Name (e.g. Bacon Burger)" }
        val etCategory = EditText(this).apply { hint = "Category (e.g. Burgers, Drinks)" }
        val etPrice = EditText(this).apply {
            hint = "Price (e.g. 150.00)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val etQuantity = EditText(this).apply {
            hint = "Initial Stock Quantity"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val cbAvailable = CheckBox(this).apply {
            text = "Available for ordering"
            isChecked = true
        }

        layout.addView(etName)
        layout.addView(etCategory)
        layout.addView(etPrice)
        layout.addView(etQuantity)
        layout.addView(cbAvailable)

        AlertDialog.Builder(this)
            .setTitle("Add New Food Item")
            .setView(layout)
            .setPositiveButton("Save Item") { _, _ ->
                val name = etName.text.toString().trim()
                val category = etCategory.text.toString().trim()
                val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                val quantity = etQuantity.text.toString().toIntOrNull() ?: 0
                val isAvailable = cbAvailable.isChecked

                if (name.isEmpty() || category.isEmpty() || price <= 0) {
                    Toast.makeText(this, "Please provide valid food details", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val success = dbHelper.addFoodItem(name, category, price, quantity, isAvailable)
                if (success) {
                    Toast.makeText(this, "Food item added to inventory!", Toast.LENGTH_SHORT).show()
                    loadInventoryData()
                } else {
                    Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditFoodDialog(item: FoodItem) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val etName = EditText(this).apply {
            hint = "Food Name"
            setText(item.name)
        }
        val etCategory = EditText(this).apply {
            hint = "Category"
            setText(item.category)
        }
        val etPrice = EditText(this).apply {
            hint = "Price"
            setText(item.price.toString())
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val etQuantity = EditText(this).apply {
            hint = "Stock Quantity"
            setText(item.quantity.toString())
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val cbAvailable = CheckBox(this).apply {
            text = "Available for ordering"
            isChecked = item.isAvailable && item.quantity > 0
        }

        layout.addView(etName)
        layout.addView(etCategory)
        layout.addView(etPrice)
        layout.addView(etQuantity)
        layout.addView(cbAvailable)

        AlertDialog.Builder(this)
            .setTitle("Edit '${item.name}'")
            .setView(layout)
            .setPositiveButton("Update Item") { _, _ ->
                val name = etName.text.toString().trim()
                val category = etCategory.text.toString().trim()
                val price = etPrice.text.toString().toDoubleOrNull() ?: item.price
                val quantity = etQuantity.text.toString().toIntOrNull() ?: item.quantity
                val isAvailable = cbAvailable.isChecked

                if (name.isEmpty() || category.isEmpty() || price <= 0) {
                    Toast.makeText(this, "Please provide valid food details", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val success = dbHelper.updateFoodItem(item.id, name, category, price, quantity, isAvailable)
                if (success) {
                    Toast.makeText(this, "Item updated successfully!", Toast.LENGTH_SHORT).show()
                    loadInventoryData()
                } else {
                    Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
