package com.amr.cart.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.amr.cart.domain.usecase.SyncShoppingListUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class ShoppingListSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncShoppingListUseCase: SyncShoppingListUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Add a small delay to prevent immediate retries
            val backoffCount = runAttemptCount.toInt()
            if (backoffCount > 0) {
                delay(INITIAL_BACKOFF_DELAY_MS * (1 shl (backoffCount - 1)))
            }

            val syncResult = syncShoppingListUseCase.invoke()

            if (syncResult.isSuccess) {
                Result.success()
            } else {
                if (runAttemptCount < MAX_RETRIES) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_DELAY_MS = 5000L
    }
}