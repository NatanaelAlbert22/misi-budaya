package com.example.misi_budaya.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.misi_budaya.util.location.LocationData
import com.example.misi_budaya.util.location.LocationService

/**
 * Debug Component untuk menampilkan lokasi pemain saat ini
 * Gunakan ini untuk testing fitur location
 */
@Composable
fun LocationDebugCard(
    locationService: LocationService,
    modifier: Modifier = Modifier,
    onStartTracking: () -> Unit = {},
    onStopTracking: () -> Unit = {}
) {
    val currentLocation by locationService.currentLocation.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Text(
                text = "📍 DEBUG: Lokasi Pemain",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00BCD4)
            )

            // Location Data
            if (currentLocation != null) {
                LocationDataDisplay(currentLocation!!, clipboardManager)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2D2D2D), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⏳ Menunggu sinyal GPS...",
                        fontSize = 12.sp,
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Status: GPS sedang mencari sinyal",
                        fontSize = 11.sp,
                        color = Color(0xFF90CAF9)
                    )
                    Text(
                        text = "Tips: Buka app Maps terlebih dahulu atau ubah lokasi di emulator",
                        fontSize = 10.sp,
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Control Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartTracking,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text("Mulai", fontSize = 12.sp)
                }
                Button(
                    onClick = onStopTracking,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text("Henti", fontSize = 12.sp)
                }
            }

            // Info Text
            Text(
                text = "💡 Tip: Gunakan Android Emulator > Extended Controls > Location untuk simulasi lokasi",
                fontSize = 10.sp,
                color = Color(0xFF90CAF9),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LocationDataDisplay(
    location: LocationData,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D2D2D), shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LocationDataRow(
            label = "Latitude",
            value = "%.6f".format(location.latitude),
            clipboardManager
        )

        LocationDataRow(
            label = "Longitude",
            value = "%.6f".format(location.longitude),
            clipboardManager
        )

        LocationDataRow(
            label = "Akurasi",
            value = "%.1f m".format(location.accuracy),
            clipboardManager
        )
    }
}

@Composable
private fun LocationDataRow(
    label: String,
    value: String,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF90CAF9)
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(
            onClick = {
                clipboardManager.setText(AnnotatedString(value))
            },
            modifier = Modifier
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "Copy",
                tint = Color(0xFF90CAF9),
                modifier = Modifier
            )
        }
    }
}

/**
 * Testing Card untuk menampilkan pengecekan lokasi spesifik
 */
@Composable
fun LocationCheckTestCard(
    playerLat: Double?,
    playerLon: Double?,
    targetLocationName: String,
    targetLat: Double,
    targetLon: Double,
    radius: Float,
    locationService: LocationService,
    modifier: Modifier = Modifier
) {
    if (playerLat == null || playerLon == null) {
        return
    }

    val distance = locationService.calculateDistance(playerLat, playerLon, targetLat, targetLon)
    val isWithinRadius = distance <= radius

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWithinRadius) Color(0xFF1B5E20) else Color(0xFF3E2C27)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🧪 Testing: $targetLocationName",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isWithinRadius) Color(0xFF81C784) else Color(0xFFEF9A9A)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TestDataRow("Target Lat", "%.6f".format(targetLat))
                TestDataRow("Target Lon", "%.6f".format(targetLon))
                TestDataRow("Radius", "%.0f m".format(radius))
                TestDataRow("Jarak Pemain", "%.2f m".format(distance))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isWithinRadius) Color(0xFF2E7D32) else Color(0xFFC62828),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isWithinRadius) "✅ DALAM RADIUS" else "❌ DILUAR RADIUS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TestDataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF90CAF9)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Bold
        )
    }
}
