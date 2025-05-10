package com.amr.cart.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM items")
    fun getAllItemsFlow(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM items WHERE isBought = :isBought")
    fun getItemsByStatusFlow(isBought: Boolean): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%'")
    fun searchItemsFlow(query: String): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' AND isBought = :isBought")
    fun searchItemsByStatusFlow(query: String, isBought: Boolean): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM items ORDER BY updatedAt ASC")
    fun getAllItemsSortedByDateAscFlow(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM items ORDER BY updatedAt DESC")
    fun getAllItemsSortedByDateDescFlow(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM items")
    suspend fun getAllItemsSnapshot(): List<ShoppingItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity)

    @Update
    suspend fun updateItem(item: ShoppingItemEntity)

    @Delete
    suspend fun deleteItem(item: ShoppingItemEntity)

    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: String)

    @Query("UPDATE items SET isBought = :isBought, updatedAt = :timestamp WHERE id = :itemId")
    suspend fun updateItemBoughtStatus(itemId: String, isBought: Boolean, timestamp: Date)

    @Transaction
    suspend fun upsertItems(items: List<ShoppingItemEntity>) {
        items.forEach { insertItem(it) }
    }
}