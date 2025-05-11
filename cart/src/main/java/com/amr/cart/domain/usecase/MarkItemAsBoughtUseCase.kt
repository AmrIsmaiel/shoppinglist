package com.amr.cart.domain.usecase

import com.amr.cart.domain.repository.ShoppingListRepository
import javax.inject.Inject

class MarkItemAsBoughtUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend fun invoke(id: String, isBought: Boolean): Result<Unit> {
        return repository.markItemAsBought(id, isBought)
    }
}