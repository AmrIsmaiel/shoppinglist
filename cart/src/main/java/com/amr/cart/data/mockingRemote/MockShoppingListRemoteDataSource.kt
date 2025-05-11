package com.amr.cart.data.mockingRemote

import com.amr.cart.data.model.ShoppingItemDto
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

class MockShoppingListRemoteDataSource @Inject constructor() : ShoppingListApiService {
    private val items = mutableListOf<ShoppingItemDto>()
    private val networkDelayMs = 500L
    private val failureRate = 0.1f

    override suspend fun getShoppingList(): List<ShoppingItemDto> {
        simulateNetworkConditions()
        return items.toList()
    }

    override suspend fun addShoppingItem(item: ShoppingItemDto): ShoppingItemDto {
        simulateNetworkConditions()
        val existingItemIndex = items.indexOfFirst { it.id == item.id }
        if (existingItemIndex >= 0) {
            items[existingItemIndex] = item
        } else {
            items.add(item)
        }
        return item
    }

    override suspend fun updateShoppingItem(id: String, item: ShoppingItemDto): ShoppingItemDto {
        simulateNetworkConditions()
        val existingItemIndex = items.indexOfFirst { it.id == id }
        if (existingItemIndex >= 0) {
            items[existingItemIndex] = item
            return item
        } else {
            throw NoSuchElementException("Item with id $id not found")
        }
    }

    override suspend fun deleteShoppingItem(id: String) {
        simulateNetworkConditions()
        val existingItemIndex = items.indexOfFirst { it.id == id }
        if (existingItemIndex >= 0) {
            items.removeAt(existingItemIndex)
        } else {
            throw NoSuchElementException("Item with id $id not found")
        }
    }

    private suspend fun simulateNetworkConditions() {
        delay(networkDelayMs + Random.nextLong(100))

        if (Random.nextFloat() < failureRate) {
            throw Exception("Network error")
        }
    }
}