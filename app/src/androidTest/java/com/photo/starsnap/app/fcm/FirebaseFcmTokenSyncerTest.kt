package com.photo.starsnap.app.fcm

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.photo.starsnap.datastore.FcmTokenStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseFcmTokenSyncerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val tokenStore = FcmTokenStore(context)
    private val workScheduler = RecordingWorkScheduler()
    private var initialAuthenticated = false
    private var initialPendingToken: String? = null

    @Before
    fun setUp() {
        initialAuthenticated = tokenStore.isAuthenticated()
        initialPendingToken = tokenStore.pendingToken()
        tokenStore.setAuthenticated(false)
        tokenStore.pendingToken()?.let(tokenStore::clearPendingTokenIfMatches)
    }

    @After
    fun tearDown() {
        tokenStore.pendingToken()?.let(tokenStore::clearPendingTokenIfMatches)
        initialPendingToken?.let(tokenStore::savePendingToken)
        tokenStore.setAuthenticated(initialAuthenticated)
    }

    @Test
    fun refreshedTokenIsPersistedButNotUploadedWhileSignedOut() {
        val syncer = FirebaseFcmTokenSyncer(tokenStore, workScheduler)

        syncer.syncToken("local-test-fcm-token")

        assertEquals("local-test-fcm-token", tokenStore.pendingToken())
        assertEquals(0, workScheduler.enqueueCount)
    }

    @Test
    fun firebaseInstallationIdIsEncodedAsAnExplicitServerTarget() {
        assertEquals("fid:installation-id", firebaseInstallationTarget(" installation-id "))
    }

    private class RecordingWorkScheduler : FcmTokenWorkScheduler {
        var enqueueCount = 0

        override fun enqueue() {
            enqueueCount += 1
        }

        override fun cancel() = Unit
    }
}
