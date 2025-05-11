package com.amr.cart.domain.usecase

import com.amr.cart.domain.model.ShoppingItem
import com.amr.cart.domain.repository.ShoppingListRepository
import javax.inject.Inject

class GetShoppingItemUseCase @Inject constructor(
    private val repository: ShoppingListRepository,
) {
    suspend fun invoke(id: String): ShoppingItem? {
        return repository.getShoppingItem(id)
    }
}