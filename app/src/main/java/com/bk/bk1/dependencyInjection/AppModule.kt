package com.bk.bk1.dependencyInjection

import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import androidx.room.Room
import com.bk.bk1.data.TrackDatabase
import com.bk.bk1.utilities.BluetoothScanManager
import com.bk.bk1.utilities.BluetoothStateReceiver
import com.bk.bk1.utilities.BluetoothStateUpdater
import com.bk.bk1.utilities.ExportManager
import com.bk.bk1.utilities.LocationStateReceiver
import com.bk.bk1.utilities.LocationStateUpdater
import com.bk.bk1.utilities.SensorManager
import com.movesense.mds.Mds
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

    @Singleton
    @Provides
    fun provideBluetoothScanManager(
        bluetoothManager: BluetoothManager
    ) = BluetoothScanManager(bluetoothManager)

    @Singleton
    @Provides
    fun provideMds(
        @ApplicationContext context: Context
    ): Mds {
        return Mds.builder().build(context)
    }

    @Singleton
    @Provides
    fun provideSensorManager(
        mds: Mds,
        bluetoothManager: BluetoothManager
    ): SensorManager {
        return SensorManager(mds, bluetoothManager)
    }

    @Singleton
    @Provides
    fun provideBluetoothStateUpdater(
        bluetoothManager: BluetoothManager
    ): BluetoothStateUpdater {
        return BluetoothStateUpdater(bluetoothManager)
    }

    @Singleton
    @Provides
    fun provideBluetoothStateReceiver(
        bluetoothStateUpdater: BluetoothStateUpdater
    ): BluetoothStateReceiver {
        return BluetoothStateReceiver(bluetoothStateUpdater)
    }

    @Singleton
    @Provides
    fun provideLocationStateUpdater(): LocationStateUpdater {
        return LocationStateUpdater()
    }

    @Singleton
    @Provides
    fun provideLocationStateReceiver(
        @ApplicationContext context: Context,
        locationStateUpdater: LocationStateUpdater
    ): LocationStateReceiver {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationStateReceiver(
            locationManager,
            locationStateUpdater
        )
    }

    @Singleton
    @Provides
    fun provideFileManager(
        @ApplicationContext context: Context
    ): ExportManager {
        return ExportManager(context)
    }

}