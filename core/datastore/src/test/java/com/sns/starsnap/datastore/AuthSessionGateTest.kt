package com.sns.starsnap.datastore

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthSessionGateTest {

    @Test
    fun `session operations cannot overlap`() = runBlocking {
        val gate = AuthSessionGate()
        val firstEntered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val order = mutableListOf<String>()

        coroutineScope {
            val first = async {
                gate.withLock {
                    order += "first-start"
                    firstEntered.complete(Unit)
                    releaseFirst.await()
                    order += "first-end"
                }
            }
            firstEntered.await()

            val second = async {
                gate.withLock { order += "second" }
            }
            yield()
            assertEquals(listOf("first-start"), order)

            releaseFirst.complete(Unit)
            first.await()
            second.await()
        }

        assertEquals(listOf("first-start", "first-end", "second"), order)
    }
}
