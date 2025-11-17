package com.example.misi_budaya.data.repository

import com.example.misi_budaya.data.model.QuizPackage
import com.example.misi_budaya.data.model.QuizPackageDao
import kotlinx.coroutines.flow.Flow

class QuizRoomRepositoryImpl(private val quizPackageDao: QuizPackageDao) : QuizRoomRepository {

    override fun getAllQuizPackages(): Flow<List<QuizPackage>> {
        return quizPackageDao.getAllQuizPackages()
    }

    override suspend fun insertAll(vararg quizPackages: QuizPackage) {
        quizPackageDao.insertAll(*quizPackages)
    }
}
