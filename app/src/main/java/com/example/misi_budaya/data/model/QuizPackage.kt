package com.example.misi_budaya.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_packages")
data class QuizPackage(
    @PrimaryKey
    val name: String,
    val description: String,
    val isCompleted: Boolean = false,
    val score: Int = 0
)
