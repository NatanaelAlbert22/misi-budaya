package com.example.misi_budaya.util.location

import com.example.misi_budaya.data.model.Location
import com.example.misi_budaya.data.repository.LocationRepository

/**
 * Helper untuk testing location feature
 * Gunakan ini untuk menambah sample locations ke database
 */
object LocationTestHelper {
    /**
     * Sample locations untuk testing
     * Koordinat-koordinat ini bisa diganti dengan lokasi nyata atau lokasi yang ingin di-test
     */
    fun getSampleLocations(): List<Location> {
        return listOf(
            // Bandung City Center
            Location(
                id = 1,
                name = "Bandung City Center",
                description = "Pusat kota Bandung - Jalan Braga",
                latitude = -6.9175,
                longitude = 107.6062,
                radiusInMeters = 500f,
                quizPackageName = "Budaya Bandung",
                isActive = true
            ),
            // Tangkuban Perahu
            Location(
                id = 2,
                name = "Tangkuban Perahu",
                description = "Gunung berapi aktif di Bandung",
                latitude = -6.7735,
                longitude = 107.5739,
                radiusInMeters = 200f,
                quizPackageName = "Alam Jawa Barat",
                isActive = true
            ),
            // Kawah Putih
            Location(
                id = 3,
                name = "Kawah Putih",
                description = "Kawah vulkanik dengan air berwarna putih",
                latitude = -7.1667,
                longitude = 107.3333,
                radiusInMeters = 300f,
                quizPackageName = "Keajaiban Alam",
                isActive = true
            ),
            // Gedung Sate
            Location(
                id = 4,
                name = "Gedung Sate",
                description = "Gedung bersejarah Bandung",
                latitude = -6.9012,
                longitude = 107.6117,
                radiusInMeters = 100f,
                quizPackageName = "Sejarah Bandung",
                isActive = true
            ),
            // Terusan Tol Cileunyi
            Location(
                id = 5,
                name = "Exit Cileunyi",
                description = "Area rest area Terusan Tol Cileunyi",
                latitude = -6.9347,
                longitude = 107.6883,
                radiusInMeters = 150f,
                quizPackageName = "Infrastruktur Jawa Barat",
                isActive = true
            )
        )
    }

    /**
     * Insert sample locations ke database untuk testing
     */
    suspend fun insertSampleLocations(locationRepository: LocationRepository) {
        val sampleLocations = getSampleLocations()
        for (location in sampleLocations) {
            locationRepository.addLocation(location)
        }
    }

    /**
     * Testing location dengan koordinat spesifik
     */
    suspend fun testLocationMatch(
        locationRepository: LocationRepository,
        testLat: Double,
        testLon: Double
    ): Location? {
        val allLocations = locationRepository.getAllLocations()
        val locationService = LocationService(
            context = null as Any as android.content.Context,
            fusedLocationClient = null as Any as com.google.android.gms.location.FusedLocationProviderClient
        )

        for (location in allLocations) {
            val distance = locationService.calculateDistance(
                lat1 = testLat,
                lon1 = testLon,
                lat2 = location.latitude,
                lon2 = location.longitude
            )

            if (distance <= location.radiusInMeters && location.isActive) {
                return location
            }
        }

        return null
    }

    /**
     * Get semua locations yang paling dekat dengan koordinat yang diberikan
     */
    suspend fun getNearestLocation(
        locationRepository: LocationRepository,
        testLat: Double,
        testLon: Double
    ): Pair<Location, Float>? {
        val allLocations = locationRepository.getAllLocations()
        if (allLocations.isEmpty()) return null

        val locationService = LocationService(
            context = null as Any as android.content.Context,
            fusedLocationClient = null as Any as com.google.android.gms.location.FusedLocationProviderClient
        )

        var nearestLocation: Location? = null
        var minDistance = Float.MAX_VALUE

        for (location in allLocations) {
            val distance = locationService.calculateDistance(
                lat1 = testLat,
                lon1 = testLon,
                lat2 = location.latitude,
                lon2 = location.longitude
            )

            if (distance < minDistance) {
                minDistance = distance
                nearestLocation = location
            }
        }

        return nearestLocation?.let { it to minDistance }
    }
}
