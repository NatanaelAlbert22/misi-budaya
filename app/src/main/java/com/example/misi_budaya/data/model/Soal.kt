package com.example.misi_budaya.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

// This file is now only used for deserializing data from Firebase.
// The primary model used within the app is Question.kt

// The Pilihan data class has been removed from this file to avoid redeclaration.
// The app now uses the Pilihan class defined in Question.kt

// Model untuk Soal
data class Soal(
    @get:Exclude @set:Exclude
    var id: String = "",

    @get:PropertyName("gambarSoal")
    @set:PropertyName("gambarSoal")
    var gambarSoal: String = "",

    @get:PropertyName("jawabanBenar")
    @set:PropertyName("jawabanBenar")
    var jawabanBenar: String = "",

    @get:PropertyName("paketId")
    @set:PropertyName("paketId")
    var paketId: String = "",

    var soal: String = "",

    var pilihan: List<Pilihan> = emptyList()
) {
    constructor() : this("", "", "", "", "", emptyList())
}
