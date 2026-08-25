package com.photo.starsnap.di

import com.photo.starsnap.model.photo.PhotoRepository
import com.photo.starsnap.model.photo.PhotoRepositoryImpl
import com.photo.starsnap.network.auth.AuthApiRepositoryImpl
import com.photo.starsnap.network.auth.AuthRepository
import com.photo.starsnap.network.file.FileApiRepositoryImpl
import com.photo.starsnap.network.file.FileRepository
import com.photo.starsnap.network.message.MessageApiRepositoryImpl
import com.photo.starsnap.network.message.MessageRepository
import com.photo.starsnap.network.report.ReportApiRepositoryImpl
import com.photo.starsnap.network.report.ReportRepository
import com.photo.starsnap.network.snap.SnapApiRepositoryImpl
import com.photo.starsnap.network.snap.SnapRepository
import com.photo.starsnap.network.star.StarApiRepositoryImpl
import com.photo.starsnap.network.star.StarRepository
import com.photo.starsnap.network.token.TokenApiRepositoryImpl
import com.photo.starsnap.network.token.TokenRepository
import com.photo.starsnap.network.user.UserApiRepositoryImpl
import com.photo.starsnap.network.user.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideTokenRepository(tokenApiRepositoryImpl: TokenApiRepositoryImpl): TokenRepository =
        tokenApiRepositoryImpl

    @Provides
    @Singleton
    fun provideAuthRepository(authApiRepositoryImpl: AuthApiRepositoryImpl): AuthRepository =
        authApiRepositoryImpl

    @Provides
    @Singleton
    fun provideUserRepository(userApiRepositoryImpl: UserApiRepositoryImpl): UserRepository =
        userApiRepositoryImpl

    @Provides
    @Singleton
    fun provideMessageRepository(messageApiRepositoryImpl: MessageApiRepositoryImpl): MessageRepository =
        messageApiRepositoryImpl

    @Provides
    @Singleton
    fun provideSnapRepository(snapApiRepositoryImpl: SnapApiRepositoryImpl): SnapRepository =
        snapApiRepositoryImpl

    @Provides
    @Singleton
    fun provideFileRepository(fileApiRepositoryImpl: FileApiRepositoryImpl): FileRepository =
        fileApiRepositoryImpl

    @Provides
    @Singleton
    fun provideStarRepository(starApiRepositoryImpl: StarApiRepositoryImpl): StarRepository =
        starApiRepositoryImpl

    @Provides
    @Singleton
    fun provideReportRepository(reportApiRepositoryImpl: ReportApiRepositoryImpl): ReportRepository =
        reportApiRepositoryImpl

    @Provides
    @Singleton
    fun providePhotoRepository(photoRepositoryImpl: PhotoRepositoryImpl): PhotoRepository =
        photoRepositoryImpl


}
