package com.example.misi_budaya.ui.login

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val presenter = remember { LoginPresenter(context) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result -> presenter.onGoogleSignInResult(result) }
    )

    val view = remember(navController) {
        object : LoginContract.View {
            override fun showLoading() {
                isLoading = true
            }

            override fun hideLoading() {
                isLoading = false
            }

            override fun showMessage(msg: String) {
                message = msg
            }

            override fun navigateToHome() {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }

            override fun navigateToSignUp() {
                navController.navigate("signup")
            }

            override fun launchGoogleSignIn(intent: Intent) {
                googleSignInLauncher.launch(intent)
            }
        }
    }

    DisposableEffect(presenter) {
        presenter.onAttach(view)
        onDispose {
            presenter.onDetach()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, enabled = !isLoading)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { presenter.login(email, password) }, enabled = !isLoading) {
                Text("Login")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { presenter.performGoogleSignIn() }, enabled = !isLoading) {
                Text("Sign in with Google")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { view.navigateToSignUp() }, enabled = !isLoading) {
                Text("Don\'t have an account? Make one")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(message)
        }

        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}
