package com.example.misi_budaya.ui.login

class LoginPresenter(private val view: LoginContract.View) : LoginContract.Presenter {
    override fun login(username: String, password: String) {
        if (username == "user" && password == "1234") {
            view.showLoginSuccess(username)
        } else {
            view.showLoginError("Username atau password salah!")
        }
    }
}
