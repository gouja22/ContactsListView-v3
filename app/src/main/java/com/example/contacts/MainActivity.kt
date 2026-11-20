package com.example.contacts

import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * MainActivity — V3
 *
 * Hôte unique de tous les fragments de l'application.
 * Elle ne contient pas de logique métier : elle se contente d'héberger
 * le FragmentContainerView et de lancer le premier fragment au démarrage.
 *
 * Le ViewModel partagé (ContactsViewModel) est instancié ici par viewModels()
 * et partagé avec les fragments via activityViewModels().
 */
class MainActivity : AppCompatActivity() {

    // ViewModel partagé — accessible par tous les fragments via activityViewModels()
    val viewModel: ContactsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Charger ListeContactsFragment uniquement lors du premier démarrage
        // (savedInstanceState != null = rotation ou recréation → ne pas empiler un fragment)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ListeContactsFragment())
                .commit()
        }
    }

    /** Utilitaire : remplace le fragment courant et ajoute au back stack. */
    fun navigateTo(fragment: Fragment, addToBackStack: Boolean = true) {
        val tx = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
        if (addToBackStack) tx.addToBackStack(null)
        tx.commit()
    }
}
