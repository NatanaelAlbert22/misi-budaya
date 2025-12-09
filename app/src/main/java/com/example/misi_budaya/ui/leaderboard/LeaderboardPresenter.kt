package com.example.misi_budaya.ui.leaderboard

import com.example.misi_budaya.data.repository.QuizRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LeaderboardPresenter(private val repository: QuizRepository, private val scope: CoroutineScope) : LeaderboardContract.Presenter {

    private var view: LeaderboardContract.View? = null

    override fun onAttach(view: LeaderboardContract.View) {
        this.view = view
        loadLeaderboard()
    }

    override fun onDetach() {
        this.view = null
    }

    override fun loadLeaderboard() {
        view?.showLoading()
        fetchData()
    }

    override fun onRefresh() {
        // Force fetch fresh data from Firestore (pull-to-refresh always gets latest)
        fetchData()
    }

    private fun fetchData() {
        scope.launch {
            try {
                val users = repository.getLeaderboardData()
                val processedUsers = users.map { user ->
                    if (user.username.isBlank() && user.email.isNotBlank()) {
                        val usernameFromEmail = user.email.split("@")[0]
                        user.copy(username = usernameFromEmail)
                    } else if (user.username.isBlank()) {
                        user.copy(username = "Anonymous")
                    } else {
                        user
                    }
                }
                view?.hideLoading()
                view?.showLeaderboard(processedUsers)
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError(e.message ?: "Failed to load leaderboard.")
            }
        }
    }
}
