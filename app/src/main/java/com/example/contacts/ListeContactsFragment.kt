package com.example.contacts

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * ListeContactsFragment — V3
 *
 * Affiche la liste complète des contacts dans une RecyclerView avec un champ
 * de recherche qui filtre en temps réel sur nom, prénom et téléphone.
 *
 * Communication avec DetailContactFragment :
 *   - L'ID du contact sélectionné est transmis par Bundle.
 *   - Le ViewModel partagé sert à observer les mises à jour après modification.
 */
class ListeContactsFragment : Fragment() {

    private val viewModel: ContactsViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var etRecherche:  EditText
    private lateinit var tvAucunResultat: TextView
    private lateinit var adapter: ContactsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_liste_contacts, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView     = view.findViewById(R.id.recyclerViewContacts)
        etRecherche      = view.findViewById(R.id.etRecherche)
        tvAucunResultat  = view.findViewById(R.id.tvAucunResultat)

        // Initialisation de l'adaptateur
        requireActivity().title = getString(R.string.app_name)

        // Initialisation de l'adaptateur : le clic renvoie l'ID (pas la position)
        adapter = ContactsAdapter { contactId ->
            // Ouvrir le fragment Détail en passant l'ID du contact par Bundle
            (requireActivity() as MainActivity)
                .navigateTo(DetailContactFragment.newInstance(contactId))
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observer les changements de la liste (ex. après modification)
        viewModel.contacts.observe(viewLifecycleOwner) { list ->
            appliquerFiltre(etRecherche.text.toString(), list)
        }

        // Filtrage en temps réel
        etRecherche.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                appliquerFiltre(s.toString(), viewModel.contacts.value ?: emptyList())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     * Filtre la liste selon la requête (insensible à la casse).
     * Porte sur : nom, prénom, téléphone.
     */
    private fun appliquerFiltre(query: String, source: List<Contact>) {
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) {
            source.toList()
        } else {
            source.filter {
                it.nom.lowercase().contains(q) ||
                it.prenom.lowercase().contains(q) ||
                it.telephone.replace(" ", "").contains(q.replace(" ", ""))
            }
        }

        adapter.updateList(filtered)

        // Message si aucun résultat
        tvAucunResultat.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility    = if (filtered.isEmpty()) View.GONE   else View.VISIBLE
    }
}
