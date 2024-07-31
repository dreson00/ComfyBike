package com.bk.bk1.dependencyInjection

import android.app.Application
import android.content.Context
import com.bk.bk1.data.ComfortIndexRecordRepository
import com.bk.bk1.data.TrackRecordRepository
import com.bk.bk1.utilities.DefaultLocationClient
import com.bk.bk1.utilities.LocationClient
import com.bk.bk1.utilities.SensorManager
import com.bk.bk1.utilities.TrackingManager
import com.google.android.gms.location.LocationServices
import com.movesense.mds.Mds
import com.squareup.otto.Bus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


@Module
@InstallIn(ServiceComponent::class)
class ServiceModule {

    @ServiceScoped
    @Provides
    fun provideLocationClient(
        @ApplicationContext context: Context
    ): LocationClient {
        return DefaultLocationClient(
            context,
            LocationServices.getFusedLocationProviderClient(context)
        )
    }

    @ServiceScoped
    @Provides
    fun provideSensorManager(
        bus: Bus,
        mds: Mds
    ): SensorManager {
        return SensorManager(bus, mds)
    }

    @ServiceScoped
    @Provides
    fun provideTrackingManager(
        application: Application,
        comfortIndexRecordRepository: ComfortIndexRecordRepository,
        trackRecordRepository: TrackRecordRepository,
        bus: Bus,
        locationClient: LocationClient
    ): TrackingManager {
        return TrackingManager(
            application,
            comfortIndexRecordRepository,
            trackRecordRepository,
            bus,
            locationClient
        )
    }

}