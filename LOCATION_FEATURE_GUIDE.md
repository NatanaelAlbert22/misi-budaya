## Panduan Implementasi Fitur Location-Based Quiz

Fitur ini memungkinkan pemain mendapatkan paket soal khusus ketika mereka berada di lokasi tertentu.

### Arsitektur Komponen

```
LocationService
├─ Tracking lokasi real-time
├─ Menghitung jarak
└─ Pengecekan radius

LocationRepository
├─ Mengelola data lokasi
├─ Mengecheck lokasi pemain
└─ Matching location dengan quiz package

QuizPackage (Extended)
├─ isLocationBased: Boolean
├─ requiredLocationId: Int?
└─ unlockedAtLocation: Boolean
```

### Setup Database

Database Room secara otomatis akan menambahkan tabel `locations` dengan schema:

```sql
CREATE TABLE locations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    radiusInMeters REAL NOT NULL,
    quizPackageName TEXT NOT NULL,
    isActive INTEGER NOT NULL
)
```

### 1️⃣ Inisialisasi LocationService

Di Activity atau ViewModel Anda:

```kotlin
import com.google.android.gms.location.LocationServices
import com.example.misi_budaya.util.location.LocationService

class YourActivity : AppCompatActivity() {
    private lateinit var locationService: LocationService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationService = LocationService(this, fusedLocationClient)
        
        // Mulai tracking lokasi
        locationService.startLocationUpdates()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Hentikan tracking
        locationService.stopLocationUpdates()
    }
}
```

### 2️⃣ Request Location Permissions

```kotlin
import androidx.activity.result.contract.ActivityResultContracts
import com.example.misi_budaya.util.location.LocationPermissionHelper

class YourActivity : AppCompatActivity() {
    private lateinit var permissionHelper: LocationPermissionHelper
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        permissionHelper = LocationPermissionHelper(this)
        
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissionHelper.hasLocationPermission()) {
                // Permission granted, mulai tracking lokasi
                locationService.startLocationUpdates()
            }
        }
        
        // Request permission
        permissionHelper.requestLocationPermissions(permissionLauncher)
    }
}
```

### 3️⃣ Menambah Lokasi Baru

```kotlin
import com.example.misi_budaya.data.model.Location
import com.example.misi_budaya.data.repository.LocationRepository

// Menambah lokasi baru
val newLocation = Location(
    name = "Museum Budaya Indonesia",
    description = "Museum dengan koleksi artefak budaya",
    latitude = -6.1751,
    longitude = 106.8267,
    radiusInMeters = 200f,  // 200 meter
    quizPackageName = "Paket_Budaya_Nusantara",
    isActive = true
)

viewModelScope.launch {
    locationRepository.addLocation(newLocation)
}
```

### 4️⃣ Membuat Quiz Package dengan Location

```kotlin
import com.example.misi_budaya.data.model.QuizPackage

val locationBasedPackage = QuizPackage(
    name = "Paket_Budaya_Nusantara",
    description = "Paket soal tentang budaya Indonesia",
    isLocationBased = true,           // 🔑 Aktifkan location-based
    requiredLocationId = 1,            // ID lokasi yang diperlukan
    unlockedAtLocation = false         // Belum di-unlock
)

// Simpan ke database
quizRepository.insertQuizPackage(locationBasedPackage)
```

### 5️⃣ Mengecek Lokasi Pemain

```kotlin
// Di ViewModel atau Presenter
viewModelScope.launch {
    // Cek apakah pemain berada di lokasi manapun
    val matchedLocation = locationRepository.checkCurrentLocationMatch()
    
    if (matchedLocation != null) {
        // Pemain berada di lokasi tertentu!
        // Unlock paket soal yang terkait
        val packageName = matchedLocation.quizPackageName
        val package = quizRepository.getQuizPackageByName(packageName)
        
        package?.let {
            val unlockedPackage = it.copy(unlockedAtLocation = true)
            quizRepository.updateQuizPackage(unlockedPackage)
            
            // Tampilkan notifikasi
            showNotification("Paket soal baru terbuka: ${unlockedPackage.name}")
        }
    }
}
```

### 6️⃣ Menampilkan Jarak ke Lokasi

```kotlin
// Mendapatkan informasi jarak dari pemain ke lokasi
viewModelScope.launch {
    val locationsWithDistance = locationRepository.getLocationsWithDistance(
        packageName = "Paket_Budaya_Nusantara"
    )
    
    for (locationData in locationsWithDistance) {
        val location = locationData.location
        val distance = locationData.distanceInMeters
        val isNearby = locationData.isWithinRadius
        
        println("${location.name}: ${distance.toInt()}m - ${if(isNearby) "DEKAT" else "JAUH"}")
    }
}
```

### 7️⃣ Menggunakan Components di UI

Di Compose:

```kotlin
import com.example.misi_budaya.ui.components.LocationStatusCard
import com.example.misi_budaya.ui.components.LocationLockedPackageCard

@Composable
fun MyScreen(
    currentLocation: Location?,
    distance: Float?,
    isWithinRadius: Boolean,
    quizPackage: QuizPackage
) {
    Column {
        // Tampilkan status lokasi pemain
        LocationStatusCard(
            location = currentLocation,
            distance = distance,
            isWithinRadius = isWithinRadius,
            modifier = Modifier.padding(16.dp)
        )
        
        // Tampilkan paket soal dengan status kunci lokasi
        LocationLockedPackageCard(
            packageName = quizPackage.name,
            packageDescription = quizPackage.description,
            isLocationBased = quizPackage.isLocationBased,
            isUnlocked = quizPackage.unlockedAtLocation,
            locationName = currentLocation?.name,
            distance = distance,
            modifier = Modifier.padding(16.dp)
        )
    }
}
```

### 8️⃣ Helper Extension Functions

Gunakan extension functions yang sudah dibuat:

```kotlin
// Check apakah pemain bisa akses paket berdasarkan lokasi
val canAccess = quizPackage.canAccessBasedOnLocation(locationRepository)

// Dapatkan lokasi terkait dengan paket
val relatedLocations = quizPackage.getRelatedLocations(locationRepository)
```

### 9️⃣ Contoh Usecase Lengkap

```kotlin
class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Mulai tracking lokasi
            locationRepository.startLocationTracking()
            
            // Load semua paket soal
            quizRepository.getAllQuizPackages().collect { packages ->
                val enrichedPackages = packages.map { package ->
                    val canAccess = package.canAccessBasedOnLocation(locationRepository)
                    
                    if (package.isLocationBased && !package.unlockedAtLocation) {
                        // Paket ini location-based dan belum di-unlock
                        val locations = package.getRelatedLocations(locationRepository)
                        
                        // Tampilkan info jarak jika ada lokasi terkait
                        locations.forEach { location ->
                            val distance = locationRepository
                                .getLocationsWithDistance(package.name)
                                .firstOrNull()?.distanceInMeters
                        }
                    }
                    
                    package
                }
                
                _uiState.value = QuizUiState.Success(enrichedPackages)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Hentikan tracking saat ViewModel di-destroy
        locationRepository.stopLocationTracking()
    }
}
```

### 🔟 Testing Location

Untuk testing di emulator Android:

1. Buka Android Studio's **Extended Controls** (emulator)
2. Pergi ke **Location**
3. Set latitude dan longitude
4. Klik **Set Location**

Atau gunakan adb command:

```bash
adb emu geo fix -6.1751 106.8267  # Jakarta
```

### Catatan Penting

- ✅ Location tracking hanya berjalan saat app aktif atau foreground
- ✅ Untuk background location, diperlukan foreground service
- ✅ Pastikan permissions sudah di-request sebelum mulai tracking
- ✅ Hentikan tracking di `onDestroy()` untuk menghemat baterai
- ✅ Gunakan `StateFlow` untuk real-time updates

---

Pertanyaan atau bantuan lebih lanjut? Silakan tanyakan! 🚀
