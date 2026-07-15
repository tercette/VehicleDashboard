package com.volks.vehicledashboard.di

import com.volks.vehicledashboard.data.repository.VehicleRepositoryImpl
import com.volks.vehicledashboard.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    // @Binds: VehicleRepositoryImpl já tem @Inject constructor
    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        impl: VehicleRepositoryImpl
    ): VehicleRepository
}
