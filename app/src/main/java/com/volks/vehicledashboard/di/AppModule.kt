package com.volks.vehicledashboard.di

import com.volks.vehicledashboard.data.repository.CarVehicleRepositoryImpl
import com.volks.vehicledashboard.data.repository.VehicleRepositoryImpl
import com.volks.vehicledashboard.domain.repository.VehicleRepository
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Fonte dos dados do veículo:
     * - false: simulação mockada (interativa — pedais, ignição, abastecer)
     * - true: VHAL real via CarPropertyManager (somente leitura; exige AAOS + permissões)
     *
     * Trocar esta linha é a única mudança necessária: domain e presentation não sabem
     * qual implementação está por trás da interface VehicleRepository.
     */
    private const val USE_REAL_VHAL = true

    @Provides
    @Singleton
    fun provideVehicleRepository(
        mock: Lazy<VehicleRepositoryImpl>,
        car: Lazy<CarVehicleRepositoryImpl>
    ): VehicleRepository = if (USE_REAL_VHAL) car.get() else mock.get()
}
