package com.photo.starsnap.app.fcm

import com.photo.starsnap.network.notification.FcmTokenSyncer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FcmModule {

    @Binds
    @Singleton
    internal abstract fun bindFcmTokenSyncer(
        implementation: FirebaseFcmTokenSyncer,
    ): FcmTokenSyncer

    @Binds
    @Singleton
    internal abstract fun bindFcmTokenWorkScheduler(
        implementation: WorkManagerFcmTokenWorkScheduler,
    ): FcmTokenWorkScheduler
}
