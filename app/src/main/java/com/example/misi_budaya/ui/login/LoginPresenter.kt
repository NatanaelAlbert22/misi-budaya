package com.example.misi_budaya.ui.login

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import com.example.misi_budaya.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginPresenter(private val context: Context) : LoginContract.Presenter {

    private var view: LoginContract.View? = null
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onAttach(view: LoginContract.View) {
        this.view = view
    }

    override fun onDetach() {
        this.view = null
    }

    override fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            view?.showMessage("Please fill in all fields.")
            return
        }

        view?.showLoading()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                view?.hideLoading()
                if (task.isSuccessful) {
                    view?.navigateToHome()
                } else {
                    view?.showMessage(task.exception?.message ?: "Login failed.")
                }
            }
    }

    override fun performGoogleSignIn() {
        view?.showLoading()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        view?.launchGoogleSignIn(googleSignInClient.signInIntent)
    }

    override fun onGoogleSignInResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)

                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        view?.hideLoading()
                        if (authTask.isSuccessful) {
                            view?.navigateToHome()
                        } else {
                            Log.w("LoginPresenter", "signInWithCredential failed", authTask.exception)
                            view?.showMessage("Authentication Failed: ${authTask.exception?.message}")
                        }
                    }
            } catch (e: ApiException) {
                view?.hideLoading()
                Log.e("LoginPresenter", "Google sign in failed with ApiException", e)
                view?.showMessage("Google sign in failed: API code ${e.statusCode}")
            }
        } else {
            view?.hideLoading()
            Log.w("LoginPresenter", "Google Sign In cancelled or failed. Result code: ${result.resultCode}")
            view?.showMessage("Sign-in was cancelled.")
        }
    }
}