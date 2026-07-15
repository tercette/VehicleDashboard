package com.volks.vehicledashboard.data.repository

import com.volks.vehicledashboard.data.mapper.VehicleMapper
import com.volks.vehicledashboard.data.source.CarVehicleDataSource
import com.volks.vehicledashboard.domain.model.VehicleData
import com.volks.vehicledashboard.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementação do mesmo contrato [VehicleRepository], mas lendo o VHAL real
 * ([CarVehicleDataSource]) em vez da fonte mockada. Domain e presentation não mudam.
 *
 * Os comandos são inertes: um app não aciona acelerador/freio/ignição de um veículo real —
 * essas propriedades não são graváveis por app comum. No modo VHAL os dados vêm do veículo
 * (ou da injeção de dados do emulador) e a tela é um espelho.
 */
class CarVehicleRepositoryImpl @Inject constructor(
    private val dataSource: CarVehicleDataSource,
    private val mapper: VehicleMapper
) : VehicleRepository {

    override fun observeVehicleData(): Flow<VehicleData> =
        dataSource.observeRaw().map { dto -> mapper.toDomain(dto) }

    override fun setAccelerator(pressed: Boolean) = Unit

    override fun setBrake(pressed: Boolean) = Unit

    override fun setIgnition(on: Boolean) = Unit

    override fun refuel() = Unit
}
