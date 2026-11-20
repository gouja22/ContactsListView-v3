package com.example.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * CarteContactFragment — V3
 *
 * Affiche la position GPS d'un contact sur Google Maps : la carte est centrée
 * sur ses coordonnées et un marqueur porte son nom.
 * Si les coordonnées sont absentes ou invalides, un message explicite est affiché.
 * Le bouton « Retour » (ou la touche retour système) revient au détail.
 */
class CarteContactFragment : Fragment() {

    companion object {
        private const val ARG_NOM = "nom"
        private const val ARG_LAT = "latitude"
        private const val ARG_LNG = "longitude"
        private const val ZOOM = 14f

        fun newInstance(nom: String, lat: Double, lng: Double) = CarteContactFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_NOM, nom)
                putDouble(ARG_LAT, lat)
                putDouble(ARG_LNG, lng)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_carte_contact, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.titre_carte)

        val tvMessage = view.findViewById<TextView>(R.id.tvMessageCarte)
        val tvInfo    = view.findViewById<TextView>(R.id.tvInfoCarte)
        val mapContainer = view.findViewById<View>(R.id.mapContainer)

        view.findViewById<Button>(R.id.btnRetourCarte).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val args = arguments
        val nom = args?.getString(ARG_NOM).orEmpty()
        val lat = if (args?.containsKey(ARG_LAT) == true) args.getDouble(ARG_LAT) else Double.NaN
        val lng = if (args?.containsKey(ARG_LNG) == true) args.getDouble(ARG_LNG) else Double.NaN

        val valides = !lat.isNaN() && !lng.isNaN() && lat in -90.0..90.0 && lng in -180.0..180.0
        if (!valides) {
            mapContainer.visibility = View.GONE
            tvInfo.visibility = View.GONE
            tvMessage.visibility = View.VISIBLE
            tvMessage.text = getString(R.string.msg_coord_absentes)
            return
        }

        tvInfo.text = getString(R.string.info_carte, nom, lat, lng)

        // SupportMapFragment déclaré dans le layout (fragment enfant)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        if (mapFragment == null) {
            mapContainer.visibility = View.GONE
            tvMessage.visibility = View.VISIBLE
            tvMessage.text = getString(R.string.msg_carte_indisponible)
            return
        }

        mapFragment.getMapAsync { googleMap ->
            val position = LatLng(lat, lng)
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(position).title(nom))?.showInfoWindow()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM))
            googleMap.uiSettings.isZoomControlsEnabled = true
        }
    }
}
