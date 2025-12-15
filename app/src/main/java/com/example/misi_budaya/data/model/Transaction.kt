package com.example.misi_budaya.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val transactionId: String = UUID.randomUUID().toString(),
    val userId: String,
    val amount: Long, // amount in cents (e.g., 100000 for 1,000 IDR)
    val description: String,
    val status: String = "pending", // pending, success, failed, expired
    val paymentMethod: String? = null, // credit_card, bank_transfer, etc
    val midtransToken: String? = null,
    val midtransOrderId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class TransactionRequest(
    val userId: String,
    val amount: Long,
    val description: String,
    val email: String,
    val firstName: String,
    val lastName: String? = null,
    val phone: String? = null
)

data class MidtransResponse(
    val transactionId: String,
    val orderId: String,
    val token: String,
    val redirectUrl: String
)
