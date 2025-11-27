package com.example.misi_budaya.ui.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.misi_budaya.data.model.QuizPackage
import com.example.misi_budaya.data.repository.QuizRepository

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun QuizScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(true) } // Start with loading
    var isRefreshing by remember { mutableStateOf(false) }
    var quizPacks by remember { mutableStateOf<List<QuizPackage>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { QuizRepository(db.quizPackageDao()) }
    val scope = rememberCoroutineScope()
    val presenter = remember { QuizPresenter(repository, scope) }

    val view = remember(navController) {
        object : QuizContract.View {
            override fun showLoading() {
                isLoading = true
                isRefreshing = false
            }

            override fun hideLoading() {
                isLoading = false
                isRefreshing = false
            }

            override fun showQuizPacks(paketList: List<QuizPackage>) {
                quizPacks = paketList
                errorMessage = null
            }

            override fun showError(message: String) {
                // Avoid overwriting a valid list with a transient error
                if (quizPacks.isEmpty()) {
                    errorMessage = message
                }
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

    // Pull-to-refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            presenter.loadQuizPacks()
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Pilih Paket Soal", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && quizPacks.isEmpty()) { // Show loading only if there's no data yet
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = errorMessage!!, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            errorMessage = null
                            presenter.loadQuizPacks()
                        }
                    ) {
                        Text("Coba Lagi")
                    }
                }
            } else if (quizPacks.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Tidak ada paket soal yang tersedia.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { presenter.loadQuizPacks() }) {
                        Text("Refresh")
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(quizPacks) { pack ->
                        QuizPackItem(pack = pack, onClick = { presenter.onPaketClicked(pack) })
                    }
                }
            }
        }

        // Pull-to-refresh indicator
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun QuizPackItem(pack: QuizPackage, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(pack.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            if (pack.isCompleted) {
                Text(pack.score.toString(), style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}
