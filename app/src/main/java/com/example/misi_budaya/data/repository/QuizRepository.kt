package com.example.misi_budaya.data.repository

import com.example.misi_budaya.data.model.Paket
import com.example.misi_budaya.data.model.QuizPackage
import com.example.misi_budaya.data.model.QuizPackageDao
import com.example.misi_budaya.data.model.Soal
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class QuizRepository(private val quizPackageDao: QuizPackageDao) {

    private val db = FirebaseFirestore.getInstance()
    private val paketCollection = db.collection("Paket")
    private val soalCollection = db.collection("Soal")

    // COROUTINE: Menggunakan Flow untuk reactive data
    // flowOn(Dispatchers.IO) memastikan query database berjalan di background thread
    fun getPaketList(): Flow<List<QuizPackage>> {
        return quizPackageDao.getAllQuizPackages()
            .flowOn(Dispatchers.IO)
    }

    // SUSPEND FUNCTION: Fungsi yang bisa di-pause tanpa blocking thread
    // Otomatis berjalan di thread yang memanggil, jadi perlu withContext untuk switch thread
    suspend fun refreshPaketList() {
        // withContext(Dispatchers.IO) untuk operasi network & database
        withContext(Dispatchers.IO) {
            try {
                // .await() mengubah callback Firebase menjadi suspend function
                val result = paketCollection.get().await()
                val firebasePaketList = result.documents.mapNotNull { document ->
                    document.toObject<Paket>()
                }

                // Filter hanya paket yang tidak secret
                val validFirebasePakets = firebasePaketList.filter { !it.isSecret }
                val firebasePaketNames = validFirebasePakets.map { it.namaPaket }.toSet()

                // Ambil semua data lokal
                val localPackages = quizPackageDao.getAllQuizPackagesSync()

                // HAPUS data lokal yang tidak ada di Firestore
                localPackages.forEach { localPackage ->
                    if (localPackage.name !in firebasePaketNames) {
                        quizPackageDao.deleteQuizPackage(localPackage)
                    }
                }

                // INSERT atau UPDATE data dari Firestore
                val packagesToUpsert = mutableListOf<QuizPackage>()

                for (paket in validFirebasePakets) {
                    val existingPackage = quizPackageDao.getQuizPackageByName(paket.namaPaket)

                    val mergedPackage = QuizPackage(
                        name = paket.namaPaket,
                        description = "",
                        score = existingPackage?.score ?: 0,
                        isCompleted = existingPackage?.isCompleted ?: false
                    )
                    packagesToUpsert.add(mergedPackage)
                }

                quizPackageDao.insertAll(*packagesToUpsert.toTypedArray())

            } catch (e: Exception) {
                throw e
            }
        }
    }

    // SUSPEND FUNCTION untuk update score di database
    suspend fun updateQuizScore(quizName: String, newScore: Int) {
        withContext(Dispatchers.IO) {
            val existingPackage = quizPackageDao.getQuizPackageByName(quizName)
            if (existingPackage != null) {
                // Only update if the new score is higher
                if (newScore > existingPackage.score) {
                    val updatedPackage = existingPackage.copy(score = newScore, isCompleted = true)
                    quizPackageDao.updateQuizPackage(updatedPackage)
                } else {
                    // If the score is not higher, still mark it as completed
                    if (!existingPackage.isCompleted) {
                        val updatedPackage = existingPackage.copy(isCompleted = true)
                        quizPackageDao.updateQuizPackage(updatedPackage)
                    }
                }
            }
        }
    }

    // MENGUBAH CALLBACK menjadi SUSPEND FUNCTION (Best Practice)
    // Ini lebih modern dan mudah di-handle dengan try-catch
    suspend fun getSoalList(paketId: String): List<Soal> {
        return withContext(Dispatchers.IO) {
            try {
                val result = soalCollection
                    .whereEqualTo("paketId", paketId)
                    .get()
                    .await() // Mengubah callback menjadi suspend

                result.documents.mapNotNull { document ->
                    document.toObject<Soal>()?.apply {
                        id = document.id
                    }
                }
            } catch (e: Exception) {
                throw e // Throw error, akan di-catch oleh caller
            }
        }
    }

    // DEPRECATED: Versi callback lama (untuk backward compatibility)
    // Sebaiknya gunakan versi suspend di atas
    @Deprecated("Use suspend version instead", ReplaceWith("getSoalList(paketId)"))
    fun getSoalListCallback(paketId: String, onResult: (Result<List<Soal>>) -> Unit) {
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
