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
            android.util.Log.d("QuizRepository", "Starting refreshPaketList from Firebase")
            val result = paketCollection.get().await()
            android.util.Log.d("QuizRepository", "Firebase returned ${result.documents.size} documents")
            
            // Log setiap dokumen untuk debugging
            result.documents.forEach { doc ->
                android.util.Log.d("QuizRepository", "Document ID: ${doc.id}, Data: ${doc.data}")
            }
            
            val firebasePaketList = result.documents.mapNotNull { document ->
                val paket = document.toObject(Paket::class.java)
                if (paket != null) {
                    paket.id = document.id
                    android.util.Log.d("QuizRepository", "Parsed Paket: namaPaket=${paket.namaPaket}, isSecret=${paket.isSecret}")
                } else {
                    android.util.Log.w("QuizRepository", "Failed to parse document ${document.id}")
                }
                paket
            }
            android.util.Log.d("QuizRepository", "Parsed ${firebasePaketList.size} Paket objects")

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
                android.util.Log.d("QuizRepository", "Added package to upsert: ${paket.namaPaket}")
            }

            android.util.Log.d("QuizRepository", "Inserting ${packagesToUpsert.size} packages to Room")
            quizPackageDao.insertAll(*packagesToUpsert.toTypedArray())
            android.util.Log.d("QuizRepository", "Successfully inserted packages to Room")

        } catch (e: Exception) {
            android.util.Log.e("QuizRepository", "Error in refreshPaketList", e)
            throw e
        }
    }

    // --- Questions ---
    suspend fun getSoalList(paketId: String, forceRefresh: Boolean = false): List<Question> {
        var localQuestions = questionDao.getQuestionsForQuiz(paketId)
        if (localQuestions.isNotEmpty() && !forceRefresh) {
            return localQuestions
        }

        // If local is empty or force refresh, try fetching from Firebase
        val firebaseSoalList = soalCollection.whereEqualTo("paketId", paketId).get().await()
            .documents.mapNotNull { document ->
                document.toObject(Soal::class.java)?.apply { id = document.id }
            }

        val questionsToCache = firebaseSoalList.map { soal ->
            Question(
                id = soal.id,
                quizPackageName = paketId,
                questionText = soal.soal,
                questionImageUrl = soal.gambarSoal,
                choices = soal.pilihan.map { Pilihan(it.id, it.teks, it.gambar) },
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
                    val raw = snapshot.get("quizScores") as? Map<*, *> ?: emptyMap<Any, Any?>()
                    val oldQuizScores: Map<String, Long> = raw.entries.associate { (k, v) ->
                        val key = k.toString()
                        val value = when (v) {
                            is Number -> v.toLong()
                            is String -> v.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                        key to value
                    }
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
            android.util.Log.e("QuizRepository", "updateUserScoresInFirestore failed", e)
        }
    }

    // --- New: User score sync utilities ---
    /**
     * Fetch quizScores map for a user from Firestore (returns empty map on failure)
     */
    suspend fun getUserQuizScores(uid: String): Map<String, Long> {
        return try {
            val doc = usersCollection.document(uid).get().await()
            if (doc.exists()) {
                val raw = doc.get("quizScores") as? Map<*, *> ?: emptyMap<Any, Any?>()
                raw.entries.associate { (k, v) ->
                    val key = k.toString()
                    val value = when (v) {
                        is Number -> v.toLong()
                        is String -> v.toLongOrNull() ?: 0L
                        else -> 0L
                    }
                    key to value
                }
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            android.util.Log.e("QuizRepository", "Failed to fetch user quizScores", e)
            emptyMap()
        }
    }

    /**
     * Clear all local quiz package scores in Room (used when offline session not associated with account)
     */
    suspend fun clearLocalScores() {
        try {
            quizPackageDao.clearQuizPackages()
        } catch (e: Exception) {
            android.util.Log.e("QuizRepository", "Failed to clear local scores", e)
        }
    }

    /**
     * Sync scores between Firestore and Room for given user uid.
     * Strategy: for each quiz in Firestore or Room, keep the higher score in both places.
     */
    suspend fun syncScoresForUser(uid: String) {
        try {
            val remoteScores = getUserQuizScores(uid).toMutableMap()

            // pull all local packages
            val localPackages = quizPackageDao.getAllQuizPackagesOnce()

            // Merge keys
            val allKeys = (remoteScores.keys + localPackages.map { it.name }).toSet()

            var anyRemoteUpdated = false
             for (quizName in allKeys) {
                 val localPkg = localPackages.find { it.name == quizName }
                 val localScore = localPkg?.score ?: 0
                 val remoteScore = (remoteScores[quizName] ?: 0L).toInt()

                 val winnerScore = maxOf(localScore, remoteScore)

                 // update local
                 if (localPkg != null) {
                     val updatedPkg = localPkg.copy(score = winnerScore, isCompleted = winnerScore > 0)
                     quizPackageDao.updateQuizPackage(updatedPkg)
                 } else {
                     // insert new package to local
                     val newPkg = QuizPackage(name = quizName, description = "", score = winnerScore, isCompleted = winnerScore > 0)
                     quizPackageDao.insertAll(newPkg)
                 }

                 // update remote if needed
                 val remoteScoreLong = remoteScores[quizName] ?: 0L
                 if (winnerScore.toLong() > remoteScoreLong) {
                     // write to firestore user document
                     try {
                         val userRef = usersCollection.document(uid)
                         userRef.update("quizScores.$quizName", winnerScore.toLong()).await()
                         // adjust totalScore as naive sum difference (for safety, we can recalc totalScore server-side)
                         val doc = userRef.get().await()
                         val oldTotal = doc.getLong("totalScore") ?: 0L
                         val oldForQuiz = remoteScoreLong
                         val newTotal = oldTotal + (winnerScore.toLong() - oldForQuiz)
                         userRef.update("totalScore", newTotal).await()
                         // notify leaderboard listeners that remote data changed
                         try { com.example.misi_budaya.util.AppEvents.emitLeaderboardRefresh() } catch (_: Exception) {}
                        anyRemoteUpdated = true
                     } catch (e: Exception) {
                         android.util.Log.e("QuizRepository", "Failed to update remote score for $quizName", e)
                     }
                 }
             }

            // If any remote updates occurred, emit a final refresh event to be safe
            if (anyRemoteUpdated) {
                try { com.example.misi_budaya.util.AppEvents.emitLeaderboardRefresh() } catch (_: Exception) {}
            }
         } catch (e: Exception) {
             android.util.Log.e("QuizRepository", "Error in syncScoresForUser", e)
         }
     }

    suspend fun getPaketByName(namaPaket: String): Paket? {
        return try {
            val query = paketCollection.whereEqualTo("NamaPaket", namaPaket).limit(1).get().await()
            val doc = query.documents.firstOrNull()
            doc?.toObject(Paket::class.java)?.apply { id = doc.id }
        } catch (e: Exception) {
            android.util.Log.e("QuizRepository", "Failed to fetch Paket by name", e)
            null
        }
    }

    suspend fun downloadAllQuestionsForAllPackages() {
        try {
            android.util.Log.d("QuizRepository", "Starting download all questions for all packages")

            // Get all packages from local DB
            val allPackages = quizPackageDao.getAllQuizPackagesOnce()

            for (paket in allPackages) {
                try {
                    // Get questions from Firebase for this package
                    val firebaseSoalList = soalCollection.whereEqualTo("paketId", paket.name).get().await()
                        .documents.mapNotNull { document ->
                            document.toObject(Soal::class.java)?.apply { id = document.id }
                        }

                    if (firebaseSoalList.isNotEmpty()) {
                        val questionsToCache = firebaseSoalList.map { soal ->
                            Question(
                                id = soal.id,
                                quizPackageName = paket.name,
                                questionText = soal.soal,
                                questionImageUrl = soal.gambarSoal,
                                choices = soal.pilihan.map { Pilihan(it.id, it.teks, it.gambar) },
                                correctAnswerId = soal.jawabanBenar
                            )
                        }

                        android.util.Log.d("QuizRepository", "Inserting ${questionsToCache.size} questions for package ${paket.name}")
                        // insertAll uses OnConflictStrategy.REPLACE, so old questions will be replaced
                        questionDao.insertAll(questionsToCache)
                        android.util.Log.d("QuizRepository", "Successfully inserted questions for ${paket.name}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("QuizRepository", "Failed to download questions for ${paket.name}", e)
                    // Continue downloading for other packages even if one fails
                }
            }

            android.util.Log.d("QuizRepository", "Successfully downloaded all questions")
            // notify UI that questions are available locally
            try {
                com.example.misi_budaya.util.AppEvents.emitQuestionsDownloaded()
            } catch (_: Exception) {}
        } catch (e: Exception) {
            android.util.Log.e("QuizRepository", "Error in downloadAllQuestionsForAllPackages", e)
            throw e
        }
    }

}
