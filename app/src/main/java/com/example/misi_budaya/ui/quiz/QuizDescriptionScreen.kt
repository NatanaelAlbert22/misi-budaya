package com.example.misi_budaya.ui.quiz

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.misi_budaya.data.local.AppDatabase
import com.example.misi_budaya.data.repository.QuizRepository

@Composable
fun QuizDescriptionScreen(navController: NavController, quizPackId: String?) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { QuizRepository(db.quizPackageDao(), db.questionDao()) }

    var description by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Mapping kategori ke warna dan icon
    val categoryInfo = remember(quizPackId) {
        when (quizPackId) {
            "Pakaian Adat" -> Triple(Color(0xFFE57373), Icons.Default.Home, "Pakaian Adat")
            "Makanan Khas" -> Triple(Color(0xFF64B5F6), Icons.Default.Fastfood, "Makanan Khas")
            "Geografi" -> Triple(Color(0xFF81C784), Icons.Default.Map, "Geografi")
            "Kesenian" -> Triple(Color(0xFFBA68C8), Icons.Default.LocalActivity, "Kesenian")
            else -> Triple(Color(0xFF9E9E9E), Icons.Default.Home, quizPackId ?: "Quiz")
        }
    }

    LaunchedEffect(quizPackId) {
        if (quizPackId != null) {
            isLoading = true
            val paket = repository.getPaketByName(quizPackId)
            description = paket?.deskripsi ?: "Deskripsi tidak tersedia."
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        categoryInfo.first.copy(alpha = 0.15f),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header dengan back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color(0xFF2E2E2E)
                    )
                }
                Text(
                    text = "Detail Quiz",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E2E2E),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card dengan icon kategori
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon dengan background warna
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = categoryInfo.first
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryInfo.second,
                                contentDescription = categoryInfo.third,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Nama kategori
                    Text(
                        text = categoryInfo.third,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E2E2E)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(3.dp)
                            .background(
                                categoryInfo.first,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Deskripsi
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = categoryInfo.first,
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        Text(
                            text = description ?: "Deskripsi tidak tersedia.",
                            fontSize = 16.sp,
                            color = Color(0xFF616161),
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tombol mulai dengan desain modern
            Button(
                onClick = { navController.navigate("question_screen/$quizPackId") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = categoryInfo.first
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Text(
                    text = "Mulai Quiz",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol kembali dengan outline style
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Text(
                    text = "Kembali",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = categoryInfo.first
                )
            }
        }
    }
}
