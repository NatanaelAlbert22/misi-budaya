package com.example.misi_budaya.data.model

import com.google.firebase.firestore.PropertyName

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val email: String = "",

    @get:PropertyName("totalScore")
    @set:PropertyName("totalScore")
    var totalScore: Long = 0, // Use Long for Firestore numbers

    @get:PropertyName("quizScores")
    @set:PropertyName("quizScores")
    var quizScores: Map<String, Long> = emptyMap(), // Use Long for Firestore numbers

    @get:PropertyName("unlockedQuizzes")
    @set:PropertyName("unlockedQuizzes")
    var unlockedQuizzes: Map<String, Boolean> = emptyMap()
) {
    // Empty constructor required by Firebase
    constructor() : this("", "", "", 0L, emptyMap(), emptyMap())
}
