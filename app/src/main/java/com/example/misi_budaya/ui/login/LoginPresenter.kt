package com.example.misi_budaya.ui.login

import com.example.misi_budaya.data.repository.AuthRepository

class LoginPresenter(
    private val view: LoginContract.View,
    private val repository: AuthRepository = AuthRepository()
) : LoginContract.Presenter {

    override fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            view.showLoginError("Email dan password tidak boleh kosong")
            return
        }

        repository.login(email, password) { success, message ->
            if (success) view.showLoginSuccess()
            else view.showLoginError(message ?: "Login gagal")
        }
    }
}
