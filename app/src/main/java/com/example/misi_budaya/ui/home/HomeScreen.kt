package com.example.misi_budaya.ui.home

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.misi_budaya.R
import com.example.misi_budaya.data.local.UserPreferencesManager
import com.example.misi_budaya.data.repository.UserRepository
import com.example.misi_budaya.ui.components.OnlineModeDialog
import com.example.misi_budaya.util.NetworkMonitor
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class QuizCategory(
    val name: String,
    val icon: ImageVector,
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
    
    val isOnline by networkMonitor.observeNetworkStatus().collectAsState(initial = false)
    val isOfflineMode by preferencesManager.isOfflineModeFlow.collectAsState(initial = false)
    val hasSeenPrompt by preferencesManager.hasSeenOnlinePromptFlow.collectAsState(initial = false)
    
    var showOnlineModeDialog by remember { mutableStateOf(false) }
    
    // Detect ketika online dan masih dalam offline mode
    LaunchedEffect(isOnline, isOfflineMode, hasSeenPrompt) {
        if (isOnline && isOfflineMode && !hasSeenPrompt) {
            showOnlineModeDialog = true
        }
    }
    
    // State untuk user data - fetch dari Firestore
    var userName by remember { mutableStateOf("Guest") }
    var userLevel by remember { mutableStateOf(1) }
    var userXP by remember { mutableStateOf(0) }
    var isLoadingUser by remember { mutableStateOf(true) }
    
    // Fetch user profile dari Firestore
    LaunchedEffect(auth.currentUser?.uid) {
        val uid = auth.currentUser?.uid
        if (uid != null && !isOfflineMode) {
            isLoadingUser = true
            userRepository.getUserProfile(uid).fold(
                onSuccess = { profile ->
                    userName = profile.username.ifEmpty { "User" }
                    // Calculate level dari total score
                    userLevel = (profile.totalScore / 1000).toInt() + 1
                    userXP = profile.totalScore.toInt()
                    isLoadingUser = false
                },
                onFailure = {
                    userName = auth.currentUser?.email?.split("@")?.firstOrNull() ?: "User"
                    isLoadingUser = false
                }
            )
        } else if (uid != null && isOfflineMode) {
            // Offline mode - tampilkan dari email
            userName = auth.currentUser?.email?.split("@")?.firstOrNull() ?: "Guest"
            isLoadingUser = false
        } else {
            userName = "Guest"
            isLoadingUser = false
        }
    }
    
    // Progress data
    var completedMissions by remember { mutableStateOf(3) }
    var totalMissions by remember { mutableStateOf(5) }
    val progress = completedMissions.toFloat() / totalMissions.toFloat()
    
    // Kategori Quiz
    val quizCategories = remember {
        listOf(
            QuizCategory(
                name = "Pakaian Adat",
                icon = Icons.Default.Home,
                color = Color(0xFFE57373),
                completedQuestions = 15,
                totalQuestions = 24,
                categoryId = "Pakaian Adat"
            ),
            QuizCategory(
                name = "Makanan Khas",
                icon = Icons.Default.Fastfood,
                color = Color(0xFF64B5F6),
                completedQuestions = 18,
                totalQuestions = 24,
                categoryId = "Makanan Khas"
            ),
            QuizCategory(
                name = "Geografi",
                icon = Icons.Default.Map,
                color = Color(0xFF81C784),
                completedQuestions = 12,
                totalQuestions = 24,
                categoryId = "Geografi"
            ),
            QuizCategory(
                name = "Kesenian",
                icon = Icons.Default.LocalActivity,
                color = Color(0xFFBA68C8),
                completedQuestions = 10,
                totalQuestions = 24,
                categoryId = "Kesenian"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                        text = "$completedMissions/$totalMissions Misi",
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
                            .fillMaxWidth(progress)
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
                            // Navigate ke soal dengan kategori ID
                            navController.navigate("question_screen/${category.categoryId}")
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
                        preferencesManager.setOfflineMode(false)
                    } else {
                        // Belum login, arahkan ke login
                        // Tapi tetap di home dulu, nanti user bisa login dari profile
                        // Atau bisa langsung navigate ke login jika mau
                        preferencesManager.setOfflineMode(false)
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
            // Icon dengan background warna
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = category.color
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.name,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
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
