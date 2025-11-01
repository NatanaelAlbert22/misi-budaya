package com.example.misi_budaya.ui.login

interface LoginContract {
    interface View {
        fun showLoginSuccess()
        fun showLoginError(message: String)
    }

    interface Presenter {
        fun login(email: String, password: String)
    }
}
