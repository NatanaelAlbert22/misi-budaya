package com.example.misi_budaya.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PaymentRepository {
    private val db = FirebaseFirestore.getInstance()
    
    companion object {
        private const val TAG = "PaymentRepository"
        private const val USERS_COLLECTION = "users"
        private const val TRANSACTIONS_COLLECTION = "transactions"
    }
    
    /**
     * Record transaction ke Firestore
     */
    suspend fun recordTransaction(
        userId: String,
        orderId: String,
        amount: Long,
        description: String,
        status: String = "pending"
    ): Result<String> = try {
        val transaction = mapOf(
            "userId" to userId,
            "orderId" to orderId,
            "amount" to amount,
            "description" to description,
            "status" to status,
            "timestamp" to System.currentTimeMillis()
        )
        
        val docRef = db.collection(TRANSACTIONS_COLLECTION).add(transaction).await()
        Log.d(TAG, "Transaction recorded: ${docRef.id}")
        Result.success(docRef.id)
    } catch (e: Exception) {
        Log.e(TAG, "Error recording transaction", e)
        Result.failure(e)
    }
    
    /**
     * Update transaction status setelah pembayaran berhasil
     */
    suspend fun updateTransactionStatus(
        transactionId: String,
        status: String,
        paymentMethod: String? = null
    ): Result<Unit> = try {
        val update = mutableMapOf<String, Any>(
            "status" to status,
            "updatedAt" to System.currentTimeMillis()
        )
        
        paymentMethod?.let {
            update["paymentMethod"] = it
        }
        
        db.collection(TRANSACTIONS_COLLECTION)
            .document(transactionId)
            .update(update)
            .await()
        
        Log.d(TAG, "Transaction status updated: $transactionId -> $status")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error updating transaction status", e)
        Result.failure(e)
    }
    
    /**
     * Upgrade user ke premium setelah pembayaran berhasil
     */
    suspend fun upgradeToPremiumAfterPayment(userId: String): Result<Unit> = try {
        db.collection(USERS_COLLECTION)
            .document(userId)
            .update(
                mapOf(
                    "isPremium" to true,
                    "premiumStartDate" to System.currentTimeMillis()
                )
            )
            .await()
        
        Log.d(TAG, "User upgraded to premium: $userId")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error upgrading user to premium", e)
        Result.failure(e)
    }
    
    /**
     * Get transaction by order ID
     */
    suspend fun getTransactionByOrderId(orderId: String): Result<Map<String, Any>?> = try {
        val result = db.collection(TRANSACTIONS_COLLECTION)
            .whereEqualTo("orderId", orderId)
            .get()
            .await()
        
        val transaction = if (result.documents.isNotEmpty()) {
            result.documents[0].data
        } else {
            null
        }
        
        Result.success(transaction)
    } catch (e: Exception) {
        Log.e(TAG, "Error getting transaction", e)
        Result.failure(e)
    }
    
    /**
     * Get latest transaction for user
     * Digunakan untuk cek apakah pembayaran sudah success
     */
    suspend fun getLatestTransactionByUserId(userId: String): Result<Map<String, Any>?> = try {
        val result = db.collection(TRANSACTIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
        
        val transaction = if (result.documents.isNotEmpty()) {
            result.documents[0].data
        } else {
            null
        }
        
        Result.success(transaction)
    } catch (e: Exception) {
        Log.e(TAG, "Error getting latest transaction", e)
        Result.failure(e)
    }
}
