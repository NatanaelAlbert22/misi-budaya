package com.example.misi_budaya.data.repository

import com.example.misi_budaya.data.model.Location
import com.example.misi_budaya.data.model.LocationDao
import com.example.misi_budaya.util.location.LocationService
import kotlinx.coroutines.flow.Flow

class LocationRepository(
    private val locationDao: LocationDao,
    private val locationService: LocationService
) {
    // Location Management
    suspend fun addLocation(location: Location): Long {
        return locationDao.insertLocation(location)
    }

    suspend fun updateLocation(location: Location) {
        locationDao.updateLocation(location)
    }

    suspend fun deleteLocation(location: Location) {
        locationDao.deleteLocation(location)
    }

    suspend fun deleteLocationById(id: Int) {
        locationDao.deleteLocationById(id)
    }

    suspend fun getLocationById(id: Int): Location? {
        return locationDao.getLocationById(id)
    }

    fun getAllActiveLocations(): Flow<List<Location>> {
        return locationDao.getAllActiveLocations()
    }

    suspend fun getAllLocations(): List<Location> {
        return locationDao.getAllLocations()
    }

    suspend fun getLocationsByPackageName(packageName: String): List<Location> {
        return locationDao.getLocationsByPackageName(packageName)
    }

    // Location Checking
    /**
     * Mengecek apakah pemain saat ini berada di dalam area lokasi apapun
     * Mengembalikan Location jika pemain berada di dalamnya, null jika tidak
     */
    suspend fun checkCurrentLocationMatch(): Location? {
        val currentLoc = locationService.currentLocation.value ?: return null
        val locations = locationDao.getAllLocations()

        for (location in locations) {
            if (location.isActive) {
                val isInRadius = locationService.isLocationWithinRadius(
                    playerLat = currentLoc.latitude,
                    playerLon = currentLoc.longitude,
                    targetLat = location.latitude,
                    targetLon = location.longitude,
                    radiusInMeters = location.radiusInMeters
                )

                if (isInRadius) {
                    return location
                }
            }
        }

        return null
    }

    /**
     * Mengecek apakah pemain berada di lokasi tertentu
     */
    suspend fun isPlayerAtLocation(locationId: Int): Boolean {
        val currentLoc = locationService.currentLocation.value ?: return false
        val location = locationDao.getLocationById(locationId) ?: return false

        return locationService.isLocationWithinRadius(
            playerLat = currentLoc.latitude,
            playerLon = currentLoc.longitude,
            targetLat = location.latitude,
            targetLon = location.longitude,
            radiusInMeters = location.radiusInMeters
        )
    }

    /**
     * Mendapatkan daftar lokasi yang cocok dengan paket soal tertentu
     * dan mengecek jarak pemain dari setiap lokasi
     */
    suspend fun getLocationsWithDistance(
        packageName: String
    ): List<LocationWithDistance> {
        val currentLoc = locationService.currentLocation.value ?: return emptyList()
        val locations = locationDao.getLocationsByPackageName(packageName)

        return locations.map { location ->
            val distance = locationService.calculateDistance(
                lat1 = currentLoc.latitude,
                lon1 = currentLoc.longitude,
                lat2 = location.latitude,
                lon2 = location.longitude
            )

            LocationWithDistance(
                location = location,
                distanceInMeters = distance,
                isWithinRadius = distance <= location.radiusInMeters
            )
        }
    }

    fun startLocationTracking() {
        locationService.startLocationUpdates()
    }

    fun stopLocationTracking() {
        locationService.stopLocationUpdates()
    }

    // Data class untuk hasil pengecekan lokasi dengan jarak
    data class LocationWithDistance(
        val location: Location,
        val distanceInMeters: Float,
        val isWithinRadius: Boolean
    )
}
