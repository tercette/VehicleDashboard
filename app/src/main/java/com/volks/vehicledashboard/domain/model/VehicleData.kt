package com.volks.vehicledashboard.domain.model

data class VehicleData(
    val speedKmh: Int,
    val fuelPercent: Int,
    val rpm: Int,
    val gear: String
)
