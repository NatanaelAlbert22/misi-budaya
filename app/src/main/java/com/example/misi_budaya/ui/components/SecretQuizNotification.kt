package com.example.misi_budaya.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Notification untuk menampilkan quiz yang baru di-unlock
 */
@Composable
fun SecretQuizUnlockedNotification(
    quizName: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF1B5E20),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Unlock icon
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF4CAF50),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unlocked",
                        tint = Color.White,
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Text content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🎉 Quiz Unlocked!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        text = "Anda telah membuka: $quizName",
                        fontSize = 12.sp,
                        color = Color(0xFFE8F5E9),
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        text = "📍 Dikunjungi dari lokasi yang tepat!",
                        fontSize = 10.sp,
                        color = Color(0xFFC8E6C9),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * Nearby Quiz Card untuk menampilkan quiz yang dekat dengan lokasi user
 */
@Composable
fun NearbySecretQuizCard(
    quizName: String,
    distanceMeters: Float,
    radiusMeters: Float,
    modifier: Modifier = Modifier
) {
    val isNearby = distanceMeters <= radiusMeters
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (isNearby) Color(0xFF2E7D32) else Color(0xFF424242),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🔓 $quizName",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isNearby) Color(0xFF81C784) else Color(0xFFBDBDBD)
                )
                
                if (isNearby) {
                    Text(
                        text = "✅ Unlockable!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Jarak: ${"%.0f".format(distanceMeters)} m (Radius: ${"%.0f".format(radiusMeters)} m)",
                fontSize = 11.sp,
                color = if (isNearby) Color(0xFFC8E6C9) else Color(0xFF9E9E9E)
            )
        }
    }
}
