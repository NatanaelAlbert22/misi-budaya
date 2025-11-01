package com.example.misi_budaya.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

// Model untuk Pilihan Jawaban
data class Pilihan(
    var id: String = "",
    var gambar: String = "",
    var teks: String = ""
) {
    constructor() : this("", "", "")
}

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
