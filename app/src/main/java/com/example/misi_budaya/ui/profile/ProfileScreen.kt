package com.example.misi_budaya.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.misi_budaya.data.local.AppDatabase
import com.example.misi_budaya.data.local.UserPreferencesManager
import com.example.misi_budaya.util.NetworkMonitor
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(rootNavController: NavController) {
    val context = LocalContext.current
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val preferencesManager = remember { UserPreferencesManager(context) }
    val db = remember { AppDatabase.getDatabase(context) }
    val quizRepository = remember { com.example.misi_budaya.data.repository.QuizRepository(db.quizPackageDao(), db.questionDao()) }
    val networkMonitor = remember { NetworkMonitor(context) }
    val userRepository = remember { com.example.misi_budaya.data.repository.UserRepository() }
    val scope = rememberCoroutineScope()
    
    val isOfflineMode by preferencesManager.isOfflineModeFlow.collectAsState(initial = false)
    val isOnline by networkMonitor.observeNetworkStatus().collectAsState(initial = false)
    
    var showOnlineDialog by remember { mutableStateOf(false) }
    var showLoginRequiredDialog by remember { mutableStateOf(false) }
    var showEditUsernameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    
    // User profile state dengan rememberSaveable
    var currentUsername by rememberSaveable { mutableStateOf("") }
    var userEmail by rememberSaveable { mutableStateOf("") }
    var isLoadingProfile by rememberSaveable { mutableStateOf(true) }
    var isGoogleUser by rememberSaveable { mutableStateOf(false) }
    var hasLoadedProfile by rememberSaveable { mutableStateOf(false) }
    
    // Load user profile - HANYA SEKALI
    LaunchedEffect(Unit) {
        if (hasLoadedProfile) return@LaunchedEffect // Skip jika sudah pernah load
        
        if (currentUser != null) {
            userEmail = currentUser.email ?: ""
            
            // Cek apakah user login dengan Google
            val providerData = currentUser.providerData
            isGoogleUser = providerData.any { it.providerId == "google.com" }
            
            if (!isOfflineMode) {
                userRepository.getUserProfile(currentUser.uid).fold(
                    onSuccess = { profile ->
                        currentUsername = profile.username
                        isLoadingProfile = false
                        hasLoadedProfile = true
                    },
                    onFailure = {
                        currentUsername = userEmail.split("@").firstOrNull() ?: ""
                        isLoadingProfile = false
                        hasLoadedProfile = true
                    }
                )
            } else {
                currentUsername = userEmail.split("@").firstOrNull() ?: ""
                isLoadingProfile = false
                hasLoadedProfile = true
            }
        } else {
            isLoadingProfile = false
            hasLoadedProfile = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Profile dengan Avatar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar dengan gradient border
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF64B5F6),
                                        Color(0xFF81C784)
                                    )
                                )
                            ),
                        shape = CircleShape
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(92.dp),
                                shape = CircleShape,
                                color = Color(0xFFE3F2FD)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color(0xFF64B5F6),
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .size(52.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentUser != null) {
                    if (isLoadingProfile) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF64B5F6)
                        )
                    } else {
                        Text(
                            text = currentUsername.ifEmpty { "User" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E2E2E)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userEmail,
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    }
                } else {
                    Text(
                        text = "Guest User",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E2E2E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Not logged in",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Card untuk status koneksi dan mode
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isOnline) Color(0xFF4CAF50).copy(alpha = 0.15f) 
                               else Color(0xFFE57373).copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isOnline) Color(0xFF4CAF50) else Color(0xFFE57373),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Status Koneksi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOnline) "Online" else "Offline",
                            fontSize = 14.sp,
                            color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFE57373),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Switch offline mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode Offline",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOfflineMode) "Aplikasi berjalan offline" 
                                  else "Aplikasi berjalan online",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = isOfflineMode,
                        onCheckedChange = { checked ->
                            if (!checked && !isOnline) {
                                showOnlineDialog = true
                            } else if (!checked && currentUser == null) {
                                showLoginRequiredDialog = true
                            } else {
                                scope.launch {
                                    preferencesManager.setOfflineMode(checked)
                                    // If switching from offline to online, sync scores immediately
                                    if (isOfflineMode && !checked && currentUser != null) {
                                        try {
                                            val ok = com.example.misi_budaya.util.NetworkActivityGuard.waitForAuthToFinish()
                                            if (ok) quizRepository.syncScoresForUser(currentUser.uid)
                                            else android.util.Log.w("ProfileScreen", "Skipping sync due to auth in progress")
                                        } catch (e: Exception) {
                                            android.util.Log.e("ProfileScreen", "Sync failed", e)
                                        }
                                    }
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (currentUser != null) {
            // User Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Informasi Akun",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    if (isLoadingProfile) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        // Username Row
                        ProfileInfoRow(
                            icon = Icons.Default.Person,
                            iconColor = Color(0xFF64B5F6),
                            label = "Username",
                            value = currentUsername.ifEmpty { "Belum diatur" },
                            onEdit = if (!isOfflineMode) ({ showEditUsernameDialog = true }) else null
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Email Row
                        ProfileInfoRow(
                            icon = Icons.Default.Email,
                            iconColor = Color(0xFF81C784),
                            label = "Email",
                            value = userEmail,
                            onEdit = null
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Login Method Row
                        ProfileInfoRow(
                            icon = if (isGoogleUser) Icons.Default.AccountCircle else Icons.Default.CheckCircle,
                            iconColor = if (isGoogleUser) Color(0xFFE57373) else Color(0xFFBA68C8),
                            label = "Metode Login",
                            value = if (isGoogleUser) "Google Account" else "Email & Password",
                            onEdit = null
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info cards
            if (isOfflineMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF9C4)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Mode Offline: Edit username dan ubah password tidak tersedia. Aktifkan mode online untuk menggunakan fitur ini.",
                            fontSize = 13.sp,
                            color = Color(0xFF616161),
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (isGoogleUser) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "ℹ️",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Akun Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E2E2E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Anda login dengan Google. Untuk mengubah password, gunakan pengaturan akun Google Anda.",
                                fontSize = 13.sp,
                                color = Color(0xFF616161),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Action Buttons
            if (!isGoogleUser) {
                OutlinedButton(
                    onClick = { showChangePasswordDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !isOfflineMode,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF64B5F6)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Ubah Password",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Logout Button
            Button(
                onClick = {
                    scope.launch {
                        // Clear previous user so next offline session is treated as anonymous
                        preferencesManager.clearPreviousUser()
                        // Clear local scores because after logout offline should not retain account scores
                        quizRepository.clearLocalScores()
                        FirebaseAuth.getInstance().signOut()
                        // Navigate to login on main thread
                        rootNavController.navigate("login") {
                            popUpTo(rootNavController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE57373)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // Not logged in card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "👤",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum Login",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E2E2E)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Login untuk menggunakan mode online dan sinkronisasi data",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // Dialog ketika tidak ada koneksi
    if (showOnlineDialog) {
        AlertDialog(
            onDismissRequest = { showOnlineDialog = false },
            title = { Text("Tidak Ada Koneksi") },
            text = { Text("Tidak dapat beralih ke mode online karena tidak ada koneksi internet.") },
            confirmButton = {
                TextButton(onClick = { showOnlineDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Dialog ketika belum login
    if (showLoginRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showLoginRequiredDialog = false },
            title = { Text("Login Diperlukan") },
            text = { Text("Anda perlu login untuk menggunakan mode online.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLoginRequiredDialog = false
                        rootNavController.navigate("login")
                    }
                ) {
                    Text("Login")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginRequiredDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
    
    // Dialog Edit Username
    if (showEditUsernameDialog) {
        EditUsernameDialog(
            currentUsername = currentUsername,
            onDismiss = { showEditUsernameDialog = false },
            onSave = { newUsername ->
                scope.launch {
                    currentUser?.uid?.let { uid ->
                        userRepository.updateUsername(uid, newUsername).fold(
                            onSuccess = {
                                currentUsername = newUsername
                                showEditUsernameDialog = false
                                Toast.makeText(
                                    context,
                                    "Username berhasil diubah!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onFailure = { error ->
                                Toast.makeText(
                                    context,
                                    "Gagal mengubah username: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                }
            }
        )
    }
    
    // Dialog Change Password
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onSave = { currentPassword, newPassword ->
                scope.launch {
                    userRepository.updatePassword(currentPassword, newPassword).fold(
                        onSuccess = {
                            showChangePasswordDialog = false
                            Toast.makeText(
                                context,
                                "Password berhasil diubah!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(
                                context,
                                "Gagal mengubah password: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    onEdit: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        onEdit?.let {
            IconButton(onClick = it) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EditUsernameDialog(
    currentUsername: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newUsername by remember { mutableStateOf(currentUsername) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Edit Username") },
        text = {
            Column {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { 
                        newUsername = it
                        errorMessage = ""
                    },
                    label = { Text("Username Baru") },
                    singleLine = true,
                    isError = errorMessage.isNotEmpty(),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        newUsername.isBlank() -> errorMessage = "Username tidak boleh kosong"
                        newUsername.length < 3 -> errorMessage = "Username minimal 3 karakter"
                        newUsername.length > 20 -> errorMessage = "Username maksimal 20 karakter"
                        newUsername == currentUsername -> errorMessage = "Username sama dengan sebelumnya"
                        else -> {
                            isLoading = true
                            onSave(newUsername)
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSave: (currentPassword: String, newPassword: String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Ubah Password") },
        text = {
            Column {
                // Current Password
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { 
                        currentPassword = it
                        errorMessage = ""
                    },
                    label = { Text("Password Saat Ini") },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = if (showCurrentPassword) 
                        VisualTransformation.None 
                    else 
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                            Icon(
                                imageVector = if (showCurrentPassword) 
                                    Icons.Default.Visibility 
                                else 
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (showCurrentPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // New Password
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        errorMessage = ""
                    },
                    label = { Text("Password Baru") },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = if (showNewPassword) 
                        VisualTransformation.None 
                    else 
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                imageVector = if (showNewPassword) 
                                    Icons.Default.Visibility 
                                else 
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (showNewPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        errorMessage = ""
                    },
                    label = { Text("Konfirmasi Password Baru") },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = if (showConfirmPassword) 
                        VisualTransformation.None 
                    else 
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) 
                                    Icons.Default.Visibility 
                                else 
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    isError = errorMessage.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        currentPassword.isBlank() -> errorMessage = "Password saat ini tidak boleh kosong"
                        newPassword.isBlank() -> errorMessage = "Password baru tidak boleh kosong"
                        newPassword.length < 6 -> errorMessage = "Password minimal 6 karakter"
                        newPassword != confirmPassword -> errorMessage = "Password tidak cocok"
                        currentPassword == newPassword -> errorMessage = "Password baru harus berbeda"
                        else -> {
                            isLoading = true
                            onSave(currentPassword, newPassword)
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Batal")
            }
        }
    )
}
