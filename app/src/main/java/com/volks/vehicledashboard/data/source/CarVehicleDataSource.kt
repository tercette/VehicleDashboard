package com.volks.vehicledashboard.data.source

import android.car.Car
import android.car.VehicleGear
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Fonte de dados REAL: lê propriedades do veículo do VHAL via [CarPropertyManager].
 * Produz o mesmo [VehicleDataDto] da fonte mockada — domain e presentation não mudam.
 *
 * Limites de app comum (sem chave da plataforma):
 * - PERF_VEHICLE_SPEED, FUEL_LEVEL, GEAR_SELECTION: permissões de runtime.
 * - ENGINE_RPM exige CAR_ENGINE_DETAILED (signature|privileged) — fica 0.
 */
class CarVehicleDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun isAvailable(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)

    fun observeRaw(): Flow<VehicleDataDto> = callbackFlow {
        val car = Car.createCar(context)
        val manager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager

        val capacity = readFloat(manager, VehiclePropertyIds.INFO_FUEL_CAPACITY)
        Log.i(TAG, "capacidade do tanque = $capacity L")

        // Estado inicial: lê o valor ATUAL de cada propriedade (o callback só avisa mudanças)
        var speedMs = readFloat(manager, VehiclePropertyIds.PERF_VEHICLE_SPEED)
        var fuelRatio = toRatio(readFloat(manager, VehiclePropertyIds.FUEL_LEVEL), capacity)
        var gearCode = toDtoGearCode(readInt(manager, VehiclePropertyIds.GEAR_SELECTION))

        fun emitCurrent() {
            trySend(
                VehicleDataDto(
                    speedMetersPerSecond = speedMs,
                    fuelRatio = fuelRatio,
                    engineRpm = 0,
                    gearCode = gearCode
                )
            )
        }

        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                when (value.propertyId) {
                    VehiclePropertyIds.PERF_VEHICLE_SPEED ->
                        speedMs = (value.value as? Float) ?: 0f

                    VehiclePropertyIds.FUEL_LEVEL ->
                        fuelRatio = toRatio((value.value as? Float) ?: 0f, capacity)

                    VehiclePropertyIds.GEAR_SELECTION ->
                        gearCode = toDtoGearCode((value.value as? Int) ?: 0)
                }
                emitCurrent()
            }

            override fun onErrorEvent(propertyId: Int, zone: Int) {
                Log.w(TAG, "erro na propriedade ${VehiclePropertyIds.toString(propertyId)}")
            }
        }

        // PERF_VEHICLE_SPEED é CONTINUOUS: precisa de taxa em Hz (ONCHANGE=0 não dispara).
        // FUEL_LEVEL e GEAR_SELECTION são ON_CHANGE: a taxa é ignorada.
        register(manager, callback, VehiclePropertyIds.PERF_VEHICLE_SPEED, CarPropertyManager.SENSOR_RATE_UI)
        register(manager, callback, VehiclePropertyIds.FUEL_LEVEL, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        register(manager, callback, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_ONCHANGE)

        emitCurrent()

        awaitClose {
            runCatching { manager.unregisterCallback(callback) }
            runCatching { car.disconnect() }
        }
    }

    /**
     * Exemplo de ESCRITA no VHAL: temperatura do ar-condicionado (HVAC é gravável por app
     * comum, via CONTROL_CAR_CLIMATE). Odômetro/reset de sensor são vendor e exigem app de sistema.
     */
    fun setHvacTemperature(celsius: Float) {
        try {
            val car = Car.createCar(context)
            val manager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
            manager.setFloatProperty(VehiclePropertyIds.HVAC_TEMPERATURE_SET, AREA_GLOBAL, celsius)
            car.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "falha ao escrever HVAC_TEMPERATURE_SET", e)
        }
    }

    private fun register(
        manager: CarPropertyManager,
        callback: CarPropertyManager.CarPropertyEventCallback,
        propertyId: Int,
        rate: Float
    ) {
        try {
            val ok = manager.registerCallback(callback, propertyId, rate)
            Log.i(TAG, "registro ${VehiclePropertyIds.toString(propertyId)} -> $ok")
        } catch (e: Exception) {
            Log.w(TAG, "falha ao registrar ${VehiclePropertyIds.toString(propertyId)}", e)
        }
    }

    private fun readFloat(manager: CarPropertyManager, propertyId: Int): Float =
        try {
            manager.getFloatProperty(propertyId, AREA_GLOBAL)
        } catch (e: Exception) {
            Log.w(TAG, "falha ao ler ${VehiclePropertyIds.toString(propertyId)}: ${e.message}")
            0f
        }

    private fun readInt(manager: CarPropertyManager, propertyId: Int): Int =
        try {
            manager.getIntProperty(propertyId, AREA_GLOBAL)
        } catch (e: Exception) {
            Log.w(TAG, "falha ao ler ${VehiclePropertyIds.toString(propertyId)}: ${e.message}")
            0
        }

    private fun toRatio(liters: Float, capacity: Float): Float =
        if (capacity > 0f) (liters / capacity) else 0f

    /** VehicleGear do AOSP -> convenção do nosso DTO (0=P, -1=R, -2=N, 1..6). */
    private fun toDtoGearCode(vehicleGear: Int): Int = when (vehicleGear) {
        VehicleGear.GEAR_PARK -> 0
        VehicleGear.GEAR_REVERSE -> -1
        VehicleGear.GEAR_NEUTRAL -> -2
        VehicleGear.GEAR_DRIVE, VehicleGear.GEAR_FIRST -> 1
        VehicleGear.GEAR_SECOND -> 2
        VehicleGear.GEAR_THIRD -> 3
        VehicleGear.GEAR_FOURTH -> 4
        VehicleGear.GEAR_FIFTH -> 5
        VehicleGear.GEAR_SIXTH -> 6
        else -> 0
    }

    private companion object {
        const val TAG = "CarVehicleDataSource"
        const val AREA_GLOBAL = 0
    }
}
