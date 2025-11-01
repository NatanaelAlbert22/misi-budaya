package com.example.misi_budaya.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(presenter: LoginPresenter) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val view = object : LoginContract.View {
        override fun showLoginSuccess(username: String) {
            message = "Selamat datang, $username!"
        }

        override fun showLoginError(msg: String) {
            message = msg
        }
    }

    presenter.apply { /* bisa digunakan nanti */ }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val p = LoginPresenter(view)
            p.login(username, password)
        }) {
            Text("Masuk")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(message)
    }
}
