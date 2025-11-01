package com.example.misi_budaya.ui.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Dummy data class for leaderboard entries
data class LeaderboardEntry(val rank: Int, val name: String, val score: Int)

// Dummy data
val dummyLeaderboardData = listOf(
    LeaderboardEntry(1, "Nael", 9500),
    LeaderboardEntry(2, "Budi", 9200),
    LeaderboardEntry(3, "Citra", 8800),
    LeaderboardEntry(4, "Dewi", 8500),
    LeaderboardEntry(5, "Eka", 8100),
    LeaderboardEntry(6, "Fajar", 7800),
    LeaderboardEntry(7, "Gita", 7500),
    LeaderboardEntry(8, "Hadi", 7200),
    LeaderboardEntry(9, "Indra", 6900),
    LeaderboardEntry(10, "Joko", 6600)
)

@Composable
fun LeaderboardScreen() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Leaderboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dummyLeaderboardData) { entry ->
                LeaderboardRow(entry = entry)
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("#${entry.rank}", fontWeight = FontWeight.Bold)
            Text(entry.name, modifier = Modifier.weight(1f).padding(horizontal = 16.dp))
            Text("${entry.score} pts", fontWeight = FontWeight.SemiBold)
        }
    }
}
