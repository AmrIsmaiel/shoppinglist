package com.amr.cart

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

object ShoppingListBuilder {
    private fun getIntent(context: Context): Intent {
        return Intent(context, ShoppingListActivity::class.java)
    }

    fun launch(context: Context) {
        context.startActivity(getIntent(context))
    }

    fun registerForActivityResult(
        activity: ComponentActivity,
        callback: (resultCode: Int, data: Intent?) -> Unit,
    ): ActivityResultLauncher<Intent> {
        return activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            callback(result.resultCode, result.data)
        }
    }
}
