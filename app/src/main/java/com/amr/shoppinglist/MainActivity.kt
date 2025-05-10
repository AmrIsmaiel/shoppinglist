package com.amr.shoppinglist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.amr.cart.ShoppingListBuilder
import com.amr.shoppinglist.ui.theme.ShoppinglistTheme

class MainActivity : ComponentActivity() {
    private lateinit var shoppingListLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shoppingListLauncher = ShoppingListBuilder.registerForActivityResult(this) { resultCode, data ->
            if (resultCode == RESULT_OK) {
                // Handle successful result
            }
        }
        enableEdgeToEdge()
        setContent {
            ShoppinglistTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        onClick = { launchShoppingList() }
                    )
                }
            }
        }
    }

    private fun launchShoppingList() {
        ShoppingListBuilder.launch(this)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(modifier = modifier, onClick = {
        onClick.invoke()
    }) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ShoppinglistTheme {
        Greeting("Android", onClick = {})
    }
}