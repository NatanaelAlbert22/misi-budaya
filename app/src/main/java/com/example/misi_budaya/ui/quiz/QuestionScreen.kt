package com.example.misi_budaya.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.misi_budaya.data.local.AppDatabase
import com.example.misi_budaya.data.model.Question
import com.example.misi_budaya.data.repository.QuizRepository

@Composable
fun QuestionScreen(navController: NavController, quizPackId: String?) {
    var isLoading by remember { mutableStateOf(true) }
    var currentQuestion by remember { mutableStateOf<Question?>(null) }
    var questionNumber by remember { mutableStateOf(0) }
    var totalQuestions by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val (selectedOption, onOptionSelected) = remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { QuizRepository(db.quizPackageDao(), db.questionDao()) }
    val scope = rememberCoroutineScope()
    val presenter = remember { QuestionPresenter(repository, scope) }

    // Mapping kategori ke warna
    val categoryColor = remember(quizPackId) {
        when (quizPackId) {
            "Pakaian Adat" -> Color(0xFFE57373)
            "Makanan Khas" -> Color(0xFF64B5F6)
            "Geografi" -> Color(0xFF81C784)
            "Kesenian" -> Color(0xFFBA68C8)
            else -> Color(0xFF9E9E9E)
        }
    }

    val view = remember(navController) {
        object : QuestionContract.View {
            override fun showLoading() {
                isLoading = true
            }

            override fun hideLoading() {
                isLoading = false
            }

            override fun showQuestions(questionList: List<Question>) {}

            override fun showError(message: String) {
                errorMessage = message
            }

            override fun navigateToResult(score: Int) {
                navController.navigate("result_screen/$score/$quizPackId") {
                    popUpTo(navController.currentDestination?.id ?: 0) { inclusive = true }
                }
            }

            override fun showCurrentQuestion(question: Question, number: Int, total: Int) {
                currentQuestion = question
                questionNumber = number
                totalQuestions = total
                onOptionSelected(null) // Reset selection for the new question
            }
        }
    }

    DisposableEffect(presenter) {
        presenter.onAttach(view)
        onDispose { presenter.onDetach() }
    }

    LaunchedEffect(quizPackId) {
        if (quizPackId != null) {
            presenter.loadQuestions(quizPackId)
        }
    }

    // Listen for questionsDownloaded events and reload if the current pack was just downloaded
    LaunchedEffect(Unit) {
        com.example.misi_budaya.util.AppEvents.questionsDownloaded.collect {
            // Force reload from local DB (questions should now be available)
            if (quizPackId != null) {
                presenter.loadQuestions(quizPackId)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        categoryColor.copy(alpha = 0.1f),
                        Color.White
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                CircularProgressIndicator(color = categoryColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Memuat soal...", color = Color(0xFF757575))
            }
        } else if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Oops!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE57373)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        fontSize = 16.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        } else if (currentQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header dengan progress
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pertanyaan $questionNumber dari $totalQuestions",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF757575)
                            )
                            Text(
                                text = "${((questionNumber.toFloat() / totalQuestions.toFloat()) * 100).toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress bar
                        LinearProgressIndicator(
                            progress = questionNumber.toFloat() / totalQuestions.toFloat(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = categoryColor,
                            trackColor = Color(0xFFE8E8E8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Card pertanyaan
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = currentQuestion!!.questionText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E2E2E),
                            lineHeight = 28.sp
                        )

                        // Tampilkan gambar soal jika ada
                        if (currentQuestion!!.questionImageUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                AsyncImage(
                                    model = currentQuestion!!.questionImageUrl,
                                    contentDescription = "Gambar Soal",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Pilihan jawaban
                currentQuestion!!.choices.forEachIndexed { index, option ->
                    val isSelected = option.id == selectedOption
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = {
                                    onOptionSelected(option.id)
                                    presenter.onAnswerSelected(option.id)
                                }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) categoryColor.copy(alpha = 0.1f) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isSelected) 4.dp else 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom radio button dengan border
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) categoryColor else Color(0xFFBDBDBD),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(
                                        color = if (isSelected) categoryColor else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                color = Color.White,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.padding(horizontal = 12.dp))

                            // Tampilkan gambar pilihan jika ada, kalau tidak ada tampilkan teks
                            if (option.gambar.isNotEmpty()) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    AsyncImage(
                                        model = option.gambar,
                                        contentDescription = "Gambar Pilihan ${option.id}",
                                        modifier = Modifier.size(100.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                Text(
                                    text = option.teks,
                                    fontSize = 16.sp,
                                    color = if (isSelected) Color(0xFF2E2E2E) else Color(0xFF616161),
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol next/finish
                Button(
                    onClick = {
                        presenter.onNextOrFinishClicked()
                    },
                    enabled = selectedOption != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = categoryColor,
                        disabledContainerColor = Color(0xFFE0E0E0)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    val buttonText = if (questionNumber == totalQuestions) "Selesai" else "Lanjut"
                    Text(
                        text = buttonText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
