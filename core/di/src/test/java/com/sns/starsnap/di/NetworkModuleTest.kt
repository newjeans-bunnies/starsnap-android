package com.sns.starsnap.di

import okhttp3.Authenticator
import okhttp3.CookieJar
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkModuleTest {

    @Test
    fun `presigned upload client has no auth cookies redirects or logging`() {
        val client = NetworkModule().providePresignedUploadOkHttpClient()

        assertSame(CookieJar.NO_COOKIES, client.cookieJar)
        assertSame(Authenticator.NONE, client.authenticator)
        assertSame(Authenticator.NONE, client.proxyAuthenticator)
        assertFalse(client.followRedirects)
        assertFalse(client.followSslRedirects)
        assertTrue(client.interceptors.none { it is HttpLoggingInterceptor })
        assertTrue(client.networkInterceptors.none { it is HttpLoggingInterceptor })
    }
}
