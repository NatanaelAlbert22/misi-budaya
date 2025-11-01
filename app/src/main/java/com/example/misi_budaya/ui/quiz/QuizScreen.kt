package com.example.misi_budaya.ui.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.misi_budaya.data.model.Paket
import com.example.misi_budaya.data.repository.QuizRepository

@Composable
fun QuizScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(false) }
    var quizPacks by remember { mutableStateOf<List<Paket>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presenter = remember { QuizPresenter(QuizRepository()) }

    val view = remember(navController) {
        object : QuizContract.View {
            override fun showLoading() {
                isLoading = true
            }

            override fun hideLoading() {
                isLoading = false
            }

            override fun showQuizPacks(paketList: List<Paket>) {
                quizPacks = paketList
                errorMessage = null
            }

            override fun showError(message: String) {
                errorMessage = message
            }

            override fun navigateToQuestions(paketId: String) {
                navController.navigate("question_screen/$paketId")
            }
        }
    }

    DisposableEffect(presenter) {
        presenter.onAttach(view)
        onDispose { presenter.onDetach() }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Pilih Paket Soal", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text(text = errorMessage!!)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(quizPacks) { pack ->
                        QuizPackItem(pack = pack, onClick = { presenter.onPaketClicked(pack) })
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizPackItem(pack: Paket, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(pack.namaPaket, style = MaterialTheme.typography.titleLarge)
        }
    }
}
