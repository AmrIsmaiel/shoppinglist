package com.amr.cart.di

import com.amr.cart.ShoppingListModuleInitializer
import com.amr.cart.data.local.ShoppingListDatabase
import com.amr.cart.data.local.datasource.NetworkMonitor
import com.amr.cart.data.mockingRemote.ShoppingListApiService
import com.amr.cart.domain.repository.ShoppingListRepository
import com.amr.cart.domain.usecase.AddShoppingItemUseCase
import com.amr.cart.domain.usecase.DeleteShoppingItemUseCase
import com.amr.cart.domain.usecase.GetShoppingItemUseCase
import com.amr.cart.domain.usecase.GetShoppingListUseCase
import com.amr.cart.domain.usecase.MarkItemAsBoughtUseCase
import com.amr.cart.domain.usecase.SyncShoppingListUseCase
import com.amr.cart.domain.usecase.UpdateShoppingItemUseCase
import com.amr.cart.sync.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ShoppingListModule {
    @Provides
    @Singleton
    fun provideShoppingListDatabase(): ShoppingListDatabase {
        return ShoppingListModuleInitializer.getDatabase()
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(): NetworkMonitor {
        return ShoppingListModuleInitializer.getNetworkMonitor()
    }

    @Provides
    @Singleton
    fun provideShoppingListApiService(): ShoppingListApiService {
        return ShoppingListModuleInitializer.getApiService()
    }

    @Provides
    @Singleton
    fun provideSyncManager(): SyncManager {
        return ShoppingListModuleInitializer.getSyncManager()
    }

    @Provides
    @Singleton
    fun provideShoppingListRepository(): ShoppingListRepository {
        return ShoppingListModuleInitializer.getRepository()
    }

    @Provides
    fun provideGetShoppingListUseCase(repository: ShoppingListRepository): GetShoppingListUseCase {
        return GetShoppingListUseCase(repository)
    }

    @Provides
    fun provideGetShoppingItemUseCase(repository: ShoppingListRepository): GetShoppingItemUseCase {
        return GetShoppingItemUseCase(repository)
    }

    @Provides
    fun provideAddShoppingItemUseCase(repository: ShoppingListRepository): AddShoppingItemUseCase {
        return AddShoppingItemUseCase(repository)
    }

    @Provides
    fun provideUpdateShoppingItemUseCase(
        repository: ShoppingListRepository,
        getShoppingItemUseCase: GetShoppingItemUseCase,
    ): UpdateShoppingItemUseCase {
        return UpdateShoppingItemUseCase(repository, getShoppingItemUseCase)
    }

    @Provides
    fun provideDeleteShoppingItemUseCase(repository: ShoppingListRepository): DeleteShoppingItemUseCase {
        return DeleteShoppingItemUseCase(repository)
    }

    @Provides
    fun provideMarkItemAsBoughtUseCase(repository: ShoppingListRepository): MarkItemAsBoughtUseCase {
        return MarkItemAsBoughtUseCase(repository)
    }

    @Provides
    fun provideSyncShoppingListUseCase(repository: ShoppingListRepository): SyncShoppingListUseCase {
        return SyncShoppingListUseCase(repository)
    }
}