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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.misi_budaya.data.local.AppDatabase
import com.example.misi_budaya.data.model.QuizPackage
import com.example.misi_budaya.data.repository.QuizRepository
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun QuizScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(true) } // Start with loading
    var isRefreshing by remember { mutableStateOf(false) }
    var quizPacks by remember { mutableStateOf<List<QuizPackage>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadMessage by remember { mutableStateOf<String?>(null) }
    var showNoConnectionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { QuizRepository(db.quizPackageDao(), db.questionDao()) }
    val scope = rememberCoroutineScope()
    val presenter = remember { QuizPresenter(repository, scope) }
    val networkMonitor = remember { com.example.misi_budaya.util.NetworkMonitor(context) }
    val isOnline by networkMonitor.observeNetworkStatus().collectAsState(initial = false)

    val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh = {
        // When the user triggers a pull-to-refresh, show the refresh indicator and call presenter
        isRefreshing = true
        presenter.onRefresh()
    })

    // Auto-refresh when online status changes (e.g., user switches from offline to online)
    androidx.compose.runtime.LaunchedEffect(isOnline) {
        if (isOnline && quizPacks.isNotEmpty()) {
            // User just went online, refresh paket data to get latest scores
            presenter.onRefresh()
        }
    }

    val view = remember(navController) {
        object : QuizContract.View {
            override fun showLoading() {
                // Full-screen loading state (e.g., initial load). Ensure pull indicator isn't shown as a full-screen loader.
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pilih Paket Soal", style = MaterialTheme.typography.headlineMedium)

                // Download button
                IconButton(
                    onClick = {
                        if (isOnline) {
                            showDownloadDialog = true
                        } else {
                            showNoConnectionDialog = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "Download all questions"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && quizPacks.isEmpty()) { // Show loading only if there's no data yet
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text(text = errorMessage!!)
            } else if (quizPacks.isEmpty()) {
                Text("Tidak ada paket soal yang tersedia.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(quizPacks) { pack ->
                        QuizPackItem(pack = pack, onClick = { navController.navigate("quiz_description/${pack.name}") })
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    // Download dialog
    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDownloading) showDownloadDialog = false },
            title = { Text("Download Pertanyaan") },
            text = {
                Column {
                    if (isDownloading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = downloadMessage ?: "Sedang mendownload pertanyaan...",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        Text("Apakah Anda ingin mendownload semua pertanyaan dari paket soal untuk dapat mengerjakan secara offline?")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDownloading = true
                        downloadMessage = "Sedang mendownload..."
                        scope.launch {
                            try {
                                repository.downloadAllQuestionsForAllPackages()
                                downloadMessage = "Berhasil mendownload semua pertanyaan!"
                                isDownloading = false
                                // Close dialog after success
                                showDownloadDialog = false
                            } catch (e: Exception) {
                                downloadMessage = "Gagal mendownload: ${e.message}"
                                isDownloading = false
                            }
                        }
                    },
                    enabled = !isDownloading
                ) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDownloadDialog = false },
                    enabled = !isDownloading
                ) {
                    Text("Batal")
                }
            }
        )
    }

    // No connection dialog
    if (showNoConnectionDialog) {
        AlertDialog(
            onDismissRequest = { showNoConnectionDialog = false },
            title = { Text("Tidak Ada Koneksi") },
            text = { Text("Tidak dapat mendownload pertanyaan karena tidak ada koneksi internet.") },
            confirmButton = {
                TextButton(onClick = { showNoConnectionDialog = false }) {
                    Text("OK")
                }
            }
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
