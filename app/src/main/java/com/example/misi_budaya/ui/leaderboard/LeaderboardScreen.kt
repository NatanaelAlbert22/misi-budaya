package com.example.misi_budaya.ui.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.misi_budaya.data.local.AppDatabase
import com.example.misi_budaya.data.local.UserPreferencesManager
import com.example.misi_budaya.data.model.UserProfile
import com.example.misi_budaya.data.repository.QuizRepository
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LeaderboardScreen() {
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var leaderboard by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val preferencesManager = remember { UserPreferencesManager(context) }
    val repository = remember { QuizRepository(db.quizPackageDao(), db.questionDao()) }
    val scope = rememberCoroutineScope()
    val presenter = remember { LeaderboardPresenter(repository, scope) }
    // Subscribe to global events: refresh leaderboard on demand
    LaunchedEffect(Unit) {
        try {
            com.example.misi_budaya.util.AppEvents.leaderboardRefresh.collect {
                try { presenter.onRefresh() } catch (e: Exception) { android.util.Log.e("LeaderboardScreen","Presenter refresh failed", e) }
            }
        } catch (e: Exception) {
            android.util.Log.e("LeaderboardScreen", "Failed to collect leaderboardRefresh events", e)
        }
    }

    // set isRefreshing = true when pull is triggered so indicator displays regardless of where user drags
    val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh = {
        isRefreshing = true
        presenter.onRefresh()
    })

    val view = remember {
        object : LeaderboardContract.View {
            override fun showLoading() {
                isLoading = true
                isRefreshing = false
            }

            override fun hideLoading() {
                isLoading = false
                isRefreshing = false
            }

            override fun showLeaderboard(users: List<UserProfile>) {
                leaderboard = users
                errorMessage = null
            }

            override fun showError(message: String) {
                errorMessage = message
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
        contentAlignment = Alignment.TopCenter // Align the indicator to the top
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Leaderboard", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            val isOfflineMode by preferencesManager.isOfflineModeFlow.collectAsState(initial = false)

            if (isOfflineMode) {
                Text("Mode offline aktif — silakan beralih ke mode online untuk melihat leaderboard.")
            } else {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (errorMessage != null) {
                    Text(text = errorMessage!!)
                } else if (leaderboard.isEmpty()) {
                    Text("Papan peringkat masih kosong.")
                } else {
                    leaderboard.forEachIndexed { index, user ->
                        LeaderboardItem(rank = index + 1, user = user)
                        if (index < leaderboard.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
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
}

@Composable
private fun LeaderboardItem(rank: Int, user: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$rank.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Text(user.username, style = MaterialTheme.typography.bodyLarge)
            }
            Text(user.totalScore.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
