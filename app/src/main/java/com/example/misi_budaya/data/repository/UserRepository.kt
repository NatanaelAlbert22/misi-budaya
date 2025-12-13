package com.example.misi_budaya.data.repository

import android.util.Log
import com.example.misi_budaya.data.model.UserProfile
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository untuk manage user profile di Firestore
 */
class UserRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = firestore.collection("users")
    
    companion object {
        private const val TAG = "UserRepository"
    }
    
    /**
     * Get user profile dari Firestore
     */
    suspend fun getUserProfile(uid: String): Result<UserProfile> {
        return try {
            val document = usersCollection.document(uid).get().await()
            val profile = document.toObject(UserProfile::class.java)
            if (profile != null) {
                Result.success(profile)
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create atau update user profile
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            usersCollection.document(profile.uid).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update username saja
     */
    suspend fun updateUsername(uid: String, newUsername: String): Result<Unit> {
        return try {
            Log.d(TAG, "Updating username for uid: $uid to: $newUsername")
            
            // Cek apakah document sudah ada
            val docRef = usersCollection.document(uid)
            val snapshot = docRef.get().await()
            
            if (snapshot.exists()) {
                // Document ada, update saja
                docRef.update("username", newUsername).await()
                Log.d(TAG, "Username updated successfully")
            } else {
                // Document belum ada, buat baru
                Log.d(TAG, "Document not found, creating new profile")
                val currentUser = auth.currentUser
                val profile = UserProfile(
                    uid = uid,
                    username = newUsername,
                    email = currentUser?.email ?: "",
                    totalScore = 0L,
                    quizScores = emptyMap(),
                    unlockedQuizzes = emptyMap()
                )
                docRef.set(profile).await()
                Log.d(TAG, "Profile created with new username")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update username", e)
            Result.failure(Exception("Gagal mengubah username: ${e.message}"))
        }
    }
    
    /**
     * Update password user
     * Memerlukan re-authentication dengan password lama
     */
    suspend fun updatePassword(
        currentPassword: String, 
        newPassword: String
    ): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("User tidak login"))
            val email = user.email ?: return Result.failure(Exception("Email tidak ditemukan"))
            
            Log.d(TAG, "Re-authenticating user: $email")
            // Re-authenticate user dengan password lama
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            
            Log.d(TAG, "Updating password")
            // Update password
            user.updatePassword(newPassword).await()
            
            Log.d(TAG, "Password updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update password", e)
            val errorMsg = when {
                e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> 
                    "Password saat ini salah"
                e.message?.contains("password is invalid") == true -> 
                    "Password saat ini salah"
                e.message?.contains("credential is incorrect") == true -> 
                    "Password saat ini salah"
                e.message?.contains("network") == true -> 
                    "Tidak ada koneksi internet"
                e.message?.contains("too-many-requests") == true -> 
                    "Terlalu banyak percobaan, coba lagi nanti"
                else -> 
                    "Gagal mengubah password: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        }
    }
    
    /**
     * Create initial user profile setelah sign up
     */
    suspend fun createInitialProfile(user: FirebaseUser, username: String): Result<Unit> {
        return try {
            val profile = UserProfile(
                uid = user.uid,
                username = username,
                email = user.email ?: "",
                totalScore = 0L,
                quizScores = emptyMap(),
                unlockedQuizzes = emptyMap(),
                isPremium = false // Setiap user baru dimulai dengan isPremium = false
            )
            saveUserProfile(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check apakah username sudah dipakai
     */
    suspend fun isUsernameTaken(username: String): Boolean {
        return try {
            val result = usersCollection
                .whereEqualTo("username", username)
                .get()
                .await()
            !result.isEmpty
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Upgrade user menjadi premium (ubah isPremium menjadi true)
     */
    suspend fun upgradeToPremium(uid: String): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            usersCollection.document(uid).update(
                "isPremium", true,
                "premiumUpgradeDate", timestamp
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upgrade to premium", e)
            Result.failure(Exception("Gagal mengupgrade ke premium: ${e.message}"))
        }
    }
}
