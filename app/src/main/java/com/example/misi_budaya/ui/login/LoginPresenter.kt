package com.example.misi_budaya.ui.login

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import com.example.misi_budaya.R
import com.example.misi_budaya.util.NetworkActivityGuard
import com.example.misi_budaya.util.NetworkMonitor
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class LoginPresenter(private val context: Context) : LoginContract.Presenter {

    private var view: LoginContract.View? = null
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val networkMonitor = NetworkMonitor(context)
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onAttach(view: LoginContract.View) {
        this.view = view
    }

    override fun onDetach() {
        this.view = null
        // Cancel any pending retries to avoid leaks
        scope.cancel()
    }

    override fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            view?.showMessage("Please fill in all fields.")
            return
        }

        // Check network before attempting auth
        if (!networkMonitor.isOnline()) {
            view?.showMessage("Tidak ada koneksi internet. Silakan sambungkan jaringan lalu coba lagi.")
            return
        }

        view?.showLoading()
        // Indicate auth started so background syncs can pause
        NetworkActivityGuard.setAuthInProgress(true)
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                view?.hideLoading()
                NetworkActivityGuard.setAuthInProgress(false)
                if (task.isSuccessful) {
                    view?.navigateToHome()
                } else {
                    val msg = task.exception?.message ?: "Login failed."
                    view?.showMessage(msg)
                    android.util.Log.e("LoginPresenter", "Email login failed: $msg", task.exception)
                }
            }
    }

    override fun performGoogleSignIn() {
        // Ensure network is available before launching Google Sign-In
        if (!networkMonitor.isOnline()) {
            view?.showMessage("Tidak ada koneksi internet. Silakan sambungkan jaringan lalu coba lagi.")
            return
        }

        view?.showLoading()
        NetworkActivityGuard.setAuthInProgress(true)
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

                // Quick network check before attempting credential sign-in
                if (!networkMonitor.isOnline()) {
                    view?.hideLoading()
                    NetworkActivityGuard.setAuthInProgress(false)
                    view?.showMessage("Tidak ada koneksi internet. Silakan sambungkan jaringan lalu coba lagi.")
                    return
                }

                // Attempt sign-in with simple retry on network errors
                attemptSignInWithCredential(credential, attemptsLeft = 3)
            } catch (e: ApiException) {
                view?.hideLoading()
                NetworkActivityGuard.setAuthInProgress(false)
                Log.e("LoginPresenter", "Google sign in failed with ApiException", e)
                view?.showMessage("Google sign in failed: API code ${e.statusCode}")
            }
        } else {
            view?.hideLoading()
            NetworkActivityGuard.setAuthInProgress(false)
            Log.w("LoginPresenter", "Google Sign In cancelled or failed. Result code: ${result.resultCode}")
            view?.showMessage("Sign-in was cancelled.")
        }
    }

    private fun attemptSignInWithCredential(credential: com.google.firebase.auth.AuthCredential, attemptsLeft: Int) {
        if (attemptsLeft <= 0) {
            view?.showMessage("Authentication Failed after retries.")
            NetworkActivityGuard.setAuthInProgress(false)
            view?.hideLoading()
            return
        }

        android.util.Log.d("LoginPresenter", "Attempt signInWithCredential, attemptsLeft=$attemptsLeft")

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                view?.hideLoading()
                NetworkActivityGuard.setAuthInProgress(false)
                view?.navigateToHome()
            } else {
                val ex = task.exception
                val msg = ex?.message ?: "Authentication failed"
                android.util.Log.w("LoginPresenter", "signInWithCredential failed (attemptsLeft=$attemptsLeft): $msg", ex)

                val isNetworkError = ex is com.google.firebase.FirebaseNetworkException || msg.contains("network", true) || msg.contains("timeout", true)

                if (isNetworkError && attemptsLeft > 1) {
                    // retry with exponential backoff
                    scope.launch {
                        val backoffMs = (4 - attemptsLeft) * 1000L // 1s, 2s
                        android.util.Log.d("LoginPresenter", "Retrying after ${backoffMs}ms")
                        delay(backoffMs)
                        attemptSignInWithCredential(credential, attemptsLeft - 1)
                    }
                } else {
                    // final failure
                    view?.hideLoading()
                    NetworkActivityGuard.setAuthInProgress(false)
                    view?.showMessage("Authentication Failed: $msg")
                }
            }
        }
    }
}