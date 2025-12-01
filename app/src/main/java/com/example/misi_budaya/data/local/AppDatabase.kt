package com.example.misi_budaya.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.misi_budaya.data.model.Question
import com.example.misi_budaya.data.model.QuestionDao
import com.example.misi_budaya.data.model.QuizPackage
import com.example.misi_budaya.data.model.QuizPackageDao

@Database(entities = [QuizPackage::class, Question::class], version = 6, exportSchema = false) // Incremented version
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizPackageDao(): QuizPackageDao
    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "misi_budaya_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
