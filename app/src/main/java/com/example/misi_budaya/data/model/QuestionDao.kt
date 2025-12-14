package com.example.misi_budaya.data.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE quizPackageName = :quizName")
    suspend fun getQuestionsForQuiz(quizName: String): List<Question>

    @Query("SELECT COUNT(*) FROM questions WHERE quizPackageName = :quizName")
    suspend fun countQuestionsForQuiz(quizName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<Question>)

    @Query("DELETE FROM questions")
    suspend fun clearQuestions()
}
