package com.amr.cart.domain.usecase

import com.amr.cart.domain.model.FilterType
import com.amr.cart.domain.model.ShoppingItem
import com.amr.cart.domain.model.SortOrder
import com.amr.cart.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetShoppingListUseCase @Inject constructor(
    private val repository: ShoppingListRepository,
) {
    fun invoke(
        filterType: FilterType = FilterType.ALL,
        sortOrder: SortOrder = SortOrder.DATE_DESC,
        searchQuery: String = "",
    ): Flow<List<ShoppingItem>> {
        return repository.getShoppingList(filterType, sortOrder, searchQuery)
    }
}