package com.example.misi_budaya.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi

/**
 * Dialog untuk menanyakan user apakah mau switch ke online mode atau tetap offline
 */
@Composable
fun OnlineModeDialog(
    onDismiss: () -> Unit,
    onStayOffline: () -> Unit,
    onSwitchToOnline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Internet Connection"
            )
        },
        title = {
            Text(text = "Koneksi Internet Terdeteksi")
        },
        text = {
            Text(
                text = "Koneksi internet tersedia. Apakah Anda ingin beralih ke mode online untuk mengakses fitur lengkap dan menyimpan progress ke cloud?"
            )
        },
        confirmButton = {
            TextButton(
                onClick = onSwitchToOnline
            ) {
                Text("Beralih ke Online")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onStayOffline
            ) {
                Text("Tetap Offline")
            }
        }
    )
}
