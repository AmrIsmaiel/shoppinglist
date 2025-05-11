package com.amr.shoppinglist

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.amr.cart.ShoppingListModuleInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ShoppingListApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workManagerConfig: Configuration
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        ShoppingListModuleInitializer.initialize(this)
    }


    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}