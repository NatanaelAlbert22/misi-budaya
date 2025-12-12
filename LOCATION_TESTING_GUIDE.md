# 📍 Panduan Testing Location Feature

## Cara Mengecek Apakah Fitur Location Sudah Bekerja

Ada beberapa cara untuk testing fitur location yang sudah dibuat:

---

## 1️⃣ Menggunakan Debug Components (Recommended)

Saya sudah membuat `LocationDebugCard` yang menampilkan lokasi pemain secara real-time.

### Cara Menggunakan:

```kotlin
// Di HomeScreen atau screen lainnya
val locationService = remember { LocationService(context, fusedLocationClient) }

LaunchedEffect(Unit) {
    locationService.startLocationUpdates()
}

LocationDebugCard(
    locationService = locationService,
    onStartTracking = { locationService.startLocationUpdates() },
    onStopTracking = { locationService.stopLocationUpdates() }
)
```

Apa yang akan ditampilkan:
- ✅ Latitude pemain saat ini
- ✅ Longitude pemain saat ini  
- ✅ Akurasi GPS
- ✅ Button untuk mulai/henti tracking

---

## 2️⃣ Menggunakan Android Emulator Location Simulation

### Setup Emulator:

1. **Buka Android Studio > Device Manager**
2. **Jalankan emulator**
3. **Di emulator, buka Extended Controls** (Ctrl+Shift+U atau icon 3 titik)
4. **Pilih Location**
5. **Masukkan koordinat yang ingin di-test**

### Sample Koordinat untuk Testing:

| Lokasi | Latitude | Longitude |
|--------|----------|-----------|
| Bandung City Center | -6.9175 | 107.6062 |
| Tangkuban Perahu | -6.7735 | 107.5739 |
| Kawah Putih | -7.1667 | 107.3333 |
| Gedung Sate | -6.9012 | 107.6117 |

---

## 3️⃣ Menambah Sample Locations ke Database

Saya sudah membuat helper untuk menambah sample locations:

```kotlin
// Di MainActivity atau screen initialization
val locationRepository = remember { LocationRepository(locationDao, locationService) }

LaunchedEffect(Unit) {
    // Tambah sample locations untuk testing
    LocationTestHelper.insertSampleLocations(locationRepository)
}
```

Ini akan menambahkan 5 lokasi sample ke database.

---

## 4️⃣ Testing Location Check dengan LocationCheckTestCard

```kotlin
val currentLocation by locationService.currentLocation.collectAsState()

// Test apakah pemain berada di Bandung City Center
if (currentLocation != null) {
    LocationCheckTestCard(
        playerLat = currentLocation.latitude,
        playerLon = currentLocation.longitude,
        targetLocationName = "Bandung City Center",
        targetLat = -6.9175,
        targetLon = 107.6062,
        radius = 500f,
        locationService = locationService
    )
}
```

Card ini akan menampilkan:
- ✅ Jarak pemain dari target location
- ✅ Radius yang ditetapkan
- ✅ Status apakah pemain DALAM atau DILUAR radius

---

## 5️⃣ Testing Real Device

Untuk testing di real device, Anda perlu:

### Permission Handling:

```kotlin
// Minta permission saat app launch
val permissionHelper = remember { LocationPermissionHelper(context) }
val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
        locationService.startLocationUpdates()
    }
}

LaunchedEffect(Unit) {
    if (!permissionHelper.hasLocationPermission()) {
        permissionHelper.requestLocationPermissions(permissionLauncher)
    } else {
        locationService.startLocationUpdates()
    }
}
```

---

## 🧪 Checklist Testing

Sebelum launching, pastikan:

- [ ] **Permission Handling**: App minta location permission
- [ ] **Location Updates**: Debug card menampilkan lat/lon yang berubah saat bergerak
- [ ] **Akurasi GPS**: Nilai akurasi menunjukkan +/- meter
- [ ] **Location Matching**: Saat pemain di lokasi sample, status berubah "DALAM RADIUS"
- [ ] **Database**: Sample locations berhasil tersimpan
- [ ] **Real Device**: Testing di real device dengan GPS nyata

---

## 🐛 Debugging Tips

### Jika lokasi tidak terdeteksi:

1. **Cek Permission**: Buka Settings > App > Permissions > Location = "Allow"
2. **Cek Location Service**: Di emulator, pastikan location service aktif
3. **Cek GPS**: Di emulator Extended Controls, pastikan sudah set koordinat
4. **Cek Logcat**: Lihat console apakah ada error SecurityException

### Jika akurasi GPS buruk:

1. Pastikan GPS sudah warm-up (tunggu 30 detik)
2. Di emulator, gunakan "Real data" bukan "Playback data"
3. Di real device, coba di lokasi terbuka (bukan dalam ruangan)

---

## 📊 Testing Report Template

Gunakan ini untuk dokumentasi testing:

```
Testing Date: [DATE]
Device: [DEVICE/EMULATOR]
OS: [ANDROID VERSION]

Location Detection:
- Status: ✅/❌
- Lat/Lon: ____ / ____
- Accuracy: ____ m

Sample Location Test:
- Location: Bandung City Center
- Distance from player: ____ m
- Status (DALAM/DILUAR): ____
- Result: ✅/❌

Permission Test:
- First Request: ✅/❌
- Allow/Deny: ____
- Result: ✅/❌

Issues Found:
- [Issue 1]
- [Issue 2]

Conclusion: [READY/NEEDS FIX]
```

---

## 🔗 Related Files

- `LocationService.kt` - Core service untuk tracking lokasi
- `LocationDebugComponents.kt` - UI components untuk testing
- `LocationTestHelper.kt` - Utility untuk sample data
- `LocationRepository.kt` - Repository untuk database operations
