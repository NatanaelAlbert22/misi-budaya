package com.example.misi_budaya.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.misi_budaya.data.local.AppDatabase
import com.example.misi_budaya.data.model.QuizPackage
import com.example.misi_budaya.data.repository.QuizRepository
import kotlinx.coroutines.launch
import coil.compose.AsyncImage

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
    var questionCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

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
    
    // Periodically refresh quiz list to show unlocked secret quizzes
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3000L) // Refresh setiap 3 detik
            presenter.loadQuizPacks()
        }
    }

    // Load question counts only once when quiz packs first become available
    val countLoadingStarted = remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(quizPacks) {
        if (quizPacks.isNotEmpty() && !countLoadingStarted.value) {
            countLoadingStarted.value = true
            scope.launch {
                try {
                    val newCounts = mutableMapOf<String, Int>()
                    for (pack in quizPacks) {
                        val count = repository.getQuestionCount(pack.name)
                        newCounts[pack.name] = count
                    }
                    questionCounts = newCounts
                } catch (e: Exception) {
                    // If loading fails, set all counts to 0
                    val newCounts = mutableMapOf<String, Int>()
                    for (pack in quizPacks) {
                        newCounts[pack.name] = 0
                    }
                    questionCounts = newCounts
                }
            }
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
            .background(MaterialTheme.colorScheme.background)
            .pullRefresh(pullRefreshState),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header dengan judul dan download button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Paket Quiz",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Pilih kategori untuk memulai",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Download button
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
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
                                contentDescription = "Download all questions",
                                tint = if (isOnline) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading && quizPacks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Memuat paket soal...",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Oops!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (quizPacks.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📚",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tidak ada paket soal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Silakan coba lagi nanti",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Sort: secret quizzes first, then regular ones
                    val sortedPacks = quizPacks.sortedByDescending { it.isSecret }
                    items(sortedPacks) { pack ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("quiz_description/${pack.name}") },
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icon/Image di sebelah kiri
                                Surface(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    color = Color(0xFFEEEEEE)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        AsyncImage(
                                            model = pack.iconUrl,
                                            contentDescription = pack.name,
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pack.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (pack.isCompleted) {
                                        Text(
                                            text = "✓ Selesai • Skor: ${pack.score}",
                                            fontSize = 14.sp,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        val questionCount = questionCounts[pack.name] ?: -1
                                        val countText = when {
                                            questionCount > 0 -> "$questionCount pertanyaan"
                                            questionCount == 0 -> "0 pertanyaan"
                                            else -> "Memuat..."
                                        }
                                        Text(
                                            text = countText,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "→",
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
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

    // Download dialog dengan desain modern
    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDownloading) showDownloadDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Download Pertanyaan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            color = Color(0xFF64B5F6),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = downloadMessage ?: "Sedang mendownload pertanyaan...",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        Text(
                            "Apakah Anda ingin mendownload semua pertanyaan dari paket soal untuk dapat mengerjakan secara offline?",
                            fontSize = 14.sp,
                            color = Color(0xFF616161),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )
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
                    Text(
                        "Download",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isDownloading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDownloadDialog = false },
                    enabled = !isDownloading
                ) {
                    Text(
                        "Batal",
                        fontSize = 16.sp,
                        color = if (!isDownloading) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        )
    }

    // No connection dialog dengan desain modern
    if (showNoConnectionDialog) {
        AlertDialog(
            onDismissRequest = { showNoConnectionDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "📡",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tidak Ada Koneksi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Text(
                    "Tidak dapat mendownload pertanyaan karena tidak ada koneksi internet. Pastikan Anda terhubung ke internet dan coba lagi.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showNoConnectionDialog = false }
                ) {
                    Text(
                        "OK",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}
