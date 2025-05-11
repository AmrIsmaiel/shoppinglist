package com.amr.cart.domain.usecase

import com.amr.cart.domain.repository.ShoppingListRepository
import javax.inject.Inject

class DeleteShoppingItemUseCase @Inject constructor(
    private val repository: ShoppingListRepository,
) {
    suspend fun invoke(id: String): Result<Unit> {
        return repository.deleteShoppingItem(id)
    }
}