package com.example.misi_budaya.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insertLocation(location: Location): Long

    @Update
    suspend fun updateLocation(location: Location)

    @Delete
    suspend fun deleteLocation(location: Location)

    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getLocationById(id: Int): Location?

    @Query("SELECT * FROM locations WHERE isActive = 1 ORDER BY name")
    fun getAllActiveLocations(): Flow<List<Location>>

    @Query("SELECT * FROM locations WHERE quizPackageName = :packageName")
    suspend fun getLocationsByPackageName(packageName: String): List<Location>

    @Query("SELECT * FROM locations")
    suspend fun getAllLocations(): List<Location>

    @Query("DELETE FROM locations WHERE id = :id")
    suspend fun deleteLocationById(id: Int)
}
