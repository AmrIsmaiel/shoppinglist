package com.amr.cart.data.model

data class ShoppingItemDto(
    val id: String,
    val name: String,
    val quantity: Int,
    val note: String?,
    val isBought: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)