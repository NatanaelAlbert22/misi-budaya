package com.example.misi_budaya.data.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction)
    
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserTransactions(userId: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE transactionId = :transactionId")
    suspend fun getTransaction(transactionId: String): Transaction?
    
    @Query("SELECT * FROM transactions WHERE midtransOrderId = :orderId")
    suspend fun getTransactionByOrderId(orderId: String): Transaction?
    
    @Query("UPDATE transactions SET status = :status, updatedAt = :updatedAt WHERE transactionId = :transactionId")
    suspend fun updateTransactionStatus(transactionId: String, status: String, updatedAt: Long)
    
    @Query("DELETE FROM transactions WHERE transactionId = :transactionId")
    suspend fun deleteTransaction(transactionId: String)
}
