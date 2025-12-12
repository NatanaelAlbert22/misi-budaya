package com.example.misi_budaya.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val radiusInMeters: Float = 100f, // Radius pengecekan lokasi dalam meter
    val quizPackageName: String, // Nama paket soal yang akan dibuka saat di lokasi ini
    val isActive: Boolean = true
)
