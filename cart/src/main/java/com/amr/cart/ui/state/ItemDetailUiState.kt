package com.amr.cart.ui.state

data class ItemDetailUiState(
    val name: String = "",
    val quantity: Int = 1,
    val note: String = "",
    val isNewItem: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)
