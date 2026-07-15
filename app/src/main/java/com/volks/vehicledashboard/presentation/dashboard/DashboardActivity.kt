package com.volks.vehicledashboard.presentation.dashboard

import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.volks.vehicledashboard.R
import com.volks.vehicledashboard.databinding.ActivityDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    private val viewModel: DashboardViewModel by viewModels()

    // Strings literais (e não android.car.Car.PERMISSION_*) pra não carregar a car lib num celular
    private val carPermissions = arrayOf(
        "android.car.permission.CAR_INFO",
        "android.car.permission.CAR_SPEED",
        "android.car.permission.CAR_ENERGY",
        "android.car.permission.CAR_POWERTRAIN"
    )

    private val carPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            // Sem permissão o VHAL apenas não emite; a tela segue com o que tiver.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestCarPermissionsIfNeeded()
        configureGauges()
        setupControls()
        observeState()
    }

    /** Permissões do VHAL só existem no AAOS; num celular a checagem é ignorada. */
    private fun requestCarPermissionsIfNeeded() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) return
        val missing = carPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) carPermissionLauncher.launch(missing.toTypedArray())
    }

    private fun configureGauges() {
        binding.speedometer.configure(
            min = 0f, max = 240f, majorStep = 20f,
            accentColor = ContextCompat.getColor(this, R.color.gauge_speed_accent),
            label = "", unit = "km/h"
        )
        binding.tachometer.configure(
            min = 0f, max = 7000f, majorStep = 1000f,
            accentColor = ContextCompat.getColor(this, R.color.gauge_rpm_accent),
            label = "", unit = "RPM",
            tickLabelDivisor = 1000f,
            redlineStart = 5500f
        )
    }

    private fun setupControls() {
        binding.pedalAccelerator.configure(
            accentColor = ContextCompat.getColor(this, R.color.pedal_accelerator_pressed),
            label = ""
        )
        binding.pedalAccelerator.onPressedChange = { pressed -> viewModel.onAccelerator(pressed) }

        binding.pedalBrake.configure(
            accentColor = ContextCompat.getColor(this, R.color.pedal_brake_pressed),
            label = ""
        )
        binding.pedalBrake.onPressedChange = { pressed -> viewModel.onBrake(pressed) }

        binding.btnIgnition.setOnClickListener { viewModel.toggleIgnition() }
        binding.btnRefuel.setOnClickListener { viewModel.onRefuel() }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { render(it) } }
                launch { viewModel.ignitionOn.collect { updateIgnitionButton(it) } }
            }
        }
    }

    private fun render(state: DashboardUiState) {
        when (state) {
            is DashboardUiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
            }

            is DashboardUiState.Content -> {
                binding.progressBar.visibility = View.GONE
                val vehicle = state.vehicle
                binding.speedometer.setValue(vehicle.speedKmh.toFloat())
                binding.tachometer.setValue(vehicle.rpm.toFloat())
                binding.fuelBar.setLevel(vehicle.fuelPercent.toFloat())
                binding.gearIndicator.setGear(vehicle.gear)
                binding.roadView.setSpeed(vehicle.speedKmh.toFloat())
            }

            is DashboardUiState.Error -> {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateIgnitionButton(on: Boolean) {
        binding.btnIgnition.text =
            getString(if (on) R.string.ignition_stop else R.string.ignition_start)
        val colorRes = if (on) R.color.ignition_off else R.color.pedal_accelerator
        binding.btnIgnition.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, colorRes))
    }
}
