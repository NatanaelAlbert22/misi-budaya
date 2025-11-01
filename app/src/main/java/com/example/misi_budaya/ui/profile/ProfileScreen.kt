package com.example.misi_budaya.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(rootNavController: NavController) {
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (currentUser != null) {
            Text("Email: ${currentUser.email}")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                FirebaseAuth.getInstance().signOut()
                rootNavController.navigate("login") {
                    // Clear the entire back stack
                    popUpTo(rootNavController.graph.startDestinationId) { inclusive = true }
                }
            }) {
                Text("Logout")
            }
        } else {
            Text("Not logged in")
        }
    }
}
