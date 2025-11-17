package com.example.misi_budaya.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizPackageDao {
    @Query("SELECT * FROM quiz_packages")
    fun getAllQuizPackages(): Flow<List<QuizPackage>>

    @Query("SELECT * FROM quiz_packages WHERE name = :name")
    suspend fun getQuizPackageByName(name: String): QuizPackage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg quizPackages: QuizPackage)

    @Update
    suspend fun updateQuizPackage(quizPackage: QuizPackage)

    @Delete
    suspend fun delete(quizPackage: QuizPackage)
}
