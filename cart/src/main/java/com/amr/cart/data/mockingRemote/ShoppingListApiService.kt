package com.amr.cart.data.mockingRemote

import com.amr.cart.data.model.ShoppingItemDto

interface ShoppingListApiService {
    // we should annotate every function here with retrofit annotation but I prefer
    // not to implement retrofit dependencies for such a small thing we will not use.
    suspend fun getShoppingList(): List<ShoppingItemDto>

    suspend fun addShoppingItem(item: ShoppingItemDto): ShoppingItemDto

    suspend fun updateShoppingItem(id: String, item: ShoppingItemDto): ShoppingItemDto

    suspend fun deleteShoppingItem(id: String)
}