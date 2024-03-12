package com.bk.bk1.dependencyInjection

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.room.Room
import com.bk.bk1.data.TrackDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideTrackDatabase(
        @ApplicationContext appContext: Context
    ) = Room.databaseBuilder(
        appContext.applicationContext,
        TrackDatabase::class.java,
        "TrackDatabase"
    ).fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun provideTrackRecordDao(
        trackDatabase: TrackDatabase
    ) = trackDatabase.trackRecordDao

    @Singleton
    @Provides
    fun provideComfortIndexRecordDao(
        trackDatabase: TrackDatabase
    ) = trackDatabase.comfortIndexRecordDao

    @Singleton
    @Provides
    fun provideBluetoothManager(
        @ApplicationContext context: Context
    ) = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager


}