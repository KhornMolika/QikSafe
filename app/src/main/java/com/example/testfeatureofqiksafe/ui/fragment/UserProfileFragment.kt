package com.example.testfeatureofqiksafe.ui.fragment

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.example.testfeatureofqiksafe.R
import com.example.testfeatureofqiksafe.data.repository.UserRepository
import com.example.testfeatureofqiksafe.databinding.FragmentUserProfileBinding
import com.example.testfeatureofqiksafe.ui.activity.LoginActivity
import com.example.testfeatureofqiksafe.ui.viewmodel.AuthViewModel
import com.example.testfeatureofqiksafe.ui.viewmodel.UserViewModel
import com.example.testfeatureofqiksafe.ui.viewmodel.UserViewModelFactory
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel by lazy { AuthViewModel() }

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(FirebaseFirestore.getInstance()))
    }

    // Activity Result API for location permission
    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            enableGpsSharing()
            binding.switchGps.isChecked = true
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            binding.switchGps.isChecked = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Live user name
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            binding.tvUserName.text =
                user?.name?.takeIf { it.isNotBlank() } ?: getString(R.string.default_user_name)
        }

        // Buttons
        binding.helpCenterBtn.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_userProfileFragment_to_helpCenterFragment)
        }
        binding.editProfileBtn.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_userProfileFragment_to_editProfileFragment)
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            authViewModel.logout(requireContext())
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }

        // GPS switch
        setupGpsSwitch(binding.switchGps)
    }

    override fun onStart() {
        super.onStart()
        // start realtime user profile listener
        userViewModel.startListeningToUserProfile()
    }

    override fun onStop() {
        super.onStop()
        // stop listener to avoid leaks
        userViewModel.stopListeningToUserProfile()
    }

    private fun setupGpsSwitch(switch: MaterialSwitch) {
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val granted = ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (granted) {
                    enableGpsSharing()
                } else {
                    requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                disableGpsSharing()
            }
        }
    }

    private fun enableGpsSharing() {
        // TODO: start location updates or background service
        Toast.makeText(requireContext(), "GPS sharing enabled", Toast.LENGTH_SHORT).show()
    }

    private fun disableGpsSharing() {
        // TODO: stop location updates or background service
        Toast.makeText(requireContext(), "GPS sharing disabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
