package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val category: String,
    val description: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val isIncome: Boolean = false, // false for Gasto (Expense), true for Ingreso (Income)
    val userEmail: String = ""
) {
    val dateString: String
        get() {
            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            return sdf.format(Date(dateMillis))
        }
}
