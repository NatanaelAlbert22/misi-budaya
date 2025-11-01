package com.example.misi_budaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.misi_budaya.ui.login.LoginScreen
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.misi_budaya.ui.theme.MisibudayaTheme
import com.example.misi_budaya.ui.login.*

class MainActivity : ComponentActivity(), LoginContract.View {

    private lateinit var presenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = LoginPresenter(this)

        setContent {
            LoginScreen(presenter)
        }
    }

    override fun showLoginSuccess() {
        println("✅ Login berhasil!")
    }

    override fun showLoginError(message: String) {
        println("❌ Login gagal: $message")
    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MisibudayaTheme {
        Greeting("Android")
    }
}