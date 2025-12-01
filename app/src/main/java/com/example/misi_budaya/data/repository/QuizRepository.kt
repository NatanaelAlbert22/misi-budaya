package com.example.misi_budaya.data.repository

import com.example.misi_budaya.data.model.Paket
import com.example.misi_budaya.data.model.Pilihan
import com.example.misi_budaya.data.model.Question
import com.example.misi_budaya.data.model.QuestionDao
import com.example.misi_budaya.data.model.QuizPackage
import com.example.misi_budaya.data.model.QuizPackageDao
import com.example.misi_budaya.data.model.Soal
import com.example.misi_budaya.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class QuizRepository(private val quizPackageDao: QuizPackageDao, private val questionDao: QuestionDao) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val paketCollection = db.collection("Paket")
    private val soalCollection = db.collection("Soal")
    private val usersCollection = db.collection("Users")

    // --- Leaderboard --- 
    suspend fun getLeaderboardData(): List<UserProfile> {
        return try {
            usersCollection
                .orderBy("totalScore", Query.Direction.DESCENDING)
                .limit(100) // Limit to top 100 to avoid pulling too much data
                .get()
                .await()
                .toObjects(UserProfile::class.java)
        } catch (e: Exception) {
            // Return an empty list or throw the exception to be handled by the presenter
            emptyList()
        }
    }

    // --- Quiz Packages ---
    fun getPaketList(): Flow<List<QuizPackage>> {
        return quizPackageDao.getAllQuizPackages()
    }

    suspend fun refreshPaketList() {
        try {
            val result = paketCollection.get().await()
            val firebasePaketList = result.documents.mapNotNull { document ->
                document.toObject(Paket::class.java)
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

    // --- Questions ---
    suspend fun getSoalList(paketId: String): List<Question> {
        val localQuestions = questionDao.getQuestionsForQuiz(paketId)
        if (localQuestions.isNotEmpty()) {
            return localQuestions
        }

        val firebaseSoalList = soalCollection.whereEqualTo("paketId", paketId).get().await()
            .documents.mapNotNull { document ->
                document.toObject(Soal::class.java)?.apply { id = document.id }
            }

        val questionsToCache = firebaseSoalList.map { soal ->
            Question(
                id = soal.id,
                quizPackageName = paketId,
                questionText = soal.soal,
                choices = soal.pilihan.map { Pilihan(it.id, it.teks) },
                correctAnswerId = soal.jawabanBenar
            )
        }

        if (questionsToCache.isNotEmpty()) {
            questionDao.insertAll(questionsToCache)
        }

        return questionsToCache
    }

    // --- Score Updates ---
    suspend fun updateQuizScore(quizName: String, newScore: Int) {
        // Update Local Score (Room)
        val existingPackage = quizPackageDao.getQuizPackageByName(quizName)
        if (existingPackage != null) {
            if (newScore > existingPackage.score) {
                val updatedPackage = existingPackage.copy(score = newScore, isCompleted = true)
                quizPackageDao.updateQuizPackage(updatedPackage)
            } else {
                if (!existingPackage.isCompleted) {
                    val updatedPackage = existingPackage.copy(isCompleted = true)
                    quizPackageDao.updateQuizPackage(updatedPackage)
                }
            }
        }
        
        // Update Remote Score (Firestore)
        updateUserScoresInFirestore(quizName, newScore)
    }

    private suspend fun updateUserScoresInFirestore(quizName: String, newScore: Int) {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid
        val userDocRef = usersCollection.document(userId)

        try {
            db.runTransaction {
                transaction ->
                val snapshot = transaction.get(userDocRef)

                if (snapshot.exists()) {
                    // Document exists, so UPDATE it
                    val oldQuizScores = snapshot.get("quizScores") as? Map<String, Long> ?: emptyMap()
                    val oldTotalScore = snapshot.getLong("totalScore") ?: 0L
                    val oldScoreForThisQuiz = oldQuizScores[quizName] ?: 0L

                    if (newScore > oldScoreForThisQuiz) {
                        val scoreDifference = newScore - oldScoreForThisQuiz
                        val newTotalScore = oldTotalScore + scoreDifference
                        transaction.update(userDocRef, "quizScores.$quizName", newScore.toLong())
                        transaction.update(userDocRef, "totalScore", newTotalScore)
                    }
                } else {
                    // Document does not exist, so CREATE it
                    val newUserProfile = UserProfile(
                        uid = userId,
                        email = currentUser.email ?: "",
                        username = currentUser.displayName ?: "",
                        quizScores = mapOf(quizName to newScore.toLong()),
                        totalScore = newScore.toLong(),
                        unlockedQuizzes = emptyMap() // Default for new user
                    )
                    transaction.set(userDocRef, newUserProfile)
                }
                null
            }.await()
        } catch (e: Exception) {
            // For now, we let it fail silently so it doesn't crash the app
        }
    }
}
