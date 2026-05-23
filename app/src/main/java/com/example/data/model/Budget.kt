package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // e.g., "General", "Comida", "Transporte" etc.
    val limitAmount: Double,
    val monthYear: String, // e.g., "05/2026"
    val userEmail: String = ""
)
