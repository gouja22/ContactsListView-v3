package com.example.contacts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel partagé entre tous les fragments via MainActivity.
 *
 * Il conserve la liste des contacts en mémoire et expose un LiveData
 * pour que les fragments puissent observer les changements (ex. après
 * modification dans DetailContactFragment → actualisation dans
 * ListeContactsFragment).
 */
class ContactsViewModel : ViewModel() {

    val contacts: MutableLiveData<MutableList<Contact>> = MutableLiveData(
        mutableListOf(
            Contact(0, "Ben Ali",  "Ahmed",   "+216 20 000 001", "ahmed.benali@email.tn",   latitude = 36.8065,  longitude = 10.1815),
            Contact(1, "Trabelsi","Sarra",   "+216 20 000 002", "sarra.trabelsi@email.tn",  latitude = 33.8869,  longitude = 9.5375),
            Contact(2, "Gharbi",  "Mohamed", "+216 20 000 003", "med.gharbi@email.tn",      latitude = 36.7538,  longitude = 10.2275),
            Contact(3, "Jaziri",  "Ines",    "+216 20 000 004", "ines.jaziri@email.tn",     latitude = 35.6757,  longitude = 10.0969),
            Contact(4, "Mansouri","Youssef", "+216 20 000 005", "youssef.mansouri@email.tn",latitude = 37.2744,  longitude = 9.8739),
            Contact(5, "Chaabane","Amira",   "+216 20 000 006", "amira.chaabane@email.tn",  latitude = 36.4610,  longitude = 10.7347),
            Contact(6, "Bouazizi","Karim",   "+216 20 000 007", "karim.bouazizi@email.tn",  latitude = 36.9922,  longitude = 8.7592),
            Contact(7, "Hammami", "Nour",    "+216 20 000 008", "nour.hammami@email.tn",    latitude = null,     longitude = null)
        )
    )

    /** Renvoie un contact par son ID, ou null s'il n'existe pas. */
    fun getById(id: Int): Contact? = contacts.value?.firstOrNull { it.id == id }

    /** Met à jour un contact existant et notifie les observateurs. */
    fun updateContact(updated: Contact) {
        val list = contacts.value ?: return
        val idx = list.indexOfFirst { it.id == updated.id }
        if (idx != -1) {
            list[idx] = updated
            contacts.value = list   // déclenche les observateurs LiveData
        }
    }
}
