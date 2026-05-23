package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "sync_logs")
data class SyncLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val timestamp: Long = System.currentTimeMillis(),
    val transactionCount: Int,
    val budgetCount: Int,
    val backupPayload: String, // Encrypted/Compressed representative JSON string
    val isSuccess: Boolean = true
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
