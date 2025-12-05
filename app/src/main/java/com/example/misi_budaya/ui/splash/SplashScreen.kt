package com.example.misi_budaya.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.misi_budaya.R
import com.example.misi_budaya.data.local.UserPreferencesManager
import com.example.misi_budaya.util.NetworkMonitor
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(navController: NavController) {
    val alpha = remember { Animatable(0f) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val networkMonitor = remember { NetworkMonitor(context) }
    val preferencesManager = remember { UserPreferencesManager(context) }

    LaunchedEffect(key1 = true) {
        // Fade in animation
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
        
        // Wait for 2 seconds
        delay(2000)
        
        // Cek status koneksi internet
        val isOnline = networkMonitor.isOnline()
        val isOfflineMode = preferencesManager.isOfflineModeFlow.first()
        val currentUser = auth.currentUser
        
        when {
            // Jika offline (no internet) -> langsung ke home dalam mode offline
            !isOnline -> {
                preferencesManager.setOfflineMode(true)
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            
            // Jika online -> set ke online mode (kecuali user manual pilih offline)
            isOnline -> {
                // Prioritas: selalu set online jika ada internet
                // User bisa manual ubah di ProfileScreen nanti
                preferencesManager.setOfflineMode(false)
                
                if (currentUser != null) {
                    // User sudah login -> ke home
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                } else {
                    // User belum login -> ke login
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1)), // Warna krem seperti di app
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha.value)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo Misi Budaya",
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Name
            Text(
                text = "Misi Budaya",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E2E2E)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            Text(
                text = "Jelajahi Warisan Indonesia",
                fontSize = 16.sp,
                color = Color(0xFF757575)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading indicator
            CircularProgressIndicator(
                color = Color(0xFF6B8E23),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
