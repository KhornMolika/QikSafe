package com.example.testfeatureofqiksafe.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testfeatureofqiksafe.R
import com.example.testfeatureofqiksafe.data.model.Contact
import de.hdodenhof.circleimageview.CircleImageView

class ContactAdapter(
    private val onContactClick: (Contact) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        Log.d("ContactAdapter", "onBindViewHolder position=$position contact=$contact")
        holder.bind(contact)
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profile_image)
        private val contactName: TextView = itemView.findViewById(R.id.contact_name)
        private val lastMessage: TextView = itemView.findViewById(R.id.last_message)
        private val unreadBadge: TextView = itemView.findViewById(R.id.unread_badge)


        fun bind(contact: Contact) {
            Log.d("ContactAdapter", "Binding contact: $contact")

            contactName.text = contact.name
            lastMessage.text = contact.lastMessage

            if (contact.unreadCount > 0) {
                unreadBadge.visibility = View.VISIBLE
                unreadBadge.text = contact.unreadCount.toString()
            } else {
                unreadBadge.visibility = View.GONE
            }

            val photoToLoad = if (!contact.photoUri.isNullOrEmpty()) contact.photoUri else R.drawable.ic_person
            Glide.with(itemView.context)
                .load(photoToLoad)
                .placeholder(R.drawable.ic_person)
                .into(profileImage)

            itemView.setOnClickListener { onContactClick(contact) }
        }



    }

    class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact) =
            oldItem.contactId == newItem.contactId

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact) =
            oldItem == newItem
    }
}
