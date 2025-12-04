package com.example.misi_budaya.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.misi_budaya.data.local.UserPreferencesManager
import com.example.misi_budaya.data.repository.UserRepository
import com.example.misi_budaya.ui.components.OnlineModeDialog
import com.example.misi_budaya.util.NetworkMonitor
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(rootNavController: NavController) {
    val context = LocalContext.current
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val preferencesManager = remember { UserPreferencesManager(context) }
    val networkMonitor = remember { NetworkMonitor(context) }
    val userRepository = remember { UserRepository() }
    val scope = rememberCoroutineScope()
    
    val isOfflineMode by preferencesManager.isOfflineModeFlow.collectAsState(initial = false)
    val isOnline by networkMonitor.observeNetworkStatus().collectAsState(initial = false)
    
    var showOnlineDialog by remember { mutableStateOf(false) }
    var showLoginRequiredDialog by remember { mutableStateOf(false) }
    var showEditUsernameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    
    // User profile state
    var currentUsername by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var isLoadingProfile by remember { mutableStateOf(true) }
    
    // Load user profile
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            userEmail = currentUser.email ?: ""
            if (!isOfflineMode) {
                userRepository.getUserProfile(currentUser.uid).fold(
                    onSuccess = { profile ->
                        currentUsername = profile.username
                        isLoadingProfile = false
                    },
                    onFailure = {
                        currentUsername = userEmail.split("@").firstOrNull() ?: ""
                        isLoadingProfile = false
                    }
                )
            } else {
                currentUsername = userEmail.split("@").firstOrNull() ?: ""
                isLoadingProfile = false
            }
        } else {
            isLoadingProfile = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(24.dp))

        // Card untuk status koneksi dan mode
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Pengaturan Koneksi",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Status koneksi
                Text(
                    text = "Status: ${if (isOnline) "Online ✓" else "Offline ✗"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOnline) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.error
                )
                
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
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = if (isOfflineMode) "Aplikasi berjalan offline" 
                                  else "Aplikasi berjalan online",
                            style = MaterialTheme.typography.bodySmall,
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
                                }
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (currentUser != null) {
            // User Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Informasi Akun",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isLoadingProfile) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        // Username
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Username",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currentUsername.ifEmpty { "Belum diatur" },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            IconButton(
                                onClick = { showEditUsernameDialog = true },
                                enabled = !isOfflineMode
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Username"
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Email
                        Column {
                            Text(
                                text = "Email",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Change Password Button
            OutlinedButton(
                onClick = { showChangePasswordDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isOfflineMode
            ) {
                Text("Ubah Password")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Logout Button
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    scope.launch {
                        preferencesManager.setOfflineMode(true)
                    }
                    rootNavController.navigate("login") {
                        popUpTo(rootNavController.graph.startDestinationId) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        } else {
            Text("Not logged in")
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Login untuk menggunakan mode online dan sinkronisasi data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
                            },
                            onFailure = { error ->
                                // Handle error - bisa tambahkan toast/snackbar
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
                            // Bisa tambahkan toast/snackbar sukses
                        },
                        onFailure = { error ->
                            // Handle error - tampilkan pesan error
                        }
                    )
                }
            }
        )
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
    
    AlertDialog(
        onDismissRequest = onDismiss,
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
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
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
                        else -> onSave(newUsername)
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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
    
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
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
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
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
                        else -> onSave(currentPassword, newPassword)
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
