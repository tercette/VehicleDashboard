package com.volks.vehicledashboard.domain.usecase

import com.volks.vehicledashboard.domain.repository.VehicleRepository
import javax.inject.Inject

class ControlVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    fun accelerate(pressed: Boolean) = repository.setAccelerator(pressed)

    fun brake(pressed: Boolean) = repository.setBrake(pressed)

    fun setIgnition(on: Boolean) = repository.setIgnition(on)

    fun refuel() = repository.refuel()
}
