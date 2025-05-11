package com.amr.cart.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amr.cart.domain.model.FilterType
import com.amr.cart.domain.model.ShoppingItem
import com.amr.cart.domain.model.SortOrder
import com.amr.cart.ui.state.ShoppingListEvent
import com.amr.cart.ui.viewmodel.ShoppingListViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onAddNewItem: () -> Unit,
    onEditItem: (String) -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.handleEvent(ShoppingListEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping List") },
                actions = {

                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            imageVector = if (showSearchBar) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = if (showSearchBar) "Hide Search" else "Show Search"
                        )
                    }


                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Filter"
                            )
                        }

                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Items") },
                                onClick = {
                                    viewModel.handleEvent(ShoppingListEvent.SetFilter(FilterType.ALL))
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Active Items") },
                                onClick = {
                                    viewModel.handleEvent(ShoppingListEvent.SetFilter(FilterType.NOT_BOUGHT))
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Bought Items") },
                                onClick = {
                                    viewModel.handleEvent(ShoppingListEvent.SetFilter(FilterType.BOUGHT))
                                    showFilterMenu = false
                                }
                            )
                        }
                    }

                    // Sort action
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Sort"
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Newest First") },
                                onClick = {
                                    viewModel.handleEvent(ShoppingListEvent.SetSortOrder(SortOrder.DATE_DESC))
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Oldest First") },
                                onClick = {
                                    viewModel.handleEvent(ShoppingListEvent.SetSortOrder(SortOrder.DATE_ASC))
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNewItem) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = showSearchBar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { query ->
                        viewModel.handleEvent(ShoppingListEvent.SetSearchQuery(query))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search items") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.handleEvent(ShoppingListEvent.SetSearchQuery(""))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (uiState.filterType) {
                        FilterType.ALL -> "Showing all items"
                        FilterType.NOT_BOUGHT -> "Showing active items"
                        FilterType.BOUGHT -> "Showing bought items"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.items.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            uiState.searchQuery.isNotEmpty() -> "No items match your search"
                            uiState.filterType == FilterType.BOUGHT -> "No bought items"
                            uiState.filterType == FilterType.NOT_BOUGHT -> "No active items"
                            else -> "Your shopping list is empty"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.items) { item ->
                        ShoppingItemRow(
                            item = item,
                            onItemClick = { onEditItem(item.id) },
                            onBoughtToggle = { isBought ->
                                viewModel.handleEvent(ShoppingListEvent.MarkItemBought(item.id, isBought))
                            },
                            onDeleteClick = {
                                viewModel.handleEvent(ShoppingListEvent.DeleteItem(item.id))
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onItemClick: () -> Unit,
    onBoughtToggle: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onItemClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    IconButton(onClick = { onBoughtToggle(!item.isBought) }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = if (item.isBought) "Mark as not bought" else "Mark as bought",
                            tint = if (item.isBought) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete item"
                        )
                    }
                }
            }

            Text(
                text = "Quantity: ${item.quantity}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (!item.note.isNullOrEmpty()) {
                Text(
                    text = "Note: ${item.note}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Text(
                text = "Updated: ${dateFormatter.format(item.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}