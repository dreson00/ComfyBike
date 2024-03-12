package com.bk.bk1.dependencyInjection

import android.bluetooth.BluetoothManager
import android.content.Context
import com.bk.bk1.utilities.BluetoothScanManager
import com.bk.bk1.utilities.DefaultLocationClient
import com.bk.bk1.utilities.LocationClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {

    @ViewModelScoped
    @Provides
    fun provideLocationClient(
        @ApplicationContext context: Context
    ): LocationClient {
        return DefaultLocationClient(
            context,
            LocationServices.getFusedLocationProviderClient(context)
        )
    }

    @ViewModelScoped
    @Provides
    fun provideBluetoothScanManager(
        bluetoothManager: BluetoothManager
    ) = BluetoothScanManager(bluetoothManager)


}