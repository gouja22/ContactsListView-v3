package com.example.contacts

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adaptateur RecyclerView pour afficher la liste (éventuellement filtrée) des contacts.
 *
 * Points clés :
 * - Utilise une liste "affichée" (filteredList) distincte de la liste source.
 * - Le clic renvoie l'ID du contact (et non sa position) pour éviter les erreurs d'indice.
 * - updateList() remplace les données et notifie l'adaptateur.
 */
class ContactsAdapter(
    private var filteredList: MutableList<Contact> = mutableListOf(),
    private val onItemClick: (contactId: Int) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    // --- ViewHolder ---
    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPhoto:    ImageView = view.findViewById(R.id.imgPhoto)
        val tvNomPrenom: TextView  = view.findViewById(R.id.tvNomPrenom)
        val tvTelephone: TextView  = view.findViewById(R.id.tvTelephone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = filteredList[position]

        holder.tvNomPrenom.text = "${contact.prenom} ${contact.nom}"
        holder.tvTelephone.text = contact.telephone

        // Photo
        if (contact.photoUri != null) {
            try {
                holder.imgPhoto.setImageURI(Uri.parse(contact.photoUri))
            } catch (e: Exception) {
                holder.imgPhoto.setImageResource(R.drawable.ic_person_placeholder)
            }
        } else {
            holder.imgPhoto.setImageResource(R.drawable.ic_person_placeholder)
        }

        // Clic → on remonte l'ID, pas la position
        holder.itemView.setOnClickListener { onItemClick(contact.id) }
    }

    override fun getItemCount(): Int = filteredList.size

    /** Remplace la liste affichée et rafraîchit le RecyclerView. */
    fun updateList(newList: List<Contact>) {
        filteredList = newList.toMutableList()
        notifyDataSetChanged()
    }
}
