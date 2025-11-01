package com.example.misi_budaya.ui.login

import android.content.Intent
import androidx.activity.result.ActivityResult

interface LoginContract {

    interface View {
        fun showLoading()
        fun hideLoading()
        fun showMessage(message: String)
        fun navigateToHome()
        fun navigateToSignUp()
        fun launchGoogleSignIn(intent: Intent)
    }

    interface Presenter {
        fun onAttach(view: View)
        fun onDetach()
        fun login(email: String, password: String)
        fun performGoogleSignIn()
        fun onGoogleSignInResult(result: ActivityResult)
    }
}