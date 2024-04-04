package com.bk.bk1.dependencyInjection

import android.content.Context
import android.location.LocationManager
import com.bk.bk1.utilities.LocationStateReceiver
import com.bk.bk1.utilities.LocationStateUpdater
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped


@Module
@InstallIn(ActivityComponent::class)
class ActivityModule {
    @ActivityScoped
    @Provides
    fun provideLocationStateUpdater(): LocationStateUpdater {
        return LocationStateUpdater()
    }

    @ActivityScoped
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
}