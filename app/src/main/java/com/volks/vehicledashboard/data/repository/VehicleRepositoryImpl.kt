package com.volks.vehicledashboard.data.repository

import com.volks.vehicledashboard.data.mapper.VehicleMapper
import com.volks.vehicledashboard.data.source.FakeVehicleDataSource
import com.volks.vehicledashboard.domain.model.VehicleData
import com.volks.vehicledashboard.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val dataSource: FakeVehicleDataSource,
    private val mapper: VehicleMapper
) : VehicleRepository {

    override fun observeVehicleData(): Flow<VehicleData> =
        dataSource.observeRaw().map { dto -> mapper.toDomain(dto) }

    override fun setAccelerator(pressed: Boolean) = dataSource.setAccelerator(pressed)

    override fun setBrake(pressed: Boolean) = dataSource.setBrake(pressed)

    override fun setIgnition(on: Boolean) = dataSource.setIgnition(on)

    override fun refuel() = dataSource.refuel()
}
