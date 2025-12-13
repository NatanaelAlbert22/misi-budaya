package com.example.misi_budaya.util.location

import android.Manifest
import android.content.Context
import android.location.Location as AndroidLocation
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float
)

class LocationService(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var isTracking = false

    private companion object {
        private const val TAG = "LocationService"
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    fun startLocationUpdates() {
        if (isTracking) {
            Log.w(TAG, "Location tracking already started")
            return
        }

        Log.d(TAG, "Starting location updates...")
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // Update setiap 5 detik
        )
            .setMinUpdateIntervalMillis(2000L)
            .setMaxUpdateDelayMillis(10000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d(TAG, "Location received: ${location.latitude}, ${location.longitude}, accuracy: ${location.accuracy}")
                    _currentLocation.value = LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy
                    )
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            isTracking = true
            Log.d(TAG, "Location updates request sent successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception: ${e.message}", e)
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        Log.d(TAG, "Stopping location updates...")
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
        isTracking = false
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    suspend fun getLastLocation(): LocationData? {
        return try {
            Log.d(TAG, "Getting last location...")
            val location = fusedLocationClient.lastLocation.result
            location?.let {
                Log.d(TAG, "Last location: ${it.latitude}, ${it.longitude}")
                LocationData(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracy = it.accuracy
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception getting last location: ${e.message}")
            null
        }
    }

    /**
     * Menghitung jarak antara dua titik lokasi dalam meter
     * Menggunakan Haversine formula
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val results = FloatArray(1)
        AndroidLocation.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    /**
     * Mengecek apakah pemain berada di dalam radius lokasi tertentu
     */
    fun isLocationWithinRadius(
        playerLat: Double,
        playerLon: Double,
        targetLat: Double,
        targetLon: Double,
        radiusInMeters: Float
    ): Boolean {
        val distance = calculateDistance(playerLat, playerLon, targetLat, targetLon)
        return distance <= radiusInMeters
    }
}
