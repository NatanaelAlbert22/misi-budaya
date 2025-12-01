package com.example.misi_budaya.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// This is now the single source of truth for the Pilihan model.
// It matches the Firebase structure and is used by Room via a TypeConverter.
data class Pilihan(
    var id: String = "",
    var teks: String = "",
    var gambar: String = "" // Matches the field in Firebase
) {
    // Empty constructor required by Firebase for deserialization
    constructor() : this("", "", "")
}

@Entity(
    tableName = "questions",
    foreignKeys = [ForeignKey(
        entity = QuizPackage::class,
        parentColumns = ["name"],
        childColumns = ["quizPackageName"],
        onDelete = ForeignKey.CASCADE // If a quiz package is deleted, delete its questions.
    )],
    indices = [Index(value = ["quizPackageName"])]
)
data class Question(
    @PrimaryKey
    val id: String, // Using the ID from Firebase as the primary key
    val quizPackageName: String, // Foreign key to QuizPackage
    val questionText: String,
    val choices: List<Pilihan>,
    val correctAnswerId: String
)
