package com.amr.cart.domain.model

import com.amr.cart.data.local.ShoppingItemEntity
import com.amr.cart.data.model.ShoppingItemDto
import java.util.Date

fun ShoppingItem.toEntity(syncedWithRemote: Boolean = false): ShoppingItemEntity {
    return ShoppingItemEntity(
        id = id,
        name = name,
        quantity = quantity,
        note = note,
        isBought = isBought,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncedWithRemote = syncedWithRemote
    )
}

fun ShoppingItemEntity.toDomain(): ShoppingItem {
    return ShoppingItem(
        id = id,
        name = name,
        quantity = quantity,
        note = note,
        isBought = isBought,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ShoppingItem.toDto(): ShoppingItemDto {
    return ShoppingItemDto(
        id = id,
        name = name,
        quantity = quantity,
        note = note,
        isBought = isBought,
        createdAt = createdAt.time,
        updatedAt = updatedAt.time
    )
}

fun ShoppingItemDto.toEntity(syncedWithRemote: Boolean = true): ShoppingItemEntity {
    return ShoppingItemEntity(
        id = id,
        name = name,
        quantity = quantity,
        note = note,
        isBought = isBought,
        createdAt = Date(createdAt),
        updatedAt = Date(updatedAt),
        syncedWithRemote = syncedWithRemote
    )
}

fun ShoppingItemEntity.toDto(): ShoppingItemDto {
    return ShoppingItemDto(
        id = id,
        name = name,
        quantity = quantity,
        note = note,
        isBought = isBought,
        createdAt = createdAt.time,
        updatedAt = updatedAt.time
    )
}