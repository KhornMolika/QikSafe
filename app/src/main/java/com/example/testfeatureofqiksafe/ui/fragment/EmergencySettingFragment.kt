package com.example.testfeatureofqiksafe.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.testfeatureofqiksafe.databinding.FragmentEmergencySettingBinding
import com.example.testfeatureofqiksafe.data.repository.SettingsRepository
import com.example.testfeatureofqiksafe.ui.viewmodel.SettingsViewModel
import com.example.testfeatureofqiksafe.ui.viewmodel.SettingsViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class EmergencySettingFragment : Fragment() {

    private var _binding: FragmentEmergencySettingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository(FirebaseFirestore.getInstance()))
    }

    private var suppressSwitchCallback = false

    // Foreground location (fine+coarse)
    private val requestForegroundLocation = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) onForegroundLocationGranted() else onLocationDenied()
    }

    // Optional background location (only auto-request on Android 10; guide to settings on 11+)
    private val requestBackgroundLocation = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Toast.makeText(
            requireContext(),
            if (granted) "Background access granted" else "Background access denied",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmergencySettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        // Observe settings -> populate UI
        viewModel.settings.observe(viewLifecycleOwner) { s ->
            s?.let {
                if (binding.backgroundAccessSwitch.isChecked != it.locationSharing) {
                    suppressSwitchCallback = true
                    binding.backgroundAccessSwitch.isChecked = it.locationSharing
                    suppressSwitchCallback = false
                }
                when (it.triggerMethod) {
                    "shake" -> binding.shakePhoneRadio.isChecked = true
                    "power" -> binding.tripleClickRadio.isChecked = true
                }
                when (it.actionPreference) {
                    "send_location_call" -> binding.sendLocationCallRadio.isChecked = true
                    "send_location_only" -> binding.sendLocationOnlyRadio.isChecked = true
                    "call_only" -> binding.callOnlyRadio.isChecked = true
                }
                if (binding.emergencyNumberEditText.text?.toString() != it.emergencyNumber) {
                    binding.emergencyNumberEditText.setText(it.emergencyNumber)
                }
            }
        }

        // Switch: toggle sharing + persist
        binding.backgroundAccessSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (suppressSwitchCallback) return@setOnCheckedChangeListener
            if (isChecked) ensureForegroundLocation() else disableLocationSharing()
            viewModel.saveLocationSharing(isChecked)
        }

        // Save aggregates all fields
        binding.btnSave.setOnClickListener {
            val trigger = selectedTrigger()
            val action = selectedAction()
            val number = binding.emergencyNumberEditText.text?.toString()?.trim().orEmpty()

            // Require number only if these actions are selected
            if (action == "send_location_call" || action == "call_only") {
                if (number.isEmpty()) {
                    binding.emergencyNumberEditText.error = "Required"
                    return@setOnClickListener
                }
            }

            saveTriggerPreference(trigger)
            saveActionPreference(action)
            saveEmergencyNumber(number)

            Snackbar.make(requireView(), "Settings saved", Snackbar.LENGTH_SHORT).show()
        }

    }

    // === Save helpers ===
    private fun saveTriggerPreference(method: String) = viewModel.saveTriggerPreference(method)
    private fun saveActionPreference(pref: String) = viewModel.saveActionPreference(pref)
    private fun saveEmergencyNumber(number: String) = viewModel.saveEmergencyNumber(number)

    private fun selectedTrigger(): String =
        if (binding.shakePhoneRadio.isChecked) "shake" else "power"

    private fun selectedAction(): String =
        when {
            binding.sendLocationCallRadio.isChecked -> "send_location_call"
            binding.sendLocationOnlyRadio.isChecked -> "send_location_only"
            else -> "call_only"
        }

    private fun ensureForegroundLocation() {
        val ctx = requireContext()
        val hasFine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            onForegroundLocationGranted()
        } else {
            requestForegroundLocation.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun onForegroundLocationGranted() {
        // Background location: auto-request only on Android 10 (Q); on 11+ guide users to Settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // If you truly require background updates, open settings:
            // openAppLocationSettings()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val hasBg = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasBg) requestBackgroundLocation.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        enableLocationSharing()

        // Warn if only approximate granted
        if (!isPreciseLocationEnabled() && hasCoarseLocation()) {
            showApproximateOnlyWarning()
        }
    }

    private fun onLocationDenied() {
        Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
        suppressSwitchCallback = true
        binding.backgroundAccessSwitch.isChecked = false
        suppressSwitchCallback = false
        viewModel.saveLocationSharing(false)
    }

    private fun enableLocationSharing() {
        // TODO: start your location updates / service
        Toast.makeText(requireContext(), "Location sharing enabled", Toast.LENGTH_SHORT).show()
    }

    private fun disableLocationSharing() {
        // TODO: stop your location updates / service
        Toast.makeText(requireContext(), "Location sharing disabled", Toast.LENGTH_SHORT).show()
    }

    private fun hasFineLocation(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasCoarseLocation(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun isPreciseLocationEnabled(): Boolean = hasFineLocation()

    private fun showApproximateOnlyWarning() {
        Snackbar.make(
            requireView(),
            "You’re sharing approximate location. Enable precise for better accuracy.",
            Snackbar.LENGTH_LONG
        ).setAction("Settings") {
            openAppLocationSettings()
        }.show()
    }

    private fun openAppLocationSettings() {
        val intent = Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        viewModel.startListening()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
