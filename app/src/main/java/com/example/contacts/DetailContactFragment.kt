package com.example.contacts

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

/**
 * DetailContactFragment — V3
 *
 * Consultation / modification d'un contact (identifié par son ID reçu en Bundle).
 * Conserve toutes les fonctionnalités V2 : Appeler, E-mail, Galerie, Caméra,
 * Enregistrer, Annuler — et ajoute la latitude / longitude + « Voir sur la carte ».
 *
 * L'enregistrement passe par le ViewModel partagé : la liste est actualisée
 * automatiquement grâce au LiveData observé par ListeContactsFragment.
 */
class DetailContactFragment : Fragment() {

    companion object {
        private const val ARG_CONTACT_ID = "contact_id"
        private const val KEY_PHOTO_URI  = "photo_uri"
        private const val KEY_CAMERA_URI = "camera_uri"

        fun newInstance(contactId: Int) = DetailContactFragment().apply {
            arguments = Bundle().apply { putInt(ARG_CONTACT_ID, contactId) }
        }
    }

    private val viewModel: ContactsViewModel by activityViewModels()

    private var contact: Contact? = null
    private var photoUri: String? = null
    private var photoInitialisee = false
    private var cameraImageUri: Uri? = null

    // --- Vues ---
    private lateinit var imgPhoto: ImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText

    // =========================================================================
    // Lanceurs (doivent être enregistrés avant que le fragment ne soit STARTED)
    // =========================================================================

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                photoUri = uri.toString()
                afficherPhoto()
            } else {
                toast(R.string.msg_selection_photo_annulee)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val uri = cameraImageUri
            if (success && uri != null) {
                photoUri = uri.toString()
                afficherPhoto()
            } else {
                // Suppression de l'entrée MediaStore vide créée avant la prise de vue
                uri?.let { runCatching { requireContext().contentResolver.delete(it, null, null) } }
                toast(R.string.msg_photo_annulee)
            }
            cameraImageUri = null
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) lancerCamera() else toast(R.string.msg_permission_camera_refusee)
        }

    // =========================================================================
    // Cycle de vie
    // =========================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments?.getInt(ARG_CONTACT_ID, -1) ?: -1
        contact = viewModel.getById(id)

        if (savedInstanceState != null) {
            photoUri = savedInstanceState.getString(KEY_PHOTO_URI)
            cameraImageUri = savedInstanceState.getString(KEY_CAMERA_URI)?.let(Uri::parse)
            photoInitialisee = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_detail_contact, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val c = contact
        if (c == null) {
            toast(R.string.erreur_contact_introuvable)
            parentFragmentManager.popBackStack()
            return
        }
        requireActivity().title = getString(R.string.titre_detail)

        imgPhoto    = view.findViewById(R.id.imgPhotoDetail)
        etNom       = view.findViewById(R.id.etNom)
        etPrenom    = view.findViewById(R.id.etPrenom)
        etTelephone = view.findViewById(R.id.etTelephone)
        etEmail     = view.findViewById(R.id.etEmail)
        etLatitude  = view.findViewById(R.id.etLatitude)
        etLongitude = view.findViewById(R.id.etLongitude)

        // Pré-remplissage (les saisies en cours sont restaurées ensuite
        // automatiquement par Android après retour de la carte ou rotation)
        etNom.setText(c.nom)
        etPrenom.setText(c.prenom)
        etTelephone.setText(c.telephone)
        etEmail.setText(c.email)
        etLatitude.setText(c.latitude?.toString() ?: "")
        etLongitude.setText(c.longitude?.toString() ?: "")

        if (!photoInitialisee) {
            photoUri = c.photoUri
            photoInitialisee = true
        }
        afficherPhoto()

        view.findViewById<Button>(R.id.btnAppeler).setOnClickListener { appeler() }
        view.findViewById<Button>(R.id.btnEmail).setOnClickListener { envoyerEmail() }
        view.findViewById<Button>(R.id.btnChoisirPhoto).setOnClickListener {
            try { galleryLauncher.launch("image/*") }
            catch (e: ActivityNotFoundException) { toast(R.string.msg_aucune_app_galerie) }
        }
        view.findViewById<Button>(R.id.btnPrendrePhoto).setOnClickListener { verifierPermissionCamera() }
        view.findViewById<Button>(R.id.btnVoirCarte).setOnClickListener { voirSurLaCarte() }
        view.findViewById<Button>(R.id.btnEnregistrer).setOnClickListener { enregistrer() }
        view.findViewById<Button>(R.id.btnAnnuler).setOnClickListener {
            // Retour sans enregistrer : le ViewModel n'est pas modifié
            parentFragmentManager.popBackStack()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PHOTO_URI, photoUri)
        outState.putString(KEY_CAMERA_URI, cameraImageUri?.toString())
    }

    // =========================================================================
    // Actions
    // =========================================================================

    private fun afficherPhoto() {
        val uri = photoUri
        if (uri == null) {
            imgPhoto.setImageResource(R.drawable.ic_person_placeholder)
            return
        }
        try {
            imgPhoto.setImageURI(Uri.parse(uri))
            if (imgPhoto.drawable == null) imgPhoto.setImageResource(R.drawable.ic_person_placeholder)
        } catch (e: Exception) {
            imgPhoto.setImageResource(R.drawable.ic_person_placeholder)
        }
    }

    /** Intent implicite ACTION_DIAL */
    private fun appeler() {
        val tel = etTelephone.text.toString().trim()
        if (tel.isEmpty()) { etTelephone.error = getString(R.string.msg_telephone_vide); return }
        try {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel")))
        } catch (e: ActivityNotFoundException) {
            toast(R.string.msg_aucune_app_telephone)
        }
    }

    /** Intent implicite ACTION_SENDTO (mailto:) */
    private fun envoyerEmail() {
        val mail = etEmail.text.toString().trim()
        if (mail.isEmpty()) { etEmail.error = getString(R.string.msg_email_vide); return }
        try {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$mail")))
        } catch (e: ActivityNotFoundException) {
            toast(R.string.msg_aucune_app_email)
        }
    }

    private fun verifierPermissionCamera() {
        val pm = requireContext().packageManager
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            toast(R.string.msg_camera_indisponible); return
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            lancerCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun lancerCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "contact_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        val uri = try {
            requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } catch (e: Exception) { null }

        if (uri == null) { toast(R.string.msg_camera_indisponible); return }
        cameraImageUri = uri
        try {
            cameraLauncher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            toast(R.string.msg_camera_indisponible)
        }
    }

    /**
     * Lit et valide les coordonnées saisies.
     * @return Pair(lat, lng) — les deux null si les champs sont vides,
     *         ou null si la saisie est invalide (l'erreur est affichée).
     */
    private fun lireCoordonnees(): Pair<Double?, Double?>? {
        val latTxt = etLatitude.text.toString().trim().replace(',', '.')
        val lngTxt = etLongitude.text.toString().trim().replace(',', '.')

        if (latTxt.isEmpty() && lngTxt.isEmpty()) return Pair(null, null)

        if (latTxt.isEmpty()) { etLatitude.error = getString(R.string.msg_coord_incomplete); return null }
        if (lngTxt.isEmpty()) { etLongitude.error = getString(R.string.msg_coord_incomplete); return null }

        val lat = latTxt.toDoubleOrNull()
        val lng = lngTxt.toDoubleOrNull()
        if (lat == null) { etLatitude.error = getString(R.string.msg_coord_non_numerique); return null }
        if (lng == null) { etLongitude.error = getString(R.string.msg_coord_non_numerique); return null }
        if (lat !in -90.0..90.0)   { etLatitude.error  = getString(R.string.msg_latitude_hors_limites);  return null }
        if (lng !in -180.0..180.0) { etLongitude.error = getString(R.string.msg_longitude_hors_limites); return null }

        return Pair(lat, lng)
    }

    /** Ouvre CarteContactFragment avec les coordonnées actuellement saisies. */
    private fun voirSurLaCarte() {
        val coords = lireCoordonnees() ?: return
        val (lat, lng) = coords
        if (lat == null || lng == null) {
            toast(R.string.msg_coord_absentes)
            return
        }
        val nom = "${etPrenom.text.toString().trim()} ${etNom.text.toString().trim()}".trim()
        (requireActivity() as MainActivity).navigateTo(CarteContactFragment.newInstance(nom, lat, lng))
    }

    /** Valide puis enregistre dans le ViewModel partagé et revient à la liste. */
    private fun enregistrer() {
        val c = contact ?: return
        val nom    = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val tel    = etTelephone.text.toString().trim()
        val mail   = etEmail.text.toString().trim()

        if (nom.isEmpty())    { etNom.error       = getString(R.string.msg_champ_obligatoire); return }
        if (prenom.isEmpty()) { etPrenom.error    = getString(R.string.msg_champ_obligatoire); return }
        if (tel.isEmpty())    { etTelephone.error = getString(R.string.msg_champ_obligatoire); return }
        if (mail.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            etEmail.error = getString(R.string.msg_email_invalide); return
        }
        val (lat, lng) = lireCoordonnees() ?: return

        viewModel.updateContact(
            c.copy(nom = nom, prenom = prenom, telephone = tel, email = mail,
                   photoUri = photoUri, latitude = lat, longitude = lng)
        )
        Toast.makeText(requireContext(), getString(R.string.msg_contact_mis_a_jour, prenom), Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    private fun toast(resId: Int) =
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
}
