package com.example.misi_budaya.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_packages")
data class QuizPackage(
    @PrimaryKey
    val name: String,
    val description: String,
    val isCompleted: Boolean = false,
    val score: Int = 0,
    // Secret quiz fields
    val isSecret: Boolean = false, // Apakah paket ini adalah secret quiz
    val isUnlocked: Boolean = false, // Apakah secret quiz sudah di-unlock oleh user
    // Location-based fields
    val isLocationBased: Boolean = false, // Apakah paket ini memerlukan pengecekan lokasi
    val requiredLocationId: Int? = null, // ID lokasi yang diperlukan untuk mengakses paket ini
    val unlockedAtLocation: Boolean = false // Flag apakah paket sudah di-unlock karena pemain berada di lokasi
)
