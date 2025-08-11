package com.example.testfeatureofqiksafe.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testfeatureofqiksafe.data.model.Contact
import com.example.testfeatureofqiksafe.data.model.SelectableContact
import com.example.testfeatureofqiksafe.data.repository.ContactRepository
import com.example.testfeatureofqiksafe.data.repository.UserRepository
import com.example.testfeatureofqiksafe.databinding.FragmentAddContactBinding
import com.example.testfeatureofqiksafe.ui.adapter.contact.AddContactAdapter
import com.example.testfeatureofqiksafe.ui.viewmodel.ContactViewModelFactory
import com.example.testfeatureofqiksafe.ui.viewmodel.UserViewModel
import com.example.testfeatureofqiksafe.ui.viewmodel.UserViewModelFactory
import com.example.testfeatureofqiksafe.viewmodel.ContactViewModel
import com.google.firebase.firestore.FirebaseFirestore

class AddContactFragment : Fragment() {

    // Initialize repositories and viewmodels (in fragment, or inject)
    private val userRepository = UserRepository(FirebaseFirestore.getInstance())
    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this, UserViewModelFactory(userRepository))[UserViewModel::class.java]
    }

    private var _binding: FragmentAddContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var contactViewModel: ContactViewModel
    private lateinit var addContactAdapter: AddContactAdapter

    private var deviceContacts: List<SelectableContact> = emptyList()
    private var selectedContact: SelectableContact? = null

    private val REQUEST_READ_CONTACTS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = ContactRepository(FirebaseFirestore.getInstance())
        val factory = ContactViewModelFactory(repository)
        contactViewModel = ViewModelProvider(this, factory)[ContactViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        observeDeviceContacts()
        checkAndRequestContactsPermission()
        setupDoneButton()
    }

    private fun setupRecyclerView() {
        addContactAdapter = AddContactAdapter(emptyList()) { contact ->
            selectedContact = contact
        }
        binding.addContactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = addContactAdapter
        }
    }

    private fun observeDeviceContacts() {
        contactViewModel.deviceContacts.observe(viewLifecycleOwner) { contacts ->
            deviceContacts = contacts
            addContactAdapter.updateList(contacts)
        }
        contactViewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun setupDoneButton() {
        binding.doneButton.setOnClickListener {
            val selected = addContactAdapter.getSelectedContact()
            if (selected == null) {
                Toast.makeText(requireContext(), "Please select a contact", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Use Firebase Auth UID for rules compatibility
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Toast.makeText(requireContext(), "Not signed in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val contactToAdd = Contact(
                contactId = selected.contactId,
                userId = uid,
                name = selected.name,
                phone = selected.phone,
                photoUri = selected.photoUri,
                type = "",
                lastMessage = "",
                unreadCount = 0
            )

            // Atomic add-if-not-exists
            contactViewModel.addContactIfNotDuplicate(uid, contactToAdd) { ok, duplicate, err ->
                when {
                    duplicate -> {
                        Toast.makeText(requireContext(), "You already have this contact.", Toast.LENGTH_SHORT).show()
                    }
                    ok -> {
                        Toast.makeText(requireContext(), "${selected.name} added.", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Failed to add: ${err ?: "unknown error"}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun checkAndRequestContactsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission granted → load contacts
            contactViewModel.loadDeviceContacts(requireContext())
        } else {
            // Request permission
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                contactViewModel.loadDeviceContacts(requireContext())
            } else {
                Toast.makeText(requireContext(), "Permission denied to read contacts", Toast.LENGTH_SHORT).show()
                // Optionally disable UI or navigate back
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
