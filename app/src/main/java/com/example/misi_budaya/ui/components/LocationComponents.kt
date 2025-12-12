package com.example.misi_budaya.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.misi_budaya.data.model.Location

/**
 * Component untuk menampilkan status lokasi pemain
 */
@Composable
fun LocationStatusCard(
    location: Location?,
    distance: Float? = null,
    isWithinRadius: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        location == null -> Color(0xFFE0E0E0)
        isWithinRadius -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        else -> Color(0xFFFFC107).copy(alpha = 0.1f)
    }

    val textColor = when {
        location == null -> Color.Gray
        isWithinRadius -> Color(0xFF2E7D32)
        else -> Color(0xFFF57F17)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Location",
                    tint = textColor,
                    modifier = Modifier.then(Modifier.padding(0.dp))
                )

                if (location != null) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = location.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = location.description,
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Text(
                        text = "Lokasi tidak terdeteksi",
                        fontSize = 14.sp,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (location != null && distance != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jarak: %.0f m".format(distance),
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.8f)
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isWithinRadius) Color(0xFF4CAF50) else Color(0xFFFFC107),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isWithinRadius) "Dalam Area" else "Diluar Area",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Component untuk menampilkan status paket soal yang terkunci/terbuka karena lokasi
 */
@Composable
fun LocationLockedPackageCard(
    packageName: String,
    packageDescription: String,
    isLocationBased: Boolean,
    isUnlocked: Boolean,
    locationName: String? = null,
    distance: Float? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF4CAF50).copy(alpha = 0.1f)
            else Color(0xFFEF5350).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    contentDescription = if (isUnlocked) "Unlocked" else "Locked",
                    tint = if (isUnlocked) Color(0xFF2E7D32) else Color(0xFFC62828),
                    modifier = Modifier
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = packageName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = packageDescription,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            if (isLocationBased && locationName != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "📍 Memerlukan lokasi: $locationName",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )

                    distance?.let {
                        Text(
                            text = "Jarak: %.0f m".format(it),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            if (!isUnlocked && isLocationBased) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFEF5350), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Kunjungi lokasi untuk membuka paket soal ini",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
