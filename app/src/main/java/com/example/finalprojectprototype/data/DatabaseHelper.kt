package com.example.finalprojectprototype.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.finalprojectprototype.models.CartItem
import com.example.finalprojectprototype.models.FoodItem
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FastFoodSystem.db"
        private const val DATABASE_VERSION = 4

        // Table Accounts
        private const val TABLE_ACCOUNTS = "Accounts"
        private const val COL_ACC_ID = "id"
        private const val COL_ACC_USERNAME = "username"
        private const val COL_ACC_PASSWORD_HASH = "password_hash"
        private const val COL_ACC_SALT = "salt"
        private const val COL_ACC_CREATED_AT = "created_at"

        // Table Inventory
        private const val TABLE_INVENTORY = "Inventory"
        private const val COL_INV_ID = "id"
        private const val COL_INV_NAME = "name"
        private const val COL_INV_CATEGORY = "category"
        private const val COL_INV_PRICE = "price"
        private const val COL_INV_QUANTITY = "quantity"
        private const val COL_INV_IS_AVAILABLE = "is_available"
        private const val COL_INV_IMAGE_RES = "image_res_name"

        // Table Orders
        private const val TABLE_ORDERS = "Orders"
        private const val COL_ORD_ID = "id"
        private const val COL_ORD_TYPE = "order_type"
        private const val COL_ORD_PAYMENT = "payment_method"
        private const val COL_ORD_TOTAL = "total_amount"
        private const val COL_ORD_DATE = "order_date"
        private const val COL_ORD_STATUS = "status"

        // Table OrderDetails
        private const val TABLE_ORDER_DETAILS = "OrderDetails"
        private const val COL_DET_ID = "id"
        private const val COL_DET_ORDER_ID = "order_id"
        private const val COL_DET_FOOD_ID = "food_id"
        private const val COL_DET_FOOD_NAME = "food_name"
        private const val COL_DET_UNIT_PRICE = "unit_price"
        private const val COL_DET_QUANTITY = "quantity"
        private const val COL_DET_SUBTOTAL = "subtotal"
        private const val COL_DET_CUSTOMIZATIONS = "customizations"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createAccountsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_ACCOUNTS (
                $COL_ACC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ACC_USERNAME TEXT UNIQUE NOT NULL,
                $COL_ACC_PASSWORD_HASH TEXT NOT NULL,
                $COL_ACC_SALT TEXT NOT NULL,
                $COL_ACC_CREATED_AT TEXT NOT NULL
            )
        """.trimIndent()

        val createInventoryTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_INVENTORY (
                $COL_INV_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_INV_NAME TEXT NOT NULL,
                $COL_INV_CATEGORY TEXT NOT NULL,
                $COL_INV_PRICE REAL NOT NULL,
                $COL_INV_QUANTITY INTEGER NOT NULL,
                $COL_INV_IS_AVAILABLE INTEGER NOT NULL,
                $COL_INV_IMAGE_RES TEXT NOT NULL
            )
        """.trimIndent()

        val createOrdersTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_ORDERS (
                $COL_ORD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ORD_TYPE TEXT NOT NULL,
                $COL_ORD_PAYMENT TEXT NOT NULL,
                $COL_ORD_TOTAL REAL NOT NULL,
                $COL_ORD_DATE TEXT NOT NULL,
                $COL_ORD_STATUS TEXT NOT NULL
            )
        """.trimIndent()

        val createOrderDetailsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_ORDER_DETAILS (
                $COL_DET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DET_ORDER_ID INTEGER NOT NULL,
                $COL_DET_FOOD_ID INTEGER NOT NULL,
                $COL_DET_FOOD_NAME TEXT NOT NULL,
                $COL_DET_UNIT_PRICE REAL NOT NULL,
                $COL_DET_QUANTITY INTEGER NOT NULL,
                $COL_DET_SUBTOTAL REAL NOT NULL,
                $COL_DET_CUSTOMIZATIONS TEXT,
                FOREIGN KEY($COL_DET_ORDER_ID) REFERENCES $TABLE_ORDERS($COL_ORD_ID)
            )
        """.trimIndent()

        db.execSQL(createAccountsTable)
        db.execSQL(createInventoryTable)
        db.execSQL(createOrdersTable)
        db.execSQL(createOrderDetailsTable)

        seedDefaultOwnerInternal(db, "markjaysontala", "legacy")
        seedInitialInventoryInternal(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDER_DETAILS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INVENTORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACCOUNTS")
        onCreate(db)
    }

    private fun seedDefaultOwnerInternal(db: SQLiteDatabase, username: String, password: String) {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ACCOUNTS", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()

        if (count == 0) {
            val salt = generateSalt()
            val hash = hashPassword(password, salt)
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val values = ContentValues().apply {
                put(COL_ACC_USERNAME, username)
                put(COL_ACC_PASSWORD_HASH, hash)
                put(COL_ACC_SALT, salt)
                put(COL_ACC_CREATED_AT, timestamp)
            }
            db.insert(TABLE_ACCOUNTS, null, values)
        }
    }

    // --- Accounts & Auth Operations ---

    fun hasOwnerAccount(): Boolean {
        val db = writableDatabase
        onCreate(db)
        seedDefaultOwnerInternal(db, "markjaysontala", "legacy")

        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ACCOUNTS", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count > 0
    }

    fun registerOwner(username: String, password: String): Boolean {
        val db = writableDatabase
        onCreate(db)

        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ACCOUNTS", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()

        if (count > 0) {
            return false
        }

        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val values = ContentValues().apply {
            put(COL_ACC_USERNAME, username)
            put(COL_ACC_PASSWORD_HASH, hash)
            put(COL_ACC_SALT, salt)
            put(COL_ACC_CREATED_AT, timestamp)
        }

        val result = db.insert(TABLE_ACCOUNTS, null, values)
        return result != -1L
    }

    fun loginOwner(username: String, password: String): Boolean {
        val db = writableDatabase
        onCreate(db)
        seedDefaultOwnerInternal(db, "markjaysontala", "legacy")

        val cursor = db.rawQuery(
            "SELECT $COL_ACC_PASSWORD_HASH, $COL_ACC_SALT FROM $TABLE_ACCOUNTS WHERE $COL_ACC_USERNAME = ?",
            arrayOf(username)
        )

        var isValid = false
        if (cursor.moveToFirst()) {
            val storedHash = cursor.getString(0)
            val salt = cursor.getString(1)
            val computedHash = hashPassword(password, salt)
            isValid = (storedHash == computedHash)
        }
        cursor.close()
        return isValid
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val input = salt + password
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    // --- Inventory Operations ---

    private fun seedInitialInventoryInternal(db: SQLiteDatabase) {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_INVENTORY", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()

        if (count > 0) return

        val items = listOf(
            ContentValues().apply {
                put(COL_INV_NAME, "Classic Cheeseburger")
                put(COL_INV_CATEGORY, "Burgers")
                put(COL_INV_PRICE, 149.00)
                put(COL_INV_QUANTITY, 25)
                put(COL_INV_IS_AVAILABLE, 1)
                put(COL_INV_IMAGE_RES, "ic_burger")
            },
            ContentValues().apply {
                put(COL_INV_NAME, "Crispy Chicken Combo")
                put(COL_INV_CATEGORY, "Meals")
                put(COL_INV_PRICE, 189.00)
                put(COL_INV_QUANTITY, 15)
                put(COL_INV_IS_AVAILABLE, 1)
                put(COL_INV_IMAGE_RES, "ic_chicken")
            },
            ContentValues().apply {
                put(COL_INV_NAME, "Loaded Cheesy Fries")
                put(COL_INV_CATEGORY, "Sides")
                put(COL_INV_PRICE, 89.00)
                put(COL_INV_QUANTITY, 30)
                put(COL_INV_IS_AVAILABLE, 1)
                put(COL_INV_IMAGE_RES, "ic_fries")
            },
            ContentValues().apply {
                put(COL_INV_NAME, "Ice Cold Soda")
                put(COL_INV_CATEGORY, "Drinks")
                put(COL_INV_PRICE, 49.00)
                put(COL_INV_QUANTITY, 50)
                put(COL_INV_IS_AVAILABLE, 1)
                put(COL_INV_IMAGE_RES, "ic_soda")
            },
            ContentValues().apply {
                put(COL_INV_NAME, "Chocolate Sundae")
                put(COL_INV_CATEGORY, "Desserts")
                put(COL_INV_PRICE, 59.00)
                put(COL_INV_QUANTITY, 20)
                put(COL_INV_IS_AVAILABLE, 1)
                put(COL_INV_IMAGE_RES, "ic_sundae")
            },
            ContentValues().apply {
                put(COL_INV_NAME, "Pepperoni Pizza Slice")
                put(COL_INV_CATEGORY, "Meals")
                put(COL_INV_PRICE, 119.00)
                put(COL_INV_QUANTITY, 10)
                put(COL_INV_IS_AVAILABLE, 1)
                put(COL_INV_IMAGE_RES, "ic_pizza")
            }
        )

        for (item in items) {
            db.insert(TABLE_INVENTORY, null, item)
        }
    }

    fun getAllInventory(): List<FoodItem> {
        val list = mutableListOf<FoodItem>()
        val db = writableDatabase

        onCreate(db)
        seedInitialInventoryInternal(db)

        val cursor = db.rawQuery("SELECT * FROM $TABLE_INVENTORY ORDER BY $COL_INV_NAME ASC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_INV_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_INV_NAME))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COL_INV_CATEGORY))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_INV_PRICE))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_INV_QUANTITY))
                val isAvailable = cursor.getInt(cursor.getColumnIndexOrThrow(COL_INV_IS_AVAILABLE)) == 1
                val imageRes = cursor.getString(cursor.getColumnIndexOrThrow(COL_INV_IMAGE_RES))

                list.add(FoodItem(id, name, category, price, quantity, isAvailable, imageRes))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getAvailableInventory(): List<FoodItem> {
        val list = mutableListOf<FoodItem>()
        val db = writableDatabase

        getAllInventory()

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_INVENTORY WHERE $COL_INV_IS_AVAILABLE = 1 AND $COL_INV_QUANTITY > 0 ORDER BY $COL_INV_CATEGORY ASC, $COL_INV_NAME ASC",
            null
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_INV_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_INV_NAME))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COL_INV_CATEGORY))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_INV_PRICE))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_INV_QUANTITY))
                val isAvailable = cursor.getInt(cursor.getColumnIndexOrThrow(COL_INV_IS_AVAILABLE)) == 1
                val imageRes = cursor.getString(cursor.getColumnIndexOrThrow(COL_INV_IMAGE_RES))

                list.add(FoodItem(id, name, category, price, quantity, isAvailable, imageRes))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addFoodItem(name: String, category: String, price: Double, quantity: Int, isAvailable: Boolean): Boolean {
        val db = writableDatabase
        val availableInt = if (quantity > 0 && isAvailable) 1 else 0
        val values = ContentValues().apply {
            put(COL_INV_NAME, name)
            put(COL_INV_CATEGORY, category)
            put(COL_INV_PRICE, price)
            put(COL_INV_QUANTITY, quantity)
            put(COL_INV_IS_AVAILABLE, availableInt)
            put(COL_INV_IMAGE_RES, "ic_food_placeholder")
        }
        val result = db.insert(TABLE_INVENTORY, null, values)
        return result != -1L
    }

    fun updateFoodItem(id: Long, name: String, category: String, price: Double, quantity: Int, isAvailable: Boolean): Boolean {
        val db = writableDatabase
        val finalAvailable = if (quantity <= 0) 0 else (if (isAvailable) 1 else 0)

        val values = ContentValues().apply {
            put(COL_INV_NAME, name)
            put(COL_INV_CATEGORY, category)
            put(COL_INV_PRICE, price)
            put(COL_INV_QUANTITY, quantity)
            put(COL_INV_IS_AVAILABLE, finalAvailable)
        }
        val rows = db.update(TABLE_INVENTORY, values, "$COL_INV_ID = ?", arrayOf(id.toString()))
        return rows > 0
    }

    fun updateStock(id: Long, newQuantity: Int): Boolean {
        val db = writableDatabase
        val isAvailable = if (newQuantity > 0) 1 else 0
        val values = ContentValues().apply {
            put(COL_INV_QUANTITY, newQuantity)
            put(COL_INV_IS_AVAILABLE, isAvailable)
        }
        val rows = db.update(TABLE_INVENTORY, values, "$COL_INV_ID = ?", arrayOf(id.toString()))
        return rows > 0
    }

    // --- Order Placement & Stock Auto-Deduction ---

    fun placeOrder(orderType: String, paymentMethod: String, cartItems: List<CartItem>): Pair<Boolean, String> {
        if (cartItems.isEmpty()) {
            return Pair(false, "Cart is empty")
        }

        val db = writableDatabase
        db.beginTransaction()
        try {
            // Step 1: Stock Validation
            for (cartItem in cartItems) {
                val cursor = db.rawQuery(
                    "SELECT $COL_INV_QUANTITY, $COL_INV_NAME, $COL_INV_IS_AVAILABLE FROM $TABLE_INVENTORY WHERE $COL_INV_ID = ?",
                    arrayOf(cartItem.foodItem.id.toString())
                )
                if (cursor.moveToFirst()) {
                    val currentStock = cursor.getInt(0)
                    val foodName = cursor.getString(1)
                    val isAvailable = cursor.getInt(2)

                    if (isAvailable == 0 || currentStock < cartItem.quantity) {
                        cursor.close()
                        return Pair(false, "Insufficient stock for '$foodName'. Available: $currentStock")
                    }
                } else {
                    cursor.close()
                    return Pair(false, "Item '${cartItem.foodItem.name}' no longer exists.")
                }
                cursor.close()
            }

            // Step 2: Insert Order Header
            val totalAmount = cartItems.sumOf { it.totalPrice }
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val orderValues = ContentValues().apply {
                put(COL_ORD_TYPE, orderType)
                put(COL_ORD_PAYMENT, paymentMethod)
                put(COL_ORD_TOTAL, totalAmount)
                put(COL_ORD_DATE, timestamp)
                put(COL_ORD_STATUS, "Completed")
            }
            val orderId = db.insert(TABLE_ORDERS, null, orderValues)
            if (orderId == -1L) {
                return Pair(false, "Failed to record order header")
            }

            // Step 3: Insert Details & Deduct Stock
            for (cartItem in cartItems) {
                val customizationsStr = cartItem.selectedAddOns.joinToString(", ")
                val detailValues = ContentValues().apply {
                    put(COL_DET_ORDER_ID, orderId)
                    put(COL_DET_FOOD_ID, cartItem.foodItem.id)
                    put(COL_DET_FOOD_NAME, cartItem.foodItem.name)
                    put(COL_DET_UNIT_PRICE, cartItem.unitPriceWithAddOns)
                    put(COL_DET_QUANTITY, cartItem.quantity)
                    put(COL_DET_SUBTOTAL, cartItem.totalPrice)
                    put(COL_DET_CUSTOMIZATIONS, customizationsStr)
                }
                db.insert(TABLE_ORDER_DETAILS, null, detailValues)

                // Stock deduction
                val newStock = cartItem.foodItem.quantity - cartItem.quantity
                val isStillAvailable = if (newStock > 0) 1 else 0
                val stockValues = ContentValues().apply {
                    put(COL_INV_QUANTITY, newStock)
                    put(COL_INV_IS_AVAILABLE, isStillAvailable)
                }
                db.update(TABLE_INVENTORY, stockValues, "$COL_INV_ID = ?", arrayOf(cartItem.foodItem.id.toString()))
            }

            db.setTransactionSuccessful()
            return Pair(true, "Order #$orderId placed successfully!")
        } catch (e: Exception) {
            return Pair(false, "Transaction failed: ${e.message}")
        } finally {
            if (db.inTransaction()) {
                db.endTransaction()
            }
        }
    }
}
