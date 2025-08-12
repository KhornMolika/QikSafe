package com.example.testfeatureofqiksafe.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        binding.helpCenterBtn.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_userProfileFragment_to_helpCenterFragment)
        }
        binding.editProfileBtn.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_userProfileFragment_to_editProfileFragment)
        }
        binding.btnContact.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_userProfileFragment_to_contactFragment)
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout(requireContext())
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

