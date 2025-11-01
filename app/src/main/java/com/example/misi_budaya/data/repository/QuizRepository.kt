package com.example.misi_budaya.data.repository

import com.example.misi_budaya.data.model.Paket
import com.example.misi_budaya.data.model.Soal
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class QuizRepository {

    private val db = FirebaseFirestore.getInstance()
    private val paketCollection = db.collection("Paket")
    private val soalCollection = db.collection("Soal")

    fun getPaketList(onResult: (Result<List<Paket>>) -> Unit) {
        paketCollection.get()
            .addOnSuccessListener { result ->
                val paketList = result.documents.mapNotNull { document ->
                    // Konversi dokumen ke objek Paket dan tambahkan ID-nya
                    document.toObject<Paket>()?.apply {
                        id = document.id
                    }
                }
                onResult(Result.success(paketList))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    fun getSoalList(paketId: String, onResult: (Result<List<Soal>>) -> Unit) {
        // Di Firestore, ID dokumen paket adalah referensinya
        // Kita perlu mengubah cara query soal berdasarkan ID paket dari dokumen 'Paket'
        // Asumsi: Field 'paketId' di koleksi 'Soal' berisi ID dokumen dari koleksi 'Paket'
        soalCollection.whereEqualTo("paketId", paketId).get()
            .addOnSuccessListener { result ->
                val soalList = result.documents.mapNotNull { document ->
                    document.toObject<Soal>()?.apply {
                        id = document.id
                    }
                }
                onResult(Result.success(soalList))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }
}
