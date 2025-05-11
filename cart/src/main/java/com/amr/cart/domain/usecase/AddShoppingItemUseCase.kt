package com.amr.cart.domain.usecase

import com.amr.cart.domain.model.ShoppingItem
import com.amr.cart.domain.repository.ShoppingListRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlin.Result

class AddShoppingItemUseCase @Inject constructor(
    private val repository: ShoppingListRepository,
) {
    suspend fun invoke(
        name: String,
        quantity: Int,
        note: String? = null,
    ): Result<Unit> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be empty"))
        }

        if (quantity <= 0) {
            return Result.failure(IllegalArgumentException("Quantity must be greater than 0"))
        }

        val now = Date()
        val item = ShoppingItem(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            quantity = quantity,
            note = note?.trim(),
            isBought = false,
            createdAt = now,
            updatedAt = now
        )

        return repository.addShoppingItem(item)
    }
}