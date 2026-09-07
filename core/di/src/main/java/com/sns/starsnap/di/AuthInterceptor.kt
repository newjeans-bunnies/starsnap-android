package com.sns.starsnap.di

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection.HTTP_OK
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().withCookieOnlyAuthentication()

        // Public auth endpoints still use CookieJar when needed, but must never recurse into refresh.
        if (request.tag(SkipAuthentication::class.java) != null) {
            Log.d(TAG, "Auth: false request - automatic refresh disabled")
            return chain.proceed(request)
        }

        // HttpOnly 쿠키는 OkHttpClient의 CookieJar가 자동으로 처리함
        // Authorization 헤더 추가는 불필요
        Log.d(TAG, "요청 진행 - HttpOnly 쿠키는 CookieJar가 자동 처리")

        val response = chain.proceed(request)

        if (response.code != HTTP_OK) {
            Log.e(TAG, "Response code: ${response.code} | URL: ${response.request.url}")
        }

        return response
    }
}

internal fun Request.withCookieOnlyAuthentication(): Request =
    newBuilder()
        .removeHeader("Authorization")
        .build()
        .withAuthenticationRequestTags()
