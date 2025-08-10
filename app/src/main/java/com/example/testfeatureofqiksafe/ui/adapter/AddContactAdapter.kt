package com.example.testfeatureofqiksafe.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testfeatureofqiksafe.R
import com.example.testfeatureofqiksafe.data.model.SelectableContact
import de.hdodenhof.circleimageview.CircleImageView
import com.squareup.picasso.Picasso

class AddContactAdapter(
    private var contacts: List<SelectableContact>,
    private val onSelectionChanged: (SelectableContact) -> Unit
) : RecyclerView.Adapter<AddContactAdapter.AddContactViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    fun updateList(newContacts: List<SelectableContact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }

    inner class AddContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: CircleImageView = itemView.findViewById(R.id.profile_image)
        val tvName: TextView = itemView.findViewById(R.id.contact_name)
        val tvNumber: TextView = itemView.findViewById(R.id.contact_number)
        val radioButton: RadioButton = itemView.findViewById(R.id.select_radio_button)

        init {
            itemView.setOnClickListener {
                selectItem(adapterPosition)
            }
            radioButton.setOnClickListener {
                selectItem(adapterPosition)
            }
        }
    }

    private fun selectItem(position: Int) {
        if (position == RecyclerView.NO_POSITION) return
        if (selectedPosition == position) return  // already selected

        val previousPosition = selectedPosition
        selectedPosition = position

        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)

        onSelectionChanged(contacts[selectedPosition])
    }

    fun getSelectedContact(): SelectableContact? =
        if (selectedPosition != RecyclerView.NO_POSITION) contacts[selectedPosition] else null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_contact, parent, false)
        return AddContactViewHolder(view)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: AddContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = contact.name
        holder.tvNumber.text = contact.phone
        holder.radioButton.isChecked = position == selectedPosition

        if (!contact.photoUri.isNullOrEmpty()) {
            Picasso.get()
                .load(contact.photoUri)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.ivProfile)
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }
}
