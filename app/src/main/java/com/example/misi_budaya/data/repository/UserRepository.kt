package com.example.misi_budaya.data.repository

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
            usersCollection.document(uid)
                .update("username", newUsername)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
            val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
            val email = user.email ?: return Result.failure(Exception("Email not found"))
            
            // Re-authenticate user dengan password lama
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            
            // Update password
            user.updatePassword(newPassword).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
                unlockedQuizzes = emptyMap()
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
}
