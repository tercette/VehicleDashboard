package com.volks.vehicledashboard.data.mapper

import com.volks.vehicledashboard.data.source.VehicleDataDto
import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleMapperTest {

    private val mapper = VehicleMapper()

    private fun dto(
        speedMps: Float = 0f,
        fuelRatio: Float = 0f,
        rpm: Int = 0,
        gearCode: Int = 0
    ) = VehicleDataDto(
        speedMetersPerSecond = speedMps,
        fuelRatio = fuelRatio,
        engineRpm = rpm,
        gearCode = gearCode
    )

    @Test
    fun toDomain_speedInMetersPerSecond_convertsToKmh() {
        val result = mapper.toDomain(dto(speedMps = 10f))
        assertEquals(36, result.speedKmh)
    }

    @Test
    fun toDomain_fuelRatio_convertsToPercent() {
        val result = mapper.toDomain(dto(fuelRatio = 0.85f))
        assertEquals(85, result.fuelPercent)
    }

    @Test
    fun toDomain_fuelRatioAboveOne_isClampedTo100() {
        val result = mapper.toDomain(dto(fuelRatio = 1.5f))
        assertEquals(100, result.fuelPercent)
    }

    @Test
    fun toDomain_negativeFuelRatio_isClampedTo0() {
        val result = mapper.toDomain(dto(fuelRatio = -0.3f))
        assertEquals(0, result.fuelPercent)
    }

    @Test
    fun toDomain_rpm_isPassedThroughUnchanged() {
        val result = mapper.toDomain(dto(rpm = 2450))
        assertEquals(2450, result.rpm)
    }

    @Test
    fun toDomain_gearCodes_mapToLabels() {
        assertEquals("P", mapper.toDomain(dto(gearCode = 0)).gear)
        assertEquals("R", mapper.toDomain(dto(gearCode = -1)).gear)
        assertEquals("N", mapper.toDomain(dto(gearCode = -2)).gear)
        assertEquals("3", mapper.toDomain(dto(gearCode = 3)).gear)
    }

    @Test
    fun toDomain_unknownGearCode_fallsBackToDash() {
        val result = mapper.toDomain(dto(gearCode = 99))
        assertEquals("-", result.gear)
    }
}
