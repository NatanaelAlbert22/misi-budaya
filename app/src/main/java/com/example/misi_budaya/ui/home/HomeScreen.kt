package com.example.misi_budaya.ui.home

import android.Manifest
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.misi_budaya.R
import com.example.misi_budaya.data.local.AppDatabase
import com.example.misi_budaya.data.local.UserPreferencesManager
import com.example.misi_budaya.data.model.QuizPackage
import com.example.misi_budaya.data.repository.UserRepository
import com.example.misi_budaya.service.LocationAwareQuizService
import com.example.misi_budaya.ui.components.OnlineModeDialog
import com.example.misi_budaya.ui.components.SecretQuizUnlockedNotification
import com.example.misi_budaya.util.NetworkMonitor
import com.example.misi_budaya.util.location.LocationPermissionHelper
import com.example.misi_budaya.util.location.LocationService
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

data class QuizCategory(
    val name: String,
    val iconUrl: String,
    val color: Color,
    val completedQuestions: Int,
    val totalQuestions: Int,
    val categoryId: String
)

@Composable
fun HomeScreen(
    navController: NavController,
    isDarkTheme: Boolean = false,
    onThemeChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val networkMonitor = remember { NetworkMonitor(context) }
    val preferencesManager = remember { UserPreferencesManager(context) }
    val auth = remember { FirebaseAuth.getInstance() }
    val userRepository = remember { UserRepository() }
    val db = remember { AppDatabase.getDatabase(context) }
    
    // Initialize LocationService
    val locationService = remember {
        LocationService(
            context = context,
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        )
    }
    
    // Initialize LocationAwareQuizService untuk monitoring secret quiz
    val currentUser = auth.currentUser
    val quizAwareService = remember {
        if (currentUser != null) {
            LocationAwareQuizService(
                context = context,
                locationService = locationService,
                userId = currentUser.uid
            )
        } else {
            null
        }
    }
    
    val isOnline by networkMonitor.observeNetworkStatus().collectAsState(initial = false)
    val isOfflineMode by preferencesManager.isOfflineModeFlow.collectAsState(initial = false)
    val hasSeenPrompt by preferencesManager.hasSeenOnlinePromptFlow.collectAsState(initial = false)
    
    var showOnlineModeDialog by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(LocationPermissionHelper.hasLocationPermission(context)) }
    var unlockedQuizzes by remember { mutableStateOf<List<String>>(emptyList()) }
    var lastUnlockedQuiz by remember { mutableStateOf<String?>(null) }
    
    // Permission launcher untuk location
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.all { it.value }
        if (granted) {
            hasLocationPermission = true
            locationService.startLocationUpdates()
        }
    }
    

    
    // Start LocationAwareQuizService saat screen dibuka (real-time monitoring)
    LaunchedEffect(Unit) {
        if (quizAwareService != null && hasLocationPermission) {
            // Load unlocked quizzes dari Firestore dulu
            quizAwareService.loadUnlockedQuizzes()
            // Tunggu sebentar untuk memastikan loading selesai
            delay(500)
            // Sync initial state dengan yang sudah di-load di service
            unlockedQuizzes = quizAwareService.unlockedQuizzes
            // Setelah itu baru start monitoring
            quizAwareService.startMonitoring()
        }
    }
    
    // Cleanup LocationAwareQuizService saat screen ditutup
    DisposableEffect(Unit) {
        onDispose {
            quizAwareService?.stopMonitoring()
        }
    }
    
    // Listen untuk new unlocked quiz events dari LocationAwareQuizService
    LaunchedEffect(quizAwareService) {
        if (quizAwareService != null) {
            quizAwareService.newUnlockedQuizEvent.collect { quizName ->
                lastUnlockedQuiz = quizName
                unlockedQuizzes = quizAwareService.unlockedQuizzes
                android.util.Log.d("HomeScreen", "🎉 New quiz unlocked: $quizName")
            }
        }
    }
    
    // Detect ketika online dan masih dalam offline mode
    LaunchedEffect(isOnline, isOfflineMode, hasSeenPrompt) {
        if (isOnline && isOfflineMode && !hasSeenPrompt) {
            // wait a short time to allow preference writes (e.g. markOnlinePromptSeen after login)
            // to propagate before we show the dialog. This avoids a race where the dialog
            // appears immediately after login even though we just marked the prompt as seen.
            delay(1200L)
            if (isOnline && isOfflineMode && !hasSeenPrompt) {
                showOnlineModeDialog = true
            }
        }
    }
    
    // State untuk user data - fetch dari Firestore dengan rememberSaveable
    var userName by rememberSaveable { mutableStateOf("") }
    var userLevel by rememberSaveable { mutableStateOf(1) }
    var userXP by rememberSaveable { mutableStateOf(0) }
    var isLoadingUser by rememberSaveable { mutableStateOf(true) }
    var hasLoadedUser by rememberSaveable { mutableStateOf(false) }
    var unlockedSecretQuizzes by remember { mutableStateOf<List<QuizPackage>>(emptyList()) }
    var completedMissions by remember { mutableStateOf(3) }
    var totalMissions by remember { mutableStateOf(5) }
    var dailyCompletedPackages by remember { mutableStateOf(0) }
    var quizPackages by remember { mutableStateOf<List<QuizPackage>>(emptyList()) }
    
    // Load daily completed packages dan check apakah perlu reset
    val dailyCompletedPackagesCollect by preferencesManager.dailyPackageCountFlow.collectAsState(initial = 0)
    
    // Load quiz packages dari database - gunakan collectAsState untuk reactive updates
    val quizPackagesFromDb by db.quizPackageDao().getAllQuizPackages().collectAsState(initial = emptyList())
    quizPackages = quizPackagesFromDb
    
    // Trigger refresh dari Firebase saat HomeScreen pertama kali dibuka
    // Ini memastikan data packages selalu up-to-date
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val quizRepository = com.example.misi_budaya.data.repository.QuizRepository(
                    db.quizPackageDao(),
                    db.questionDao()
                )
                // Refresh data packages dari Firebase dan simpan ke local database
                quizRepository.refreshPaketList()
                android.util.Log.d("HomeScreen", "Refreshed quiz packages from Firebase")
            } catch (e: Exception) {
                android.util.Log.w("HomeScreen", "Failed to refresh quiz packages: ${e.message}")
                // Tetap lanjut dengan local data jika refresh gagal
            }
        }
    }
    
    // Check apakah perlu reset daily counter
    LaunchedEffect(Unit) {
        val today = java.time.LocalDate.now().toString() // Format: YYYY-MM-DD
        val prefs = context.getSharedPreferences("daily_reset", Context.MODE_PRIVATE)
        val lastResetDate = prefs.getString("last_reset_date", "")
        
        // Jika hari berbeda dengan last reset date, reset counter
        if (lastResetDate != today) {
            scope.launch {
                preferencesManager.resetDailyPackageCount()
                prefs.edit().putString("last_reset_date", today).apply()
            }
        }
    }
    
    // Update dailyCompletedPackages state from flow
    LaunchedEffect(dailyCompletedPackagesCollect) {
        dailyCompletedPackages = dailyCompletedPackagesCollect
    }
    
    // Pre-load pertanyaan untuk semua kategori quiz saat HomeScreen dibuka
    // Ini memastikan soal sudah ter-cache ketika user membuka quiz
    LaunchedEffect(quizPackages) {
        if (quizPackages.isNotEmpty()) {
            scope.launch {
                try {
                    val quizRepository = com.example.misi_budaya.data.repository.QuizRepository(
                        db.quizPackageDao(),
                        db.questionDao()
                    )
                    
                    // Pre-load pertanyaan untuk setiap kategori quiz
                    val categoryNames = listOf("Pakaian Adat", "Makanan Khas", "Geografi", "Kesenian")
                    for (categoryName in categoryNames) {
                        try {
                            // Load dari local database dulu, jika kosong load dari Firebase
                            quizRepository.getSoalList(categoryName, forceRefresh = false)
                            android.util.Log.d("HomeScreen", "Pre-loaded questions for $categoryName")
                        } catch (e: Exception) {
                            android.util.Log.w("HomeScreen", "Failed to pre-load questions for $categoryName", e)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HomeScreen", "Error in pre-loading questions", e)
                }
            }
        }
    }
    
    // Fetch user profile dari Firestore - HANYA SEKALI
    LaunchedEffect(Unit) {
        if (hasLoadedUser) return@LaunchedEffect // Skip jika sudah pernah load
        
        val user = auth.currentUser
        val uid = user?.uid
        
        if (uid == null) {
            // Tidak ada user login
            userName = "Guest"
            isLoadingUser = false
            hasLoadedUser = true
            return@LaunchedEffect
        }
        
        if (isOfflineMode) {
            // Offline mode - tampilkan dari email
            userName = user.email?.split("@")?.firstOrNull() ?: user.displayName ?: "User"
            isLoadingUser = false
            hasLoadedUser = true
            return@LaunchedEffect
        }
        
        // Online mode - fetch dari Firestore
        isLoadingUser = true
        userRepository.getUserProfile(uid).fold(
            onSuccess = { profile ->
                userName = profile.username.ifEmpty { 
                    user.displayName ?: user.email?.split("@")?.firstOrNull() ?: "User" 
                }
                // Calculate level dari total score
                userLevel = (profile.totalScore / 1000).toInt() + 1
                userXP = profile.totalScore.toInt()
                isLoadingUser = false
                hasLoadedUser = true
            },
            onFailure = { error ->
                // Profile tidak ada, buat profile baru dengan username dari email
                val defaultUsername = user.displayName 
                    ?: user.email?.split("@")?.firstOrNull() 
                    ?: "User"
                    
                userRepository.createInitialProfile(user, defaultUsername).fold(
                    onSuccess = {
                        userName = defaultUsername
                        isLoadingUser = false
                        hasLoadedUser = true
                    },
                    onFailure = {
                        userName = defaultUsername
                        isLoadingUser = false
                        hasLoadedUser = true
                    }
                )
            }
        )
    }
    
    // Progress data
    val dailyProgress = dailyCompletedPackages.toFloat() / 4f // Total 4 kategori quiz
    
    // Kategori Quiz - built from database packages
    val quizCategories = remember(quizPackages) {
        val categoryColorMap = mapOf(
            "Pakaian Adat" to Color(0xFFE57373),
            "Makanan Khas" to Color(0xFF64B5F6),
            "Geografi" to Color(0xFF81C784),
            "Kesenian" to Color(0xFFBA68C8)
        )
        
        quizPackages
            .filter { it.name in listOf("Pakaian Adat", "Makanan Khas", "Geografi", "Kesenian") }
            .map { pkg ->
                QuizCategory(
                    name = pkg.name,
                    iconUrl = pkg.iconUrl,
                    color = categoryColorMap[pkg.name] ?: Color(0xFF9E9E9E),
                    completedQuestions = 10,
                    totalQuestions = 10,
                    categoryId = pkg.name
                )
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Secret Quiz Unlocked Notification
        SecretQuizUnlockedNotification(
            quizName = lastUnlockedQuiz ?: "",
            isVisible = lastUnlockedQuiz != null,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Auto-hide notification after 5 seconds
        if (lastUnlockedQuiz != null) {
            LaunchedEffect(lastUnlockedQuiz) {
                delay(5000)
                lastUnlockedQuiz = null
            }
        }
        // Header Card dengan info user
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF8E1) // Warna krem seperti di gambar
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Icon + Name + Level/XP
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo aplikasi
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo Misi Budaya",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Name, Level & XP
                    Column {
                        Text(
                            text = userName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E2E2E)
                        )
                        Text(
                            text = "Level $userLevel • ${userXP.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")} XP",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                // Right side: Dark Mode Toggle + Avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dark Mode Toggle
                    IconButton(
                        onClick = { onThemeChange(!isDarkTheme) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = if (isDarkTheme) Color(0xFFFFA000) else Color(0xFF424242),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Avatar
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color(0xFFE0E0E0)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Avatar",
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Progress Harian Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header: Progress Harian + Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress Harian",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E2E2E)
                    )
                    Text(
                        text = "$dailyCompletedPackages/4 Paket",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B35) // Orange
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Progress Bar dengan gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE8E8E8))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(dailyProgress)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFF9800), // Orange
                                        Color(0xFF4CAF50)  // Green
                                    )
                                )
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Kategori Quiz Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Kategori Quiz",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E2E2E)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Grid kategori quiz (2 kolom)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(quizCategories) { category ->
                    QuizCategoryCard(
                        category = category,
                        onClick = {
                            // Navigate to description screen first
                            navController.navigate("quiz_description/${category.categoryId}")
                        }
                    )
                }
            }
        }
    }
    
    // Dialog untuk switch ke online mode
    if (showOnlineModeDialog) {
        OnlineModeDialog(
            onDismiss = {
                showOnlineModeDialog = false
                scope.launch {
                    preferencesManager.markOnlinePromptSeen()
                }
            },
            onStayOffline = {
                showOnlineModeDialog = false
                scope.launch {
                    preferencesManager.markOnlinePromptSeen()
                }
            },
            onSwitchToOnline = {
                showOnlineModeDialog = false
                scope.launch {
                    preferencesManager.markOnlinePromptSeen()
                    
                    // Cek apakah user sudah login
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        // Sudah login, langsung switch ke online
                        val ok = com.example.misi_budaya.util.NetworkUtil.waitForInternet()
                        if (ok) {
                            preferencesManager.setOfflineMode(false)
                        } else {
                            // Inform user that connection not yet stable
                            android.util.Log.w("HomeScreen", "User requested online but internet not reachable")
                            // keep offline; optionally show a toast
                        }
                    } else {
                        // Belum login, arahkan ke login
                        // Tapi tetap di home dulu, nanti user bisa login dari profile
                        // Atau bisa langsung navigate ke login jika mau
                        val ok = com.example.misi_budaya.util.NetworkUtil.waitForInternet()
                        if (ok) {
                            preferencesManager.setOfflineMode(false)
                        } else {
                            android.util.Log.w("HomeScreen", "User requested online but internet not reachable")
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun QuizCategoryCard(
    category: QuizCategory,
    onClick: () -> Unit
) {
    val progress = category.completedQuestions.toFloat() / category.totalQuestions.toFloat()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon dengan background warna - dari URL atau fallback
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = category.color
            ) {
                if (category.iconUrl.isNotEmpty()) {
                    AsyncImage(
                        model = category.iconUrl,
                        contentDescription = category.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text("📦", fontSize = 28.sp)
                    }
                }
            }
            
            // Category name
            Text(
                text = category.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E2E2E)
            )
            
            // Progress info
            Column {
                Text(
                    text = "${category.completedQuestions} Teka-teki",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE8E8E8))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(category.color)
                    )
                }
            }
        }
    }
}
