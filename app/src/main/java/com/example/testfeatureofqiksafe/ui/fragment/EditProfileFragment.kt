package com.example.testfeatureofqiksafe.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.testfeatureofqiksafe.data.repository.UserRepository
import com.example.testfeatureofqiksafe.databinding.FragmentEditProfileBinding
import com.example.testfeatureofqiksafe.ui.viewmodel.AuthViewModel
import com.example.testfeatureofqiksafe.ui.viewmodel.UserViewModel
import com.example.testfeatureofqiksafe.ui.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(FirebaseFirestore.getInstance()))
    }
    private val authViewModel by lazy { AuthViewModel() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Prefill when profile loads
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                if (binding.editName.text.isNullOrBlank())  binding.editName.setText(it.name)
                if (binding.editPhone.text.isNullOrBlank()) binding.editPhone.setText(it.phone)
                if (binding.editEmail.text.isNullOrBlank()) binding.editEmail.setText(it.email)
            }
        }
        // Start a one-time fetch or use your live listener; here we fetch once
        userViewModel.startListeningToUserProfile()

        // Optional simple validation feedback
        binding.editEmail.doAfterTextChanged {
            if (!it.isNullOrEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                binding.editEmail.error = "Invalid email"
            }
        }

        binding.btnSave.setOnClickListener { onSaveProfile() }
        binding.btnResetPassword.setOnClickListener { onResetPassword() }
    }

    private fun onSaveProfile() {
        val name  = binding.editName.text?.toString()?.trim().orEmpty()
        val phone = binding.editPhone.text?.toString()?.trim().orEmpty()
        val email = binding.editEmail.text?.toString()?.trim().orEmpty()

        // Basic validation
        if (name.isEmpty()) { binding.editName.error = "Required"; return }
        if (phone.isEmpty()) { binding.editPhone.error = "Required"; return }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.error = "Valid email required"; return
        }

        // Update Firestore profile fields
        val updates = mapOf(
            "name" to name,
            "phone" to phone,
            "email" to email
        )
        userViewModel.updateUserProfile(updates)

        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun onResetPassword() {
        // Prefer to send reset to the current Auth email (source of truth)
        val authEmail = FirebaseAuth.getInstance().currentUser?.email
        val targetEmail = when {
            !authEmail.isNullOrBlank() -> authEmail
            !binding.editEmail.text.isNullOrBlank() -> binding.editEmail.text.toString()
            else -> null
        }

        if (targetEmail.isNullOrBlank()) {
            Toast.makeText(requireContext(), "No email available for reset", Toast.LENGTH_SHORT).show()
            return
        }

        authViewModel.sendPasswordReset(targetEmail) { ok, err ->
            if (ok) {
                Toast.makeText(requireContext(), "Reset email sent to $targetEmail", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), err ?: "Failed to send reset email", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
