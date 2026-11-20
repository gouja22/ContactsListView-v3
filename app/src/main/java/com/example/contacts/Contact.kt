package com.example.contacts

import android.os.Parcel
import android.os.Parcelable

/**
 * Modèle Contact — V3 : ajout de latitude et longitude pour Google Maps.
 * Implémente Parcelable pour la transmission via Bundle entre fragments.
 */
data class Contact(
    val id: Int,
    var nom: String,
    var prenom: String,
    var telephone: String,
    var email: String,
    var photoUri: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id        = parcel.readInt(),
        nom       = parcel.readString() ?: "",
        prenom    = parcel.readString() ?: "",
        telephone = parcel.readString() ?: "",
        email     = parcel.readString() ?: "",
        photoUri  = parcel.readString(),
        latitude  = parcel.readValue(Double::class.java.classLoader) as? Double,
        longitude = parcel.readValue(Double::class.java.classLoader) as? Double
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(nom)
        parcel.writeString(prenom)
        parcel.writeString(telephone)
        parcel.writeString(email)
        parcel.writeString(photoUri)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Contact> {
        override fun createFromParcel(parcel: Parcel): Contact = Contact(parcel)
        override fun newArray(size: Int): Array<Contact?> = arrayOfNulls(size)
    }
}
