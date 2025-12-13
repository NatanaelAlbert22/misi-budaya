package com.example.misi_budaya.service

import android.content.Context
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.misi_budaya.data.local.AppDatabase
import com.example.misi_budaya.data.model.Location
import com.example.misi_budaya.data.model.QuizPackage
import com.example.misi_budaya.util.location.LocationData
import com.example.misi_budaya.util.location.LocationService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Service untuk monitoring lokasi pemain dan unlock secret quiz secara real-time
 */
class LocationAwareQuizService(
    private val context: Context,
    private val locationService: LocationService,
    private val userId: String
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val quizPackageDao = AppDatabase.getDatabase(context).quizPackageDao()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + Job())
    
    private companion object {
        private const val TAG = "LocationAwareQuizService"
        private const val LOCATION_CHECK_INTERVAL_MS = 5000L // Check setiap 5 detik
    }
    
    // StateFlow untuk memberi tahu UI tentang quiz yang di-unlock
    private val _unlockedQuizzes = mutableListOf<String>()
    val unlockedQuizzes: List<String> get() = _unlockedQuizzes.toList()
    
    // SharedFlow untuk emit event ketika ada quiz baru di-unlock
    private val _newUnlockedQuizEvent = MutableSharedFlow<String>(replay = 0)
    val newUnlockedQuizEvent: SharedFlow<String> = _newUnlockedQuizEvent.asSharedFlow()
    
    fun startMonitoring() {
        Log.d(TAG, "🚀 Starting location monitoring for user: $userId")
        
        coroutineScope.launch {
            // Cek apakah user premium
            try {
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                val isPremium = userDoc.getBoolean("isPremium") ?: false
                
                if (isPremium) {
                    Log.d(TAG, "👑 User is premium! Skipping location monitoring.")
                    // Premium users tidak perlu location monitoring
                    return@launch
                }
                
                Log.d(TAG, "📍 User is not premium. Starting location monitoring...")
                locationService.startLocationUpdates()
                
                locationService.currentLocation.collect { location ->
                    if (location != null) {
                        Log.d(TAG, "📍 Location update received: ${location.latitude}, ${location.longitude}, accuracy: ${location.accuracy}m")
                        checkSecretQuizzes(location)
                    } else {
                        Log.w(TAG, "⚠️ Location is null")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking premium status or monitoring location", e)
                // Fallback: mulai location monitoring jika ada error
                try {
                    locationService.startLocationUpdates()
                    locationService.currentLocation.collect { location ->
                        if (location != null) {
                            checkSecretQuizzes(location)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Fallback location monitoring also failed", e)
                }
            }
        }
    }
    
    fun stopMonitoring() {
        Log.d(TAG, "Stopping location monitoring")
        locationService.stopLocationUpdates()
        coroutineScope.cancel()
    }
    
    /**
     * Check semua secret quiz dan unlock jika user berada di lokasi atau jika user premium
     */
    private suspend fun checkSecretQuizzes(location: LocationData) {
        try {
            Log.d(TAG, "Checking secret quizzes for location: ${location.latitude}, ${location.longitude}")
            
            // Cek dulu apakah user adalah premium user
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val isPremium = userDoc.getBoolean("isPremium") ?: false
            Log.d(TAG, "User isPremium: $isPremium")
            
            // Fetch semua quiz packages dari Firestore
            firestore.collection("Paket")
                .whereEqualTo("Secret", true)
                .get()
                .addOnSuccessListener { documents ->
                    Log.d(TAG, "Found ${documents.size()} secret quizzes in Firestore")
                    
                    coroutineScope.launch {
                        for (doc in documents) {
                            val quizData = doc.data
                            Log.d(TAG, "Document: ${doc.id}, Data: $quizData")
                            
                            val quizName = quizData?.get("NamaPaket") as? String
                            if (quizName == null) {
                                Log.w(TAG, "Quiz name not found in document ${doc.id}")
                                continue
                            }
                            
                            val isSecret = quizData["Secret"] as? Boolean ?: false
                            Log.d(TAG, "Processing quiz: $quizName, isSecret: $isSecret")
                            
                            if (isSecret) {
                                // Jika user premium, unlock semua secret quiz
                                if (isPremium) {
                                    Log.d(TAG, "👑 Premium user detected! Auto-unlocking secret quiz: $quizName")
                                    unlockSecretQuiz(quizName, 0.0, 0.0)
                                } else {
                                    // Jika bukan premium, check lokasi biasa
                                    // Extract location dari Firestore (GeoPoint format)
                                    val geoPoint = quizData["location"] as? GeoPoint
                                    Log.d(TAG, "GeoPoint: $geoPoint")
                                    
                                    if (geoPoint != null) {
                                        val lat = geoPoint.latitude
                                        val lon = geoPoint.longitude
                                        
                                        Log.d(TAG, "Quiz location: lat=$lat, lon=$lon")
                                        
                                        // Check distance
                                        val distance = locationService.calculateDistance(
                                            location.latitude, location.longitude,
                                            lat, lon
                                        )
                                        
                                        val radiusMeters = (quizData["radiusMeters"] as? Number)?.toFloat() ?: 500f
                                        
                                        Log.d(TAG, "Quiz: $quizName, Distance: ${"%.2f".format(distance)} m, Radius: ${"%.2f".format(radiusMeters)} m")
                                        
                                        // Jika user dalam radius, unlock quiz
                                        if (distance <= radiusMeters) {
                                            Log.d(TAG, "✅ User DALAM RADIUS! Unlocking quiz: $quizName")
                                            unlockSecretQuiz(quizName, lat, lon)
                                        } else {
                                            Log.d(TAG, "❌ User DILUAR RADIUS untuk quiz: $quizName")
                                        }
                                    } else {
                                        Log.w(TAG, "GeoPoint is null for quiz: $quizName")
                                    }
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error checking secret quizzes", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in checkSecretQuizzes", e)
        }
    }
    
    /**
     * Unlock secret quiz di Firestore dan update user progress
     * Cek dulu apakah sudah unlock sebelumnya di Firestore
     */
    private suspend fun unlockSecretQuiz(quizName: String, lat: Double, lon: Double) {
        // Cek dulu di list lokal
        if (_unlockedQuizzes.contains(quizName)) {
            Log.d(TAG, "Quiz $quizName already unlocked (in local list)")
            return
        }
        
        try {
            // Cek di Firestore juga untuk memastikan tidak ada race condition
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            @Suppress("UNCHECKED_CAST")
            val unlockedQuizzesMap = userDoc.get("unlockedQuizzes") as? Map<String, Any>
            
            if (unlockedQuizzesMap != null && unlockedQuizzesMap.containsKey(quizName)) {
                val isUnlocked = unlockedQuizzesMap[quizName] as? Boolean ?: false
                if (isUnlocked) {
                    Log.d(TAG, "Quiz $quizName already unlocked (checked in Firestore)")
                    if (!_unlockedQuizzes.contains(quizName)) {
                        _unlockedQuizzes.add(quizName)
                    }
                    return
                }
            }
            
            // Jika belum unlock, baru lakukan unlock
            Log.d(TAG, "Unlocking secret quiz: $quizName")
            
            firestore.collection("users")
                .document(userId)
                .update(
                    "unlockedQuizzes.$quizName", true,
                    "lastUnlockedAt", System.currentTimeMillis()
                )
                .await()
            
            Log.d(TAG, "✅ Successfully unlocked quiz in Firestore: $quizName")
            _unlockedQuizzes.add(quizName)
            
            // Emit event untuk notify UI tentang quiz baru yang di-unlock
            _newUnlockedQuizEvent.emit(quizName)
            
            // Update local database juga
            try {
                val existingPackage = quizPackageDao.getQuizPackageByName(quizName)
                if (existingPackage != null) {
                    val updatedPackage = existingPackage.copy(isUnlocked = true)
                    quizPackageDao.updateQuizPackage(updatedPackage)
                    Log.d(TAG, "✓ Updated local database for quiz: $quizName, isUnlocked: true")
                } else {
                    Log.w(TAG, "Package not found in local DB: $quizName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating local database", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in unlockSecretQuiz", e)
        }
    }
    
    /**
     * Cek quiz mana saja yang sudah di-unlock sebelumnya
     */
    suspend fun loadUnlockedQuizzes() {
        try {
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val unlockedQuizzes = document.get("unlockedQuizzes") as? Map<*, *>
                        if (unlockedQuizzes != null) {
                            for ((quiz, isUnlocked) in unlockedQuizzes) {
                                if (isUnlocked == true && quiz is String) {
                                    _unlockedQuizzes.add(quiz)
                                }
                            }
                            Log.d(TAG, "Loaded unlocked quizzes: $_unlockedQuizzes")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error loading unlocked quizzes", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in loadUnlockedQuizzes", e)
        }
    }
}
