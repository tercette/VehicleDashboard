package com.volks.vehicledashboard.data.mapper

import com.volks.vehicledashboard.data.source.VehicleDataDto
import com.volks.vehicledashboard.domain.model.VehicleData
import javax.inject.Inject
import kotlin.math.roundToInt

class VehicleMapper @Inject constructor() {

    fun toDomain(dto: VehicleDataDto): VehicleData = VehicleData(
        speedKmh = (dto.speedMetersPerSecond * 3.6f).roundToInt(),
        fuelPercent = (dto.fuelRatio * 100f).roundToInt().coerceIn(0, 100),
        rpm = dto.engineRpm,
        gear = mapGear(dto.gearCode)
    )

    private fun mapGear(gearCode: Int): String = when (gearCode) {
        0 -> "P"
        -1 -> "R"
        -2 -> "N"
        in 1..6 -> gearCode.toString()
        else -> "-"
    }
}
