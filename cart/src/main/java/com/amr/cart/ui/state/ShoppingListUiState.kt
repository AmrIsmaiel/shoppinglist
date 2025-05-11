package com.amr.cart.ui.state

import com.amr.cart.domain.model.FilterType
import com.amr.cart.domain.model.ShoppingItem
import com.amr.cart.domain.model.SortOrder

data class ShoppingListUiState(
    val items: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filterType: FilterType = FilterType.NOT_BOUGHT,
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val searchQuery: String = ""
)
