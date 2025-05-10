package com.amr.cart.data.local

import androidx.room.Database
import androidx.room.TypeConverters

@Database(
    entities = [ShoppingItemEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class ShoppingListDatabase {
    abstract fun shoppingItemDao(): ShoppingItemDao
}