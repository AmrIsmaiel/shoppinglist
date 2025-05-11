package com.amr.cart.domain.repository

import com.amr.cart.data.local.ShoppingItemEntity
import com.amr.cart.data.local.datasource.NetworkMonitor
import com.amr.cart.data.local.datasource.ShoppingListLocalDataSource
import com.amr.cart.data.mockingRemote.ShoppingListApiService
import com.amr.cart.data.model.ShoppingItemDto
import com.amr.cart.di.IoDispatcher
import com.amr.cart.domain.model.FilterType
import com.amr.cart.domain.model.ShoppingItem
import com.amr.cart.domain.model.SortOrder
import com.amr.cart.domain.model.toDomain
import com.amr.cart.domain.model.toDto
import com.amr.cart.domain.model.toEntity
import com.amr.cart.sync.SyncManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepositoryImpl @Inject constructor(
    private val localDataSource: ShoppingListLocalDataSource,
    private val remoteDataSource: ShoppingListApiService,
    private val networkMonitor: NetworkMonitor,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val syncManager: SyncManager
) : ShoppingListRepository {

    override fun getShoppingList(
        filterType: FilterType,
        sortOrder: SortOrder,
        searchQuery: String
    ): Flow<List<ShoppingItem>> {
        // Try to sync if connected but don't block the UI
        if (networkMonitor.isConnected()) {
            CoroutineScope(ioDispatcher).launch {
                syncWithRemote()
            }
        }

        return localDataSource.getShoppingItems(filterType, sortOrder, searchQuery)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getShoppingItem(id: String): ShoppingItem? {
        return withContext(ioDispatcher) {
            localDataSource.getAllShoppingItemsSnapshot()
                .firstOrNull { it.id == id }
                ?.toDomain()
        }
    }

    override suspend fun addShoppingItem(item: ShoppingItem): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val entity = item.toEntity(syncedWithRemote = false)
                localDataSource.insertShoppingItem(entity)

                if (networkMonitor.isConnected()) {
                    try {
                        val dto = item.toDto()
                        remoteDataSource.addShoppingItem(dto)

                        // Mark as synced
                        localDataSource.updateShoppingItem(entity.copy(syncedWithRemote = true))
                    } catch (e: Exception) {
                        // Failed to sync with remote, but local operation succeeded
                        // Schedule a background sync
                        syncManager.scheduleSyncWork()
                    }
                } else {
                    // Offline mode, schedule sync for later
                    syncManager.scheduleSyncWork()
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateShoppingItem(item: ShoppingItem): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val entity = item.toEntity(syncedWithRemote = false)
                localDataSource.updateShoppingItem(entity)

                if (networkMonitor.isConnected()) {
                    try {
                        val dto = item.toDto()
                        remoteDataSource.updateShoppingItem(item.id, dto)

                        // Mark as synced
                        localDataSource.updateShoppingItem(entity.copy(syncedWithRemote = true))
                    } catch (e: Exception) {
                        // Failed to sync with remote, but local operation succeeded
                        syncManager.scheduleSyncWork()
                    }
                } else {
                    // Offline mode, schedule sync for later
                    syncManager.scheduleSyncWork()
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteShoppingItem(id: String): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                localDataSource.deleteShoppingItem(id)

                if (networkMonitor.isConnected()) {
                    try {
                        remoteDataSource.deleteShoppingItem(id)
                    } catch (e: Exception) {
                        // Failed to sync with remote, but local operation succeeded
                        syncManager.scheduleSyncWork()
                    }
                } else {
                    // Offline mode, schedule sync for later
                    syncManager.scheduleSyncWork()
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun markItemAsBought(id: String, isBought: Boolean): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val timestamp = Date()
                localDataSource.updateItemBoughtStatus(id, isBought, timestamp)

                if (networkMonitor.isConnected()) {
                    try {
                        // Get the updated item from local DB
                        val updatedItem = localDataSource.getAllShoppingItemsSnapshot()
                                              .firstOrNull { it.id == id } ?: throw Exception("Item not found")

                        // Update remote
                        val dto = updatedItem.toDto()
                        remoteDataSource.updateShoppingItem(id, dto)

                        // Mark as synced
                        localDataSource.updateShoppingItem(updatedItem.copy(syncedWithRemote = true))
                    } catch (e: Exception) {
                        // Failed to sync with remote, but local operation succeeded
                        syncManager.scheduleSyncWork()
                    }
                } else {
                    // Offline mode, schedule sync for later
                    syncManager.scheduleSyncWork()
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun syncWithRemote(): Result<Unit> {
        return withContext(ioDispatcher) {
            if (!networkMonitor.isConnected()) {
                return@withContext Result.failure(Exception("No network connection"))
            }

            try {
                // 1. Get all items from remote
                val remoteItems = remoteDataSource.getShoppingList()

                // 2. Get all items from local
                val localItems = localDataSource.getAllShoppingItemsSnapshot()

                // 3. Apply last-write-wins by timestamp strategy
                val itemsToUpdate = resolveConflicts(localItems, remoteItems)

                // 4. Update local database with resolved items
                localDataSource.updateItems(itemsToUpdate)

                // 5. Push local changes to remote
                pushLocalChanges(localItems, remoteItems)

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun pushLocalChanges(
        localItems: List<ShoppingItemEntity>,
        remoteItems: List<ShoppingItemDto>
    ) {
        // Find items that are not synced with remote
        val unsyncedItems = localItems.filter { !it.syncedWithRemote }

        for (localItem in unsyncedItems) {
            try {
                val dto = localItem.toDto()

                // Check if item exists in remote
                val remoteItem = remoteItems.firstOrNull { it.id == localItem.id }

                if (remoteItem == null) {
                    // Item doesn't exist in remote, add it
                    remoteDataSource.addShoppingItem(dto)
                } else {
                    // Item exists in remote, update it
                    remoteDataSource.updateShoppingItem(localItem.id, dto)
                }

                // Mark as synced
                localDataSource.updateShoppingItem(localItem.copy(syncedWithRemote = true))
            } catch (e: Exception) {
                // Continue with next item if one fails
                continue
            }
        }
    }

    private fun resolveConflicts(
        localItems: List<ShoppingItemEntity>,
        remoteItems: List<ShoppingItemDto>
    ): List<ShoppingItemEntity> {
        val mergedItems = mutableListOf<ShoppingItemEntity>()
        val localItemsMap = localItems.associateBy { it.id }

        // Process all remote items
        for (remoteItem in remoteItems) {
            val localItem = localItemsMap[remoteItem.id]

            if (localItem == null) {
                // Item exists only in remote, add to local
                mergedItems.add(remoteItem.toEntity(syncedWithRemote = true))
            } else {
                // Item exists in both, apply last-write-wins strategy
                val mergedItem = if (remoteItem.updatedAt > localItem.updatedAt.time) {
                    // Remote is newer
                    remoteItem.toEntity(syncedWithRemote = true)
                } else if (remoteItem.updatedAt < localItem.updatedAt.time) {
                    // Local is newer
                    localItem.copy(syncedWithRemote = false)
                } else {
                    // Same timestamp, no conflict
                    localItem.copy(syncedWithRemote = true)
                }

                mergedItems.add(mergedItem)
            }
        }

        // Add local items that don't exist in remote
        val remoteItemIds = remoteItems.map { it.id }.toSet()
        val localOnlyItems = localItems.filter { !remoteItemIds.contains(it.id) }
        mergedItems.addAll(localOnlyItems)

        return mergedItems
    }
}