package com.example.misi_budaya.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.misi_budaya.data.local.AppDatabase
import com.example.misi_budaya.data.model.Soal
import com.example.misi_budaya.data.repository.QuizRepository

@Composable
fun QuestionScreen(navController: NavController, quizPackId: String?) {
    var isLoading by remember { mutableStateOf(true) }
    var currentSoal by remember { mutableStateOf<Soal?>(null) }
    var questionNumber by remember { mutableStateOf(0) }
    var totalQuestions by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val (selectedOption, onOptionSelected) = remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { QuizRepository(db.quizPackageDao()) }
    val scope = rememberCoroutineScope()
    val presenter = remember { QuestionPresenter(repository, scope) }

    val view = remember(navController) {
        object : QuestionContract.View {
            override fun showLoading() {
                isLoading = true
            }

            override fun hideLoading() {
                isLoading = false
            }

            override fun showQuestions(soalList: List<Soal>) {}

            override fun showError(message: String) {
                errorMessage = message
            }

            override fun navigateToResult(score: Int) {
                // Navigate to the ResultScreen, replacing the current screen
                // so the user can't go back to the last question.
                navController.navigate("result_screen/$score") {
                    popUpTo(navController.currentDestination?.id ?: 0) { inclusive = true }
                }
            }

            override fun showCurrentQuestion(soal: Soal, number: Int, total: Int) {
                currentSoal = soal
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

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text(text = errorMessage!!)
        } else if (currentSoal != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Pertanyaan $questionNumber / $totalQuestions", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(24.dp))

                Text(currentSoal!!.soal, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                currentSoal!!.pilihan.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (option.id == selectedOption),
                                onClick = {
                                    onOptionSelected(option.id)
                                    presenter.onAnswerSelected(option.id)
                                }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option.id == selectedOption),
                            onClick = { 
                                onOptionSelected(option.id)
                                presenter.onAnswerSelected(option.id)
                            }
                        )
                        Text(
                            text = option.teks,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = { 
                    presenter.onNextOrFinishClicked() 
                }, enabled = selectedOption != null) {
                    val buttonText = if (questionNumber == totalQuestions) "Selesai" else "Selanjutnya"
                    Text(buttonText)
                }
            }
        }
    }
}
