package com.example.misi_budaya.data.repository

import com.example.misi_budaya.data.model.Paket
import com.example.misi_budaya.data.model.QuizPackage
import com.example.misi_budaya.data.model.QuizPackageDao
import com.example.misi_budaya.data.model.Soal
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class QuizRepository(private val quizPackageDao: QuizPackageDao) {

    private val db = FirebaseFirestore.getInstance()
    private val paketCollection = db.collection("Paket")
    private val soalCollection = db.collection("Soal")

    fun getPaketList(): Flow<List<QuizPackage>> {
        return quizPackageDao.getAllQuizPackages()
    }

    suspend fun refreshPaketList() {
        try {
            val result = paketCollection.get().await()
            val firebasePaketList = result.documents.mapNotNull { document ->
                document.toObject<Paket>()
            }

            val packagesToUpsert = mutableListOf<QuizPackage>()

            for (paket in firebasePaketList.filter { !it.isSecret }) {
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

    suspend fun updateQuizScore(quizName: String, newScore: Int) {
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

    fun getSoalList(paketId: String, onResult: (Result<List<Soal>>) -> Unit) {
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
