package com.amr.cart.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amr.cart.ui.state.ItemDetailEvent
import com.amr.cart.ui.viewmodel.ItemDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.handleEvent(ItemDetailEvent.ClearError)
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = if (uiState.isNewItem) "Add Item" else "Edit Item"
            )
        }, navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, contentDescription = "Back"
                )
            }
        }, actions = {
            IconButton(onClick = { viewModel.handleEvent(ItemDetailEvent.SaveItem) }) {
                Icon(
                    imageVector = Icons.Default.Check, contentDescription = "Save"
                )
            }
        })
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.handleEvent(ItemDetailEvent.NameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Item Name") },
                    singleLine = true,
                    isError = uiState.name.isBlank()
                )

                Spacer(modifier = Modifier.height(16.dp))


                OutlinedTextField(
                    value = if (uiState.quantity > 0) uiState.quantity.toString() else "",
                    onValueChange = { viewModel.handleEvent(ItemDetailEvent.QuantityChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = uiState.quantity <= 0
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = { viewModel.handleEvent(ItemDetailEvent.NoteChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note (Optional)") },
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))


                Button(
                    onClick = { viewModel.handleEvent(ItemDetailEvent.SaveItem) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.name.isNotBlank() && uiState.quantity > 0
                ) {
                    Text(text = "Save")
                }
            }
        }
    }
}