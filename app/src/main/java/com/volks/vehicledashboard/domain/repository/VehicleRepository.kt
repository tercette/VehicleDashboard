package com.volks.vehicledashboard.domain.repository

import com.volks.vehicledashboard.domain.model.VehicleData
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {

    fun observeVehicleData(): Flow<VehicleData>

    fun setAccelerator(pressed: Boolean)

    fun setBrake(pressed: Boolean)

    fun setIgnition(on: Boolean)

    fun refuel()
}
