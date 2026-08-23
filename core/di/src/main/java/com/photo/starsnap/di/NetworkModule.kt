package com.photo.starsnap.di

import android.content.Context
import com.photo.starsnap.datastore.SessionManager
import com.photo.starsnap.datastore.FcmTokenStore
import com.photo.starsnap.datastore.AuthSessionGate
import com.photo.starsnap.datastore.TokenManager
import com.photo.starsnap.di.Url.BASE_URL
import com.photo.starsnap.network.auth.AuthApi
import com.photo.starsnap.network.message.ChatSocketManager
import com.photo.starsnap.network.message.MessageApi
import com.photo.starsnap.network.report.ReportApi
import com.photo.starsnap.network.snap.SnapApi
import com.photo.starsnap.network.star.StarApi
import com.photo.starsnap.network.token.TokenApi
import com.photo.starsnap.network.token.TokenRepository
import com.photo.starsnap.network.user.UserApi
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


object Url {
    const val BASE_URL = "http://master.hamtory.com:8080/"
}



@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideTokenApi(retrofit: Retrofit): TokenApi = retrofit.create(TokenApi::class.java)

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideReportApi(retrofit: Retrofit): ReportApi = retrofit.create(ReportApi::class.java)

    @Provides
    @Singleton
    fun provideSnapApi(retrofit: Retrofit): SnapApi = retrofit.create(SnapApi::class.java)

    @Provides
    @Singleton
    fun provideStarApi(retrofit: Retrofit): StarApi = retrofit.create(StarApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideMessageApi(retrofit: Retrofit): MessageApi = retrofit.create(MessageApi::class.java)

    @Provides
    @Singleton
    fun provideChatSocketManager(okHttpClient: OkHttpClient): ChatSocketManager =
        ChatSocketManager(okHttpClient, BASE_URL)

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

    @Provides
    @Singleton
    fun provideAuthInterceptor(): AuthInterceptor =
        AuthInterceptor()

    @Provides
    @Singleton
    fun provideAuthAuthenticator(
        tokenManager: TokenManager,
        tokenRepository: Lazy<TokenRepository>, // 여기를 Lazy로 변경
        cookieJar: PersistentCookieJar,
        sessionManager: SessionManager,
        fcmTokenStore: FcmTokenStore,
        authSessionGate: AuthSessionGate,
    ): AuthAuthenticator =
        AuthAuthenticator(
            tokenManager,
            tokenRepository,
            cookieJar,
            sessionManager,
            fcmTokenStore,
            authSessionGate,
        )

    @Singleton
    @Provides
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager =
        TokenManager(context)

    @Singleton
    @Provides
    fun providePersistentCookieJar(@ApplicationContext context: Context): PersistentCookieJar =
        PersistentCookieJar(context)


    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        cookieJar: PersistentCookieJar,
        loggerInterceptor: HttpLoggingInterceptor,
        authAuthenticator: AuthAuthenticator,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient().newBuilder().apply {
            // 서버에서 내려주는 Set-Cookie (HttpOnly 포함)를 영속 CookieJar에 저장해 자동으로 전송합니다.
            // 주의: 서버가 Secure 플래그가 설정된 쿠키를 사용하면 HTTPS로 통신해야 쿠키가 전송됩니다.
            cookieJar(cookieJar)
            connectTimeout(10, TimeUnit.SECONDS)
            readTimeout(10, TimeUnit.SECONDS)
            writeTimeout(10, TimeUnit.SECONDS)
            addInterceptor(loggerInterceptor)
            addInterceptor(authInterceptor)
            authenticator(authAuthenticator)
        }.build()
    }
}
