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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            MisibudayaTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
                LoginScreen(presenter = com.example.misi_budaya.ui.login.LoginPresenter(object : com.example.misi_budaya.ui.login.LoginContract.View {
                    override fun showLoginSuccess(username: String) {}
                    override fun showLoginError(message: String) {}
                }))
            }
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