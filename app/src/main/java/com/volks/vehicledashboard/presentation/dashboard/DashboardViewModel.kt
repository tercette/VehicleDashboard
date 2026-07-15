package com.volks.vehicledashboard.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.volks.vehicledashboard.domain.usecase.ControlVehicleUseCase
import com.volks.vehicledashboard.domain.usecase.ObserveVehicleDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeVehicleData: ObserveVehicleDataUseCase,
    private val controlVehicle: ControlVehicleUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        observeVehicleData()
            .map<_, DashboardUiState> { vehicle -> DashboardUiState.Content(vehicle) }
            .catch { throwable ->
                emit(DashboardUiState.Error(throwable.message ?: "Erro ao ler dados do veículo"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DashboardUiState.Loading
            )

    fun onAccelerator(pressed: Boolean) = controlVehicle.accelerate(pressed)

    fun onBrake(pressed: Boolean) = controlVehicle.brake(pressed)

    private val _ignitionOn = MutableStateFlow(false)
    val ignitionOn: StateFlow<Boolean> = _ignitionOn

    fun toggleIgnition() {
        val next = !_ignitionOn.value
        _ignitionOn.value = next
        controlVehicle.setIgnition(next)
    }

    fun onRefuel() = controlVehicle.refuel()
}
