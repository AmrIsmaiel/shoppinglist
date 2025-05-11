package com.amr.cart.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amr.cart.ui.view.ItemDetailScreen
import com.amr.cart.ui.view.ShoppingListScreen

@Composable
fun ShoppingListNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.ShoppingList.route
    ) {
        composable(Screen.ShoppingList.route) {
            ShoppingListScreen(
                onAddNewItem = { navController.navigate(Screen.ItemDetail.route) },
                onEditItem = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                }
            )
        }

        composable(
            route = Screen.ItemDetail.routeWithArgs,
            arguments = listOf(
                navArgument(Screen.ItemDetail.itemIdArg) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            ItemDetailScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}