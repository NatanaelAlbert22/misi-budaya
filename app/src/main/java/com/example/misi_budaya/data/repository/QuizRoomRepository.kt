package com.example.misi_budaya.data.repository

import com.example.misi_budaya.data.model.QuizPackage
import kotlinx.coroutines.flow.Flow

interface QuizRoomRepository {
    fun getAllQuizPackages(): Flow<List<QuizPackage>>

    suspend fun insertAll(vararg quizPackages: QuizPackage)
}
