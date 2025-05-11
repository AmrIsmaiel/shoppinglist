package com.amr.cart.ui.state

import com.amr.cart.domain.model.FilterType
import com.amr.cart.domain.model.SortOrder

sealed interface ShoppingListEvent {
    data class AddItem(val name: String, val quantity: Int, val note: String? = null) : ShoppingListEvent
    data class UpdateItem(val id: String, val name: String, val quantity: Int, val note: String? = null) :
        ShoppingListEvent

    data class DeleteItem(val id: String) : ShoppingListEvent
    data class MarkItemBought(val id: String, val isBought: Boolean) : ShoppingListEvent
    data class SetFilter(val filterType: FilterType) : ShoppingListEvent
    data class SetSortOrder(val sortOrder: SortOrder) : ShoppingListEvent
    data class SetSearchQuery(val query: String) : ShoppingListEvent
    data object ClearError : ShoppingListEvent
}