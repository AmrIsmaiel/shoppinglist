package com.amr.cart.domain.model

import java.util.Date

data class ShoppingItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val note: String?,
    val isBought: Boolean,
    val createdAt: Date,
    val updatedAt: Date
)
