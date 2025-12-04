package com.example.misi_budaya

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.misi_budaya.ui.login.LoginScreen
import com.example.misi_budaya.ui.main.MainScreen
import com.example.misi_budaya.ui.signup.SignUpScreen
import com.example.misi_budaya.ui.splash.SplashScreen
import com.example.misi_budaya.ui.theme.MisibudayaTheme
import com.example.misi_budaya.ui.login.*

class MainActivity : ComponentActivity(), LoginContract.View {

    private lateinit var presenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = LoginPresenter(this)

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            
            MisibudayaTheme(darkTheme = isDarkTheme) {
                MyApp(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = it }
                )
            }
        }
    }

    override fun showLoading() {
        // Ditangani di dalam Composable
    }

    override fun hideLoading() {
        // Ditangani di dalam Composable
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun navigateToHome() {
        // Navigasi ditangani oleh NavController di MyApp
    }

    override fun navigateToSignUp() {
        // Navigasi ditangani oleh NavController di MyApp
    }

    override fun launchGoogleSignIn(intent: Intent) {
        // TODO: Implementasikan dengan ActivityResultLauncher
    }
}

@Composable
fun MyApp(
    isDarkTheme: Boolean = false,
    onThemeChange: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("signup") {
            SignUpScreen(navController = navController)
        }
        composable("home") {
            MainScreen(
                rootNavController = navController,
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange
            )
        }
    }
}
