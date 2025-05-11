package com.amr.cart.ui.viewmodel

import androidx.lifecycle.ViewModel
import javax.inject.Inject
import com.amr.cart.domain.usecase.GetShoppingListUseCase
import com.amr.cart.domain.usecase.AddShoppingItemUseCase
import com.amr.cart.domain.usecase.UpdateShoppingItemUseCase
import com.amr.cart.domain.usecase.DeleteShoppingItemUseCase
import com.amr.cart.domain.usecase.MarkItemAsBoughtUseCase
import com.amr.cart.ui.state.ShoppingListEvent
import com.amr.cart.ui.state.ShoppingListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.amr.cart.domain.model.FilterType
import com.amr.cart.domain.model.ShoppingItem
import com.amr.cart.domain.model.SortOrder

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val getShoppingListUseCase: GetShoppingListUseCase,
    private val addShoppingItemUseCase: AddShoppingItemUseCase,
    private val updateShoppingItemUseCase: UpdateShoppingItemUseCase,
    private val deleteShoppingItemUseCase: DeleteShoppingItemUseCase,
    private val markItemAsBoughtUseCase: MarkItemAsBoughtUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState

    private val _filterType = MutableStateFlow(FilterType.NOT_BOUGHT)
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class) val shoppingItems: StateFlow<List<ShoppingItem>> = combine(
        _filterType, _sortOrder, _searchQuery
    ) { filterType, sortOrder, searchQuery ->
        Triple(filterType, sortOrder, searchQuery)
    }.flatMapLatest { (filterType, sortOrder, searchQuery) ->
        getShoppingListUseCase.invoke(filterType, sortOrder, searchQuery)
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            shoppingItems.collect { items ->
                _uiState.update { it.copy(items = items) }
            }
        }
    }

    fun handleEvent(event: ShoppingListEvent) {
        when (event) {
            is ShoppingListEvent.AddItem -> addItem(event.name, event.quantity, event.note)
            is ShoppingListEvent.UpdateItem -> updateItem(event.id, event.name, event.quantity, event.note)
            is ShoppingListEvent.DeleteItem -> deleteItem(event.id)
            is ShoppingListEvent.MarkItemBought -> markItemBought(event.id, event.isBought)
            is ShoppingListEvent.SetFilter -> setFilter(event.filterType)
            is ShoppingListEvent.SetSortOrder -> setSortOrder(event.sortOrder)
            is ShoppingListEvent.SetSearchQuery -> setSearchQuery(event.query)
            is ShoppingListEvent.ClearError -> clearError()
        }
    }

    private fun addItem(name: String, quantity: Int, note: String?) {
        viewModelScope.launch {
            setLoading(true)

            try {
                val result = addShoppingItemUseCase.invoke(name, quantity, note)

                // Guard against null result
                if (result != null) {
                    result.onFailure { error ->
                        _uiState.update { it.copy(error = error.message ?: "Unknown error occurred") }
                    }
                } else {
                    // If result is null, set a specific error
                    _uiState.update { it.copy(error = "Internal error: Use case returned null") }
                    println("WARNING: addShoppingItemUseCase returned null instead of a Result")
                }
            } catch (e: Exception) {
                // Catch any exceptions during use case invocation
                _uiState.update { it.copy(error = "Exception: ${e.message}") }
                println("Exception during use case invocation: ${e.message}")
                e.printStackTrace()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun updateItem(id: String, name: String, quantity: Int, note: String?) {
        viewModelScope.launch {
            setLoading(true)

            updateShoppingItemUseCase.invoke(id, name, quantity, note).onSuccess {
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }

            setLoading(false)
        }
    }

    private fun deleteItem(id: String) {
        viewModelScope.launch {
            setLoading(true)

            deleteShoppingItemUseCase.invoke(id).onSuccess {
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }

            setLoading(false)
        }
    }

    private fun markItemBought(id: String, isBought: Boolean) {
        viewModelScope.launch {
            setLoading(true)

            markItemAsBoughtUseCase.invoke(id, isBought).onSuccess {
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }

            setLoading(false)
        }
    }

    private fun setFilter(filterType: FilterType) {
        _filterType.value = filterType
        _uiState.update { it.copy(filterType = filterType) }
    }

    private fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
        _uiState.update { it.copy(sortOrder = sortOrder) }
    }

    private fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
