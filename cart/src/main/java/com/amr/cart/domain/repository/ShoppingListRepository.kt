package com.amr.cart.domain.repository

import com.amr.cart.domain.model.FilterType
import com.amr.cart.domain.model.ShoppingItem
import com.amr.cart.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun getShoppingList(
        filterType: FilterType = FilterType.ALL,
        sortOrder: SortOrder = SortOrder.DATE_DESC,
        searchQuery: String = ""
    ): Flow<List<ShoppingItem>>

    suspend fun getShoppingItem(id: String): ShoppingItem?

    suspend fun addShoppingItem(item: ShoppingItem): Result<Unit>

    suspend fun updateShoppingItem(item: ShoppingItem): Result<Unit>

    suspend fun deleteShoppingItem(id: String): Result<Unit>

    suspend fun markItemAsBought(id: String, isBought: Boolean): Result<Unit>

    suspend fun syncWithRemote(): Result<Unit>
}