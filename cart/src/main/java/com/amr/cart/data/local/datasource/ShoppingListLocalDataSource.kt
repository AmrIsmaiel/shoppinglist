package com.amr.cart.data.local.datasource

import com.amr.cart.domain.model.FilterType
import com.amr.cart.domain.model.SortOrder
import com.amr.cart.data.local.ShoppingItemDao
import com.amr.cart.data.local.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class ShoppingListLocalDataSource @Inject constructor(
    private val shoppingItemDao: ShoppingItemDao
) {
    fun getShoppingItems(
        filterType: FilterType,
        sortOrder: SortOrder,
        searchQuery: String
    ): Flow<List<ShoppingItemEntity>> {
        // Base query depending on filter type
        val baseQuery = when (filterType) {
            FilterType.ALL -> if (searchQuery.isEmpty()) {
                shoppingItemDao.getAllItemsFlow()
            } else {
                shoppingItemDao.searchItemsFlow(searchQuery)
            }
            FilterType.BOUGHT -> if (searchQuery.isEmpty()) {
                shoppingItemDao.getItemsByStatusFlow(true)
            } else {
                shoppingItemDao.searchItemsByStatusFlow(searchQuery, true)
            }
            FilterType.NOT_BOUGHT -> if (searchQuery.isEmpty()) {
                shoppingItemDao.getItemsByStatusFlow(false)
            } else {
                shoppingItemDao.searchItemsByStatusFlow(searchQuery, false)
            }
        }

        // Apply sorting
        return when (sortOrder) {
            SortOrder.DATE_ASC -> baseQuery.map { items ->
                items.sortedBy { it.updatedAt }
            }
            SortOrder.DATE_DESC -> baseQuery.map { items ->
                items.sortedByDescending { it.updatedAt }
            }
        }
    }

    suspend fun getAllShoppingItemsSnapshot(): List<ShoppingItemEntity> {
        return shoppingItemDao.getAllItemsSnapshot()
    }

    suspend fun insertShoppingItem(item: ShoppingItemEntity) {
        shoppingItemDao.insertItem(item)
    }

    suspend fun updateShoppingItem(item: ShoppingItemEntity) {
        shoppingItemDao.updateItem(item)
    }

    suspend fun deleteShoppingItem(itemId: String) {
        shoppingItemDao.deleteItemById(itemId)
    }

    suspend fun updateItemBoughtStatus(itemId: String, isBought: Boolean, timestamp: Date) {
        shoppingItemDao.updateItemBoughtStatus(itemId, isBought, timestamp)
    }

    suspend fun updateItems(items: List<ShoppingItemEntity>) {
        shoppingItemDao.upsertItems(items)
    }
}
