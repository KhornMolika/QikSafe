package com.example.testfeatureofqiksafe.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testfeatureofqiksafe.R
import com.example.testfeatureofqiksafe.data.model.Contact
import com.example.testfeatureofqiksafe.data.repository.ContactRepository
import com.example.testfeatureofqiksafe.ui.adapter.ContactAdapter
import com.example.testfeatureofqiksafe.ui.viewmodel.ContactViewModelFactory
import com.example.testfeatureofqiksafe.util.SharedPrefHelper
import com.example.testfeatureofqiksafe.viewmodel.ContactViewModel
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
class ContactFragment : Fragment() {

//    val userId = SharedPrefHelper.getUserId(requireContext()) ?: ""
    val repository = ContactRepository(FirebaseFirestore.getInstance())
    val factory = ContactViewModelFactory(repository)
    val viewModel: ContactViewModel by viewModels { factory }

    private lateinit var adapter: ContactAdapter
    private lateinit var searchInput: EditText
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var addContactCard: MaterialCardView

    private var allContacts = listOf<Contact>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchInput = view.findViewById(R.id.search_input)
        contactsRecyclerView = view.findViewById(R.id.contacts_recycler_view)
        addContactCard = view.findViewById(R.id.add_contact_card)


        setupRecyclerView()
        setupSearch()
        observeViewModel()

        // Example: Replace with your logged-in user's ID from SharedPrefHelper

        val userId = SharedPrefHelper.getUserId(requireContext()) ?: ""
        viewModel.startListening(userId)

        addContactCard.setOnClickListener {
            // Navigate to Add Contact screen or open dialog
            findNavController().navigate(R.id.action_contactFragment_to_addContactFragment)
        }
    }


    private fun setupRecyclerView() {
        adapter = ContactAdapter { contact ->
            // Handle click (navigate to chat or contact details)
//            val action = ContactFragmentDirections.actionContactFragmentToChatFragment(contact.userId, contact.name)
//            findNavController().navigate(action)
        }
        contactsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        contactsRecyclerView.adapter = adapter

    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterContacts(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterContacts(query: String) {
        val filtered = if (query.isEmpty()) {
            allContacts
        } else {
            allContacts.filter { it.name.contains(query, ignoreCase = true) }
        }
        adapter.submitList(filtered)
    }

    private fun observeViewModel() {
        viewModel.contacts.observe(viewLifecycleOwner) { contactList ->
            allContacts = contactList
            Log.d("TestingDemo", "Contacts received: $allContacts")
            filterContacts(searchInput.text.toString())
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                // Show error to user (Toast/Snackbar)
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopListening()
    }
}
