package com.example.testfeatureofqiksafe.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.testfeatureofqiksafe.R
import com.example.testfeatureofqiksafe.data.repository.UserRepository
import com.example.testfeatureofqiksafe.databinding.FragmentHomeBinding
import com.example.testfeatureofqiksafe.ui.viewmodel.UserViewModel
import com.example.testfeatureofqiksafe.ui.viewmodel.UserViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding  = FragmentHomeBinding.inflate(inflater, container, false)

        binding.apply {
            emergencySettingCard.setOnClickListener {
                Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_emergencySettingFragment)
            }
            startEmergencyCard.setOnClickListener {
                Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_startEmergencyFragment)
            }
            emergencyContactCard.setOnClickListener {
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
                    .selectedItemId = R.id.contactFragment
            }
            recentAlertCard.setOnClickListener {
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
                    .selectedItemId = R.id.recentAlertFragment
            }
            userProfileCard.setOnClickListener {
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
                    .selectedItemId = R.id.userProfileFragment
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = UserRepository(FirebaseFirestore.getInstance())
        userViewModel = ViewModelProvider(this, UserViewModelFactory(repo))[UserViewModel::class.java]

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            binding.tvUserName.text = user?.name?.takeIf { it.isNotBlank() } ?: "User"
        }
    }

    override fun onStart() {
        super.onStart()
        userViewModel.startListeningToUserProfile()
    }

    override fun onStop() {
        super.onStop()
        userViewModel.stopListeningToUserProfile()
    }

}