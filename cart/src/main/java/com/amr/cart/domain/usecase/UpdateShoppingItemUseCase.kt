package com.amr.cart.domain.usecase

import com.amr.cart.domain.repository.ShoppingListRepository
import java.util.Date
import javax.inject.Inject

class UpdateShoppingItemUseCase @Inject constructor(
    private val repository: ShoppingListRepository,
    private val getShoppingItemUseCase: GetShoppingItemUseCase,
) {
    suspend fun invoke(
        id: String,
        name: String,
        quantity: Int,
        note: String?,
    ): Result<Unit> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be empty"))
        }

        if (quantity <= 0) {
            return Result.failure(IllegalArgumentException("Quantity must be greater than 0"))
        }

        val existingItem =
            getShoppingItemUseCase.invoke(id) ?: return Result.failure(IllegalArgumentException("Item not found"))

        val updatedItem = existingItem.copy(
            name = name.trim(),
            quantity = quantity,
            note = note?.trim(),
            updatedAt = Date()
        )

        return repository.updateShoppingItem(updatedItem)
    }
}