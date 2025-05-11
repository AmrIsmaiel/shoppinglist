package com.amr.cart.ui.state

sealed interface ItemDetailEvent {
    data class NameChanged(val name: String) : ItemDetailEvent
    data class QuantityChanged(val quantity: String) : ItemDetailEvent
    data class NoteChanged(val note: String) : ItemDetailEvent
    data object SaveItem : ItemDetailEvent
    data object ClearError : ItemDetailEvent
}