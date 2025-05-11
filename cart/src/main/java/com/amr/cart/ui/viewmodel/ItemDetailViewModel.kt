package com.amr.cart.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amr.cart.domain.usecase.AddShoppingItemUseCase
import com.amr.cart.domain.usecase.GetShoppingItemUseCase
import com.amr.cart.domain.usecase.UpdateShoppingItemUseCase
import com.amr.cart.ui.state.ItemDetailEvent
import com.amr.cart.ui.state.ItemDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val getShoppingItemUseCase: GetShoppingItemUseCase,
    private val addShoppingItemUseCase: AddShoppingItemUseCase,
    private val updateShoppingItemUseCase: UpdateShoppingItemUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemDetailUiState())
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    private val itemId: String? = savedStateHandle["itemId"]

    init {
        if (itemId != null) {
            loadItem(itemId)
        }
    }

    private fun loadItem(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val item = getShoppingItemUseCase.invoke(id)
            if (item != null) {
                _uiState.update {
                    it.copy(
                        name = item.name,
                        quantity = item.quantity,
                        note = item.note ?: "",
                        isNewItem = false,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        error = "Item not found", isLoading = false
                    )
                }
            }
        }
    }

    fun handleEvent(event: ItemDetailEvent) {
        when (event) {
            is ItemDetailEvent.NameChanged -> _uiState.update { it.copy(name = event.name) }
            is ItemDetailEvent.QuantityChanged -> {
                val quantity = event.quantity.toIntOrNull() ?: 0
                _uiState.update { it.copy(quantity = quantity) }
            }

            is ItemDetailEvent.NoteChanged -> _uiState.update { it.copy(note = event.note) }
            is ItemDetailEvent.SaveItem -> saveItem()
            is ItemDetailEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun saveItem() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentState = _uiState.value

            val result = if (currentState.isNewItem) {
                addShoppingItemUseCase.invoke(name = currentState.name,
                    quantity = currentState.quantity,
                    note = currentState.note.ifEmpty { null })
            } else {
                val id = itemId ?: return@launch
                updateShoppingItemUseCase.invoke(id = id,
                    name = currentState.name,
                    quantity = currentState.quantity,
                    note = currentState.note.ifEmpty { null })
            }

            result.onSuccess {
                _uiState.update { it.copy(isSaved = true, isLoading = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message, isLoading = false) }
            }
        }
    }
}
