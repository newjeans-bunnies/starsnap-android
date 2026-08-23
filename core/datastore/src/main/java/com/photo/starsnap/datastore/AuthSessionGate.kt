package com.photo.starsnap.datastore

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/** Serializes refresh, logout, and authenticated FCM registration operations. */
@Singleton
class AuthSessionGate @Inject constructor() {
    private val mutex = Mutex()

    suspend fun <T> withLock(action: suspend () -> T): T = mutex.withLock {
        action()
    }
}
