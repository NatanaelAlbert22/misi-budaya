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
                // Check for existing data in the local database
                val existingPackage = quizPackageDao.getQuizPackageByName(paket.namaPaket)

                // Create a new package object, merging Firebase data with existing local data
                val mergedPackage = QuizPackage(
                    name = paket.namaPaket,
                    description = "", // Description is not used as per the user's request
                    // Preserve the score and completion status if it exists locally
                    score = existingPackage?.score ?: 0,
                    isCompleted = existingPackage?.isCompleted ?: false
                )
                packagesToUpsert.add(mergedPackage)
            }

            // Use insertAll (with OnConflictStrategy.REPLACE) to update or insert the merged data
            quizPackageDao.insertAll(*packagesToUpsert.toTypedArray())

        } catch (e: Exception) {
            // Propagate the error to be handled by the caller (Presenter).
            throw e
        }
    }

    suspend fun updateQuizScore(quizName: String, newScore: Int) {
        val quizPackage = quizPackageDao.getQuizPackageByName(quizName)
        if (quizPackage != null) {
            // Update the score and mark it as completed
            val updatedPackage = quizPackage.copy(score = newScore, isCompleted = true)
            quizPackageDao.updateQuizPackage(updatedPackage)
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
