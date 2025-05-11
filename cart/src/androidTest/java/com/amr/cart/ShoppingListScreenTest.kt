package com.amr.cart

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amr.cart.domain.model.ShoppingItem
import com.amr.cart.theme.ShoppinglistTheme
import com.amr.cart.ui.view.ShoppingListScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ShoppingListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var repository: FakeShoppingListRepository

    @Before
    fun setUp() {

        // Access the repository from the injected ViewModels if needed
        repository = FakeShoppingListRepository()

        // Add some test data to the repository
        val testItems = listOf(
            ShoppingItem(
                id = "1",
                name = "Apples",
                quantity = 5,
                note = "Red ones",
                isBought = false,
                createdAt = Date(),
                updatedAt = Date()
            ),
            ShoppingItem(
                id = "2",
                name = "Bananas",
                quantity = 3,
                note = null,
                isBought = true,
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        repository.addTestItems(testItems)
    }

    @Test
    fun display_shoppingList_showsItems() {
        composeTestRule.setContent {
            ShoppinglistTheme {
                ShoppingListScreen(
                    onAddNewItem = {},
                    onEditItem = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Apples").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quantity: 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Note: Red ones").assertIsDisplayed()
    }

    @Test
    fun clicking_addButton_navigatesToDetailScreen() {
        var addButtonClicked = false

        composeTestRule.setContent {
            ShoppinglistTheme {
                ShoppingListScreen(
                    onAddNewItem = { addButtonClicked = true },
                    onEditItem = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add Item").performClick()

        // Verify that the onAddNewItem callback was called
        assert(addButtonClicked)
    }

    @Test
    fun filter_showsCorrectItems() {
        composeTestRule.setContent {
            ShoppinglistTheme {
                ShoppingListScreen(
                    onAddNewItem = {},
                    onEditItem = {}
                )
            }
        }

        // Initially, active items should be shown (Apples)
        composeTestRule.onNodeWithText("Apples").assertIsDisplayed()

        // Bananas is bought, so it shouldn't be visible initially
        composeTestRule.onNodeWithText("Bananas").assertDoesNotExist()

        // Open the filter menu
        composeTestRule.onNodeWithContentDescription("Filter").performClick()

        // Select "Bought Items"
        composeTestRule.onNodeWithText("Bought Items").performClick()

        // Now Bananas should be visible, but not Apples
        composeTestRule.onNodeWithText("Bananas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Apples").assertDoesNotExist()
    }

    @Test
    fun search_findsMatchingItems() {
        composeTestRule.setContent {
            ShoppinglistTheme {
                ShoppingListScreen(
                    onAddNewItem = {},
                    onEditItem = {}
                )
            }
        }

        // Open search
        composeTestRule.onNodeWithContentDescription("Show Search").performClick()

        // Type in search query
        composeTestRule.onNodeWithText("Search items").performTextInput("Apple")

        // Only Apples should be visible
        composeTestRule.onNodeWithText("Apples").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bananas").assertDoesNotExist()
    }
}