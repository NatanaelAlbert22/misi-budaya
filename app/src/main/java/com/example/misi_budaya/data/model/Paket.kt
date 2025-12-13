package com.example.misi_budaya.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

// Menggunakan @PropertyName untuk memetakan nama field di Firebase
// ke nama variabel yang sesuai dengan konvensi Kotlin (camelCase).
data class Paket(
    // Exclude 'id' so Firestore doesn't try to map it from the document fields
    @get:Exclude @set:Exclude
    var id: String = "",

    @get:PropertyName("NamaPaket")
    @set:PropertyName("NamaPaket")
    var namaPaket: String = "",

    @get:PropertyName("Secret")
    @set:PropertyName("Secret")
    var isSecret: Boolean = false,

    @get:PropertyName("Deskripsi")
    @set:PropertyName("Deskripsi")
    var deskripsi: String = "", // New field for description from Firestore
    
    @get:PropertyName("location")
    @set:PropertyName("location")
    var location: GeoPoint? = null, // GeoPoint untuk secret quiz location
    
    @get:PropertyName("radiusMeters")
    @set:PropertyName("radiusMeters")
    var radiusMeters: Float = 500f // Radius untuk secret quiz detection
) {
    // Konstruktor kosong diperlukan oleh Firebase untuk deserialisasi data
    constructor() : this("", "", false, "", null, 500f)
}
