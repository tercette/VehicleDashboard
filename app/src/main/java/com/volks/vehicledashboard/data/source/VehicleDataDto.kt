package com.volks.vehicledashboard.data.source

data class VehicleDataDto(
    val speedMetersPerSecond: Float,
    val fuelRatio: Float,
    val engineRpm: Int,
    // 0=P, -1=R, -2=N, 1..6 = marchas à frente
    val gearCode: Int
)
