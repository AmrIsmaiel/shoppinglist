package com.amr.cart

import com.amr.cart.domain.model.FilterType
import com.amr.cart.domain.model.ShoppingItem
import com.amr.cart.domain.model.SortOrder
import com.amr.cart.domain.repository.ShoppingListRepository
import java.util.Date

class FakeShoppingListRepository : ShoppingListRepository {
    private val items = mutableListOf<ShoppingItem>()

    fun addTestItems(newItems: List<ShoppingItem>) {
        items.addAll(newItems)
    }

    override fun getShoppingList(
        filterType: FilterType,
        sortOrder: SortOrder,
        searchQuery: String
    ): kotlinx.coroutines.flow.Flow<List<ShoppingItem>> {
        return kotlinx.coroutines.flow.flow {
            val filteredItems = when (filterType) {
                FilterType.ALL -> items
                FilterType.BOUGHT -> items.filter { it.isBought }
                FilterType.NOT_BOUGHT -> items.filter { !it.isBought }
            }

            val searchedItems = if (searchQuery.isNotEmpty()) {
                filteredItems.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    (it.note?.contains(searchQuery, ignoreCase = true) ?: false)
                }
            } else {
                filteredItems
            }

            val sortedItems = when (sortOrder) {
                SortOrder.DATE_ASC -> searchedItems.sortedBy { it.updatedAt }
                SortOrder.DATE_DESC -> searchedItems.sortedByDescending { it.updatedAt }
            }

            emit(sortedItems)
        }
    }

    override suspend fun getShoppingItem(id: String): ShoppingItem? {
        return items.find { it.id == id }
    }

    override suspend fun addShoppingItem(item: ShoppingItem): Result<Unit> {
        items.add(item)
        return Result.success(Unit)
    }

    override suspend fun updateShoppingItem(item: ShoppingItem): Result<Unit> {
        val index = items.indexOfFirst { it.id == item.id }
        if (index != -1) {
            items[index] = item
            return Result.success(Unit)
        }
        return Result.failure(Exception("Item not found"))
    }

    override suspend fun deleteShoppingItem(id: String): Result<Unit> {
        val removed = items.removeIf { it.id == id }
        return if (removed) Result.success(Unit) else Result.failure(Exception("Item not found"))
    }

    override suspend fun markItemAsBought(id: String, isBought: Boolean): Result<Unit> {
        val index = items.indexOfFirst { it.id == id }
        if (index != -1) {
            items[index] = items[index].copy(isBought = isBought, updatedAt = Date())
            return Result.success(Unit)
        }
        return Result.failure(Exception("Item not found"))
    }

    override suspend fun syncWithRemote(): Result<Unit> {
        return Result.success(Unit)
    }
}