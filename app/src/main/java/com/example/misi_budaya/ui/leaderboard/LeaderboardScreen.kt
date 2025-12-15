package com.example.misi_budaya.ui.leaderboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.misi_budaya.data.local.AppDatabase
import com.example.misi_budaya.data.local.UserPreferencesManager
import com.example.misi_budaya.data.model.UserProfile
import com.example.misi_budaya.data.repository.QuizRepository

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
            .background(MaterialTheme.colorScheme.background)
            .pullRefresh(pullRefreshState),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header dengan gradient
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Trophy icon
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFA726).copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = Color(0xFFFFA726),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            "Leaderboard",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Top pemain terbaik",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val isOfflineMode by preferencesManager.isOfflineModeFlow.collectAsState(initial = false)

            if (isOfflineMode) {
                // Offline mode card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📡",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Mode Offline Aktif",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E2E2E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Silakan beralih ke mode online untuk melihat leaderboard.",
                            fontSize = 14.sp,
                            color = Color(0xFF616161),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFFFA726),
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Memuat peringkat...",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else if (leaderboard.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🏆",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Papan Peringkat Kosong",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E2E2E)
                            )
                            Text(
                                text = "Jadilah yang pertama!",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(leaderboard) { index, user ->
                            LeaderboardItem(rank = index + 1, user = user)
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
    // Warna berdasarkan ranking
    val (rankColor, rankBgColor) = when (rank) {
        1 -> Pair(Color(0xFFFFD700), Color(0xFFFFF9C4)) // Gold
        2 -> Pair(Color(0xFFC0C0C0), Color(0xFFECEFF1)) // Silver
        3 -> Pair(Color(0xFFCD7F32), Color(0xFFFFE0B2)) // Bronze
        else -> Pair(Color(0xFF9E9E9E), Color(0xFFF5F5F5)) // Gray
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (rank <= 3) 6.dp else 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (rank <= 3) rankBgColor else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank badge
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = rankColor.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (rank <= 3) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = rankColor,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                "$rank",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = rankColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // User info
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (rank <= 3) {
                            Text(
                                "#$rank",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = rankColor,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        Text(
                            user.username,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (rank <= 3) Color(0xFF2E2E2E) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Level info
                    val level = (user.totalScore / 1000).toInt() + 1
                    Text(
                        "Level $level",
                        fontSize = 12.sp,
                        color = if (rank <= 3) Color(0xFF616161) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Score dengan background
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (rank <= 3) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        user.totalScore.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) rankColor else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        " XP",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    }
}
