package com.amr.cart.ui.navigation

sealed class Screen(val route: String) {
    data object ShoppingList : Screen("list")

    data object ItemDetail : Screen("detail") {
        const val itemIdArg = "itemId"
        const val routeWithArgs = "detail?$itemIdArg={$itemIdArg}"

        fun createRoute(itemId: String): String {
            return "detail?$itemIdArg=$itemId"
        }
    }
}