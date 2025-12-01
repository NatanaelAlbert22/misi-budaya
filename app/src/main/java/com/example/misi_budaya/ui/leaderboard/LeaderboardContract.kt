package com.example.misi_budaya.ui.leaderboard

import com.example.misi_budaya.data.model.UserProfile

interface LeaderboardContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showLeaderboard(users: List<UserProfile>)
        fun showError(message: String)
    }

    interface Presenter {
        fun onAttach(view: View)
        fun onDetach()
        fun loadLeaderboard()
        fun onRefresh()
    }
}
