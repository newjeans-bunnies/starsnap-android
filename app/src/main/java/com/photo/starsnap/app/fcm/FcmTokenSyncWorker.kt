package com.photo.starsnap.app.fcm

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.photo.starsnap.datastore.FcmTokenStore
import com.photo.starsnap.datastore.AuthSessionGate
import com.photo.starsnap.network.user.UserRepository
import com.photo.starsnap.network.user.dto.UpdateFcmTokenRequest
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import retrofit2.HttpException

class FcmTokenSyncWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val dependencies = EntryPointAccessors.fromApplication(
            applicationContext,
            FcmTokenSyncWorkerEntryPoint::class.java,
        )
        return dependencies.authSessionGate().withLock {
            val tokenStore = dependencies.fcmTokenStore()
            if (!tokenStore.isAuthenticated()) return@withLock Result.success()

            if (tokenStore.isTokenRemovalPending()) {
                return@withLock try {
                    dependencies.userRepository().updateFcmToken(
                        UpdateFcmTokenRequest(token = null),
                    )
                    tokenStore.clearTokenRemovalPending()
                    tokenStore.clearPendingToken()
                    Result.success()
                } catch (_: IOException) {
                    Result.retry()
                } catch (error: HttpException) {
                    handleHttpError(error, tokenStore)
                } catch (_: Exception) {
                    Result.retry()
                }
            }

            if (!tokenStore.arePushNotificationsEnabled()) return@withLock Result.success()

            val token = tokenStore.pendingToken() ?: return@withLock Result.success()
            try {
                dependencies.userRepository().updateFcmToken(
                    UpdateFcmTokenRequest(token = token),
                )
                tokenStore.clearPendingTokenIfMatches(token)
                Result.success()
            } catch (_: IOException) {
                Result.retry()
            } catch (error: HttpException) {
                handleHttpError(error, tokenStore)
            } catch (_: Exception) {
                Result.retry()
            }
        }
    }

    private fun handleHttpError(error: HttpException, tokenStore: FcmTokenStore): Result = when {
        error.code() == 401 -> {
            tokenStore.setAuthenticated(false)
            Result.success()
        }
        // A forbidden token update does not prove that the login session is invalid.
        error.code() == 403 -> Result.failure()
        error.code() == 408 || error.code() == 429 || error.code() >= 500 -> Result.retry()
        else -> Result.failure()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FcmTokenSyncWorkerEntryPoint {
    fun userRepository(): UserRepository
    fun fcmTokenStore(): FcmTokenStore
    fun authSessionGate(): AuthSessionGate
}
