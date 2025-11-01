package com.example.misi_budaya.data.model

import com.google.firebase.firestore.Exclude
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
    var isSecret: Boolean = false
) {
    // Konstruktor kosong diperlukan oleh Firebase untuk deserialisasi data
    constructor() : this("", "", false)
}
