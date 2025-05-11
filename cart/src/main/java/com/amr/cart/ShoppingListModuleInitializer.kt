package com.amr.cart

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.amr.cart.data.local.ShoppingListDatabase
import com.amr.cart.data.local.datasource.NetworkMonitor
import com.amr.cart.data.local.datasource.ShoppingListLocalDataSource
import com.amr.cart.data.mockingRemote.MockShoppingListRemoteDataSource
import com.amr.cart.data.mockingRemote.ShoppingListApiService
import com.amr.cart.domain.repository.ShoppingListRepository
import com.amr.cart.domain.repository.ShoppingListRepositoryImpl
import com.amr.cart.sync.ShoppingListSyncWorker
import com.amr.cart.sync.SyncManager
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit

object ShoppingListModuleInitializer {
    private var initialized = false
    private lateinit var applicationContext: Context

    private val databaseInit by lazy {
        Room.databaseBuilder(
            applicationContext,
            ShoppingListDatabase::class.java,
            "shopping_list.db"
        ).build()
    }

    private val networkMonitorInit by lazy {
        NetworkMonitor(applicationContext)
    }

    private val remoteApiService by lazy {
        MockShoppingListRemoteDataSource()
    }

    private val syncManagerInit by lazy {
        SyncManager(applicationContext)
    }

    private val localDataSource by lazy {
        databaseInit.shoppingItemDao().let { dao ->
            ShoppingListLocalDataSource(dao)
        }
    }

    private val repositoryInit by lazy {
        ShoppingListRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteApiService,
            networkMonitor = networkMonitorInit,
            ioDispatcher = Dispatchers.IO,
            syncManager = syncManagerInit
        )
    }

    private val workManagerInit by lazy {
        WorkManager.getInstance(applicationContext)
    }

    fun initialize(context: Context) {
        if (initialized) {
            Log.d("ShoppingListModuleInitializer","ShoppingList module already initialized")
            return
        }

        applicationContext = context.applicationContext

        initializeDatabase()

        setupBackgroundSync()

        initialized = true
        Log.d("ShoppingListModuleInitializer","ShoppingList module initialized successfully")
    }

    private fun initializeDatabase() {
        databaseInit.openHelper.writableDatabase
        Log.d("ShoppingListModuleInitializer","Shopping List database initialized")
    }

    private fun setupBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<ShoppingListSyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(5, TimeUnit.MINUTES)
            .build()

        // Enqueue the work
        workManagerInit.enqueueUniquePeriodicWork(
            "shopping_list_sync",
            ExistingPeriodicWorkPolicy.UPDATE,
            syncWorkRequest
        )

        Log.d("ShoppingListModuleInitializer","Background sync scheduled")
    }

    fun getDatabase(): ShoppingListDatabase {
        checkInitialized()
        return databaseInit
    }

    fun getRepository(): ShoppingListRepository {
        checkInitialized()
        return repositoryInit
    }

    fun getNetworkMonitor(): NetworkMonitor {
        checkInitialized()
        return networkMonitorInit
    }


    fun getApiService(): ShoppingListApiService {
        checkInitialized()
        return remoteApiService
    }

    fun getSyncManager(): SyncManager {
        checkInitialized()
        return syncManagerInit
    }

    fun getApplicationContext(): Context {
        checkInitialized()
        return applicationContext
    }

    private fun checkInitialized() {
        check(initialized) { "ShoppingList module not initialized. Call initialize() first." }
    }
}