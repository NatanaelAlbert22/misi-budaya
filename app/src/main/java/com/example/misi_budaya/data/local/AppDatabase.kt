package com.example.misi_budaya.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.misi_budaya.data.model.QuizPackage
import com.example.misi_budaya.data.model.QuizPackageDao

@Database(entities = [QuizPackage::class], version = 3) // Version incremented again
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizPackageDao(): QuizPackageDao

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
                .fallbackToDestructiveMigration() // Destroys and recreates the DB if schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
