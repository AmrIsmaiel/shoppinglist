package com.amr.cart.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.amr.cart.domain.model.FilterType
import com.amr.cart.domain.model.ShoppingItem
import com.amr.cart.domain.model.SortOrder
import com.amr.cart.domain.usecase.AddShoppingItemUseCase
import com.amr.cart.domain.usecase.DeleteShoppingItemUseCase
import com.amr.cart.domain.usecase.GetShoppingListUseCase
import com.amr.cart.domain.usecase.MarkItemAsBoughtUseCase
import com.amr.cart.domain.usecase.UpdateShoppingItemUseCase
import com.amr.cart.ui.state.ShoppingListEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getShoppingListUseCase: GetShoppingListUseCase
    private lateinit var addShoppingItemUseCase: AddShoppingItemUseCase
    private lateinit var updateShoppingItemUseCase: UpdateShoppingItemUseCase
    private lateinit var deleteShoppingItemUseCase: DeleteShoppingItemUseCase
    private lateinit var markItemAsBoughtUseCase: MarkItemAsBoughtUseCase

    private lateinit var viewModel: ShoppingListViewModel

    private val testItems = listOf(
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getShoppingListUseCase = mock()
        addShoppingItemUseCase = mock()
        updateShoppingItemUseCase = mock()
        deleteShoppingItemUseCase = mock()
        markItemAsBoughtUseCase = mock()

        whenever(getShoppingListUseCase.invoke(any(), any(), any())).thenReturn(flowOf(testItems))

        viewModel = ShoppingListViewModel(
            getShoppingListUseCase,
            addShoppingItemUseCase,
            updateShoppingItemUseCase,
            deleteShoppingItemUseCase,
            markItemAsBoughtUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(FilterType.NOT_BOUGHT, initialState.filterType)
            assertEquals(SortOrder.DATE_DESC, initialState.sortOrder)
            assertEquals("", initialState.searchQuery)
            assertFalse(initialState.isLoading)
            assertNull(initialState.error)
            testScheduler.advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `set filter updates state`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(FilterType.NOT_BOUGHT, initialState.filterType)

            viewModel.handleEvent(ShoppingListEvent.SetFilter(FilterType.ALL))
            testScheduler.advanceUntilIdle()

            val updatedState = awaitItem()
            assertEquals(FilterType.ALL, updatedState.filterType)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `set sort order updates state`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(SortOrder.DATE_DESC, initialState.sortOrder)

            viewModel.handleEvent(ShoppingListEvent.SetSortOrder(SortOrder.DATE_ASC))
            testScheduler.advanceUntilIdle()

            val updatedState = awaitItem()
            assertEquals(SortOrder.DATE_ASC, updatedState.sortOrder)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `set search query updates state`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals("", initialState.searchQuery)

            viewModel.handleEvent(ShoppingListEvent.SetSearchQuery("apple"))
            testScheduler.advanceUntilIdle()

            val updatedState = awaitItem()
            assertEquals("apple", updatedState.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `add item calls use case and manages loading state`() = runTest {
        doAnswer { Result.success(Unit) }
            .whenever(addShoppingItemUseCase).invoke(any(), any(), any())


        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)

            viewModel.handleEvent(ShoppingListEvent.AddItem("Oranges", 4, "Sweet ones"))

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            testScheduler.advanceUntilIdle()

            val finalState = awaitItem()
            assertFalse(finalState.isLoading)

            verify(addShoppingItemUseCase).invoke("Oranges", 4, "Sweet ones")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `mark item bought calls use case`() = runTest {
        doAnswer { Result.success(Unit) }
            .whenever(markItemAsBoughtUseCase).invoke(any(), any())

        val itemId = "test-id"

        viewModel.handleEvent(ShoppingListEvent.MarkItemBought(itemId, true))
        testScheduler.advanceUntilIdle()


        verify(markItemAsBoughtUseCase).invoke(itemId, true)
    }

    @Test
    fun `delete item calls use case`() = runTest {
        doAnswer { Result.success(Unit) }
            .whenever(deleteShoppingItemUseCase).invoke(any())

        val itemId = "test-id"

        viewModel.handleEvent(ShoppingListEvent.DeleteItem(itemId))
        testScheduler.advanceUntilIdle()

        verify(deleteShoppingItemUseCase).invoke(itemId)
    }

    @Test
    fun `error is handled correctly`() = runTest {
        val errorMessage = "Name cannot be empty"
        val exception = IllegalArgumentException(errorMessage)
        reset(addShoppingItemUseCase)

        // Reset the mocks before testing
        reset(addShoppingItemUseCase)

        // Approach 1: using whenever with thenReturn
        println("Approach 1: whenever/thenReturn")
        whenever(addShoppingItemUseCase.invoke(any(), any(), any()))
            .thenReturn(Result.failure(IllegalArgumentException("Test error")))

        val result1 = addShoppingItemUseCase.invoke("Test", 1, null)
        println("Result from approach 1: $result1")

        // Reset the mock
        reset(addShoppingItemUseCase)

        // Approach 2: using doAnswer with wheneverrson
        println("Approach 2: doAnswer/whenever")
        doAnswer {
            println("doAnswer was called!")
            Result.failure<Unit>(IllegalArgumentException("Test error"))
        }.whenever(addShoppingItemUseCase).invoke(any(), any(), any())

        val result2 = addShoppingItemUseCase.invoke("Test", 1, null)
        println("Result from approach 2: $result2")

        // Reset the mock
        reset(addShoppingItemUseCase)

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertNull(initialState.error)

            viewModel.handleEvent(ShoppingListEvent.AddItem("", 1, null))
            testScheduler.advanceUntilIdle()

            val loadingState = awaitItem()
            println("Loading state: $loadingState")
            assertTrue(loadingState.isLoading)
            assertNull(loadingState.error)

            val errorState = awaitItem()
            println("Error state: $errorState")
            assertEquals(errorMessage, errorState.error)

            // Clear the error
            viewModel.handleEvent(ShoppingListEvent.ClearError)
            println("Clear error event dispatched")

            // Final state should have the error cleared
            val clearedState = awaitItem()
            println("Cleared state: $clearedState")
            assertNull(clearedState.error)

        }
        verify(addShoppingItemUseCase).invoke("Test", 0, null)
    }

    @Test
    fun `update item calls use case using doAnswer`() = runTest {
        doAnswer {
            Result.success(Unit)
        }.whenever(updateShoppingItemUseCase).invoke(any(), any(), any(), any())

        val itemId = "test-id"
        val name = "Updated Name"
        val quantity = 10
        val note = "Updated Note"

        viewModel.handleEvent(ShoppingListEvent.UpdateItem(itemId, name, quantity, note))
        testScheduler.advanceUntilIdle()

        verify(updateShoppingItemUseCase).invoke(itemId, name, quantity, note)
    }
}
