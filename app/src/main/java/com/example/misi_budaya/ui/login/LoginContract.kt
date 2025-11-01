package com.example.misi_budaya.ui.login

interface LoginContract {
    interface View {
        fun showLoginSuccess(username: String)
        fun showLoginError(message: String)
    }

    interface Presenter {
        fun login(username: String, password: String)
    }
}
