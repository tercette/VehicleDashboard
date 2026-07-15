package com.volks.vehicledashboard.domain.usecase

import com.volks.vehicledashboard.domain.model.VehicleData
import com.volks.vehicledashboard.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveVehicleDataUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    operator fun invoke(): Flow<VehicleData> = repository.observeVehicleData()
}
