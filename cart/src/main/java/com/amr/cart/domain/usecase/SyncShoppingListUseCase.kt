package com.amr.cart.domain.usecase

import com.amr.cart.domain.repository.ShoppingListRepository
import javax.inject.Inject

class SyncShoppingListUseCase @Inject constructor(
    private val repository: ShoppingListRepository
) {
    suspend fun invoke(): Result<Unit> {
        return repository.syncWithRemote()
    }
}