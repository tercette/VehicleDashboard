package com.volks.vehicledashboard.presentation.dashboard

import com.volks.vehicledashboard.domain.model.VehicleData

sealed interface DashboardUiState {

    data object Loading : DashboardUiState

    data class Content(val vehicle: VehicleData) : DashboardUiState

    data class Error(val message: String) : DashboardUiState
}
