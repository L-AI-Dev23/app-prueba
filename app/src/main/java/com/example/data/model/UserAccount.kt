package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val email: String,
    val displayName: String,
    val passwordHash: String = "",
    val isGoogleUser: Boolean = false,
    val photoUrl: String? = null
)
