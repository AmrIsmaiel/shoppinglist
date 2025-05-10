package com.amr.cart.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "items")
data class ShoppingItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val quantity: Int,
    val note: String?,
    val isBought: Boolean,
    val createdAt: Date,
    val updatedAt: Date,
    val syncedWithRemote: Boolean,
)
