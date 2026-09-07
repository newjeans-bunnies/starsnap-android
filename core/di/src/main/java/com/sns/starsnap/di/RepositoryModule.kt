package com.sns.starsnap.di

import com.sns.starsnap.model.photo.PhotoRepository
import com.sns.starsnap.model.photo.PhotoRepositoryImpl
import com.sns.starsnap.network.auth.AuthApiRepositoryImpl
import com.sns.starsnap.network.auth.AuthRepository
import com.sns.starsnap.network.file.FileApiRepositoryImpl
import com.sns.starsnap.network.file.FileRepository
import com.sns.starsnap.network.message.MessageApiRepositoryImpl
import com.sns.starsnap.network.message.MessageRepository
import com.sns.starsnap.network.report.ReportApiRepositoryImpl
import com.sns.starsnap.network.report.ReportRepository
import com.sns.starsnap.network.snap.SnapApiRepositoryImpl
import com.sns.starsnap.network.snap.SnapRepository
import com.sns.starsnap.network.star.StarApiRepositoryImpl
import com.sns.starsnap.network.star.StarRepository
import com.sns.starsnap.network.token.TokenApiRepositoryImpl
import com.sns.starsnap.network.token.TokenRepository
import com.sns.starsnap.network.user.UserApiRepositoryImpl
import com.sns.starsnap.network.user.UserRepository
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
