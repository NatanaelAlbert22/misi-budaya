package com.example.misi_budaya.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Simple app-wide event bus for small UI events.
 */
object AppEvents {
    private val _leaderboardRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val leaderboardRefresh = _leaderboardRefresh.asSharedFlow()

    fun emitLeaderboardRefresh() {
        _leaderboardRefresh.tryEmit(Unit)
    }

    private val _questionsDownloaded = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val questionsDownloaded = _questionsDownloaded.asSharedFlow()

    fun emitQuestionsDownloaded() {
        _questionsDownloaded.tryEmit(Unit)
    }
}
