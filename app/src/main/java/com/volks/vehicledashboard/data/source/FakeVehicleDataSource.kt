package com.volks.vehicledashboard.data.source

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class FakeVehicleDataSource @Inject constructor() {

    // @Volatile: escritos pela thread da UI, lidos pela coroutine da simulação
    @Volatile private var ignitionOn = false
    @Volatile private var accelerating = false
    @Volatile private var braking = false
    // refuel é check-then-act (ler + resetar): precisa de atomicidade, não só visibilidade
    private val refuelRequested = AtomicBoolean(false)

    fun setIgnition(on: Boolean) { ignitionOn = on }

    fun refuel() { refuelRequested.set(true) }

    fun setAccelerator(pressed: Boolean) { accelerating = pressed }

    fun setBrake(pressed: Boolean) { braking = pressed }

    fun observeRaw(): Flow<VehicleDataDto> = flow {
        var speedMs = 0f
        var fuelRatio = 0.85f
        var gearCode = 0

        while (true) {
            if (refuelRequested.getAndSet(false)) {
                fuelRatio = 1.0f
            }
            val hasFuel = fuelRatio > 0f
            // motor só roda com ignição ligada E combustível
            val engineRunning = ignitionOn && hasFuel

            speedMs = when {
                braking -> speedMs - 2.8f
                accelerating && engineRunning -> speedMs + 1.0f
                else -> speedMs - 0.3f
            }.coerceIn(0f, 50f)

            gearCode = when {
                speedMs < 0.5f  -> 0
                speedMs < 8f    -> 1
                speedMs < 16f   -> 2
                speedMs < 24f   -> 3
                speedMs < 34f   -> 4
                speedMs < 44f   -> 5
                else            -> 6
            }

            val engineRpm = if (engineRunning) {
                val gearDivisor = if (gearCode > 0) gearCode else 1
                (800 + (speedMs * 320f) / gearDivisor).toInt().coerceIn(700, 6500)
            } else {
                0
            }

            if (engineRunning) {
                fuelRatio = (fuelRatio - engineRpm * 0.0000004f).coerceIn(0f, 1f)
            }

            emit(
                VehicleDataDto(
                    speedMetersPerSecond = speedMs,
                    fuelRatio = fuelRatio,
                    engineRpm = engineRpm,
                    gearCode = gearCode
                )
            )

            delay(200) // suspende sem bloquear a thread
        }
    }
}
