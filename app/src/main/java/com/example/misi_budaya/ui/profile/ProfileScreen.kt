package com.example.misi_budaya.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.misi_budaya.data.local.UserPreferencesManager
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
    val scope = rememberCoroutineScope()
    
    val isOfflineMode by preferencesManager.isOfflineModeFlow.collectAsState(initial = false)
    val isOnline by networkMonitor.observeNetworkStatus().collectAsState(initial = false)
    
    var showOnlineDialog by remember { mutableStateOf(false) }
    var showLoginRequiredDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                                // User mau online tapi tidak ada koneksi
                                showOnlineDialog = true
                            } else if (!checked && currentUser == null) {
                                // User mau online tapi belum login
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
        
        Spacer(modifier = Modifier.height(24.dp))

        if (currentUser != null) {
            Text("Email: ${currentUser.email}")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                FirebaseAuth.getInstance().signOut()
                scope.launch {
                    preferencesManager.setOfflineMode(true)
                }
                rootNavController.navigate("login") {
                    popUpTo(rootNavController.graph.startDestinationId) { inclusive = true }
                }
            }) {
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
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showOnlineDialog = false },
            title = { Text("Tidak Ada Koneksi") },
            text = { Text("Tidak dapat beralih ke mode online karena tidak ada koneksi internet.") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showOnlineDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    // Dialog ketika belum login
    if (showLoginRequiredDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLoginRequiredDialog = false },
            title = { Text("Login Diperlukan") },
            text = { Text("Anda perlu login untuk menggunakan mode online.") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showLoginRequiredDialog = false
                        rootNavController.navigate("login")
                    }
                ) {
                    Text("Login")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showLoginRequiredDialog = false }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}
