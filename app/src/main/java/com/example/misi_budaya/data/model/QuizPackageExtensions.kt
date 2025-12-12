package com.example.misi_budaya.data.model

import com.example.misi_budaya.data.repository.LocationRepository

/**
 * Extension functions untuk QuizPackage yang berhubungan dengan lokasi
 */

/**
 * Mengecek apakah pemain dapat mengakses paket soal ini berdasarkan lokasi
 */
suspend fun QuizPackage.canAccessBasedOnLocation(
    locationRepository: LocationRepository
): Boolean {
    // Jika paket tidak memerlukan pengecekan lokasi
    if (!isLocationBased) {
        return true
    }

    // Jika sudah di-unlock sebelumnya
    if (unlockedAtLocation) {
        return true
    }

    // Cek apakah pemain berada di lokasi yang diperlukan
    requiredLocationId?.let { locationId ->
        return locationRepository.isPlayerAtLocation(locationId)
    }

    return false
}

/**
 * Mengecek lokasi mana saja yang terkait dengan paket soal ini
 */
suspend fun QuizPackage.getRelatedLocations(
    locationRepository: LocationRepository
): List<Location> {
    if (!isLocationBased) {
        return emptyList()
    }

    return requiredLocationId?.let { locationId ->
        locationRepository.getLocationById(locationId)?.let { location ->
            listOf(location)
        } ?: emptyList()
    } ?: emptyList()
}
