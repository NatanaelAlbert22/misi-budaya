package com.example.misi_budaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.misi_budaya.ui.home.HomeScreen
import com.example.misi_budaya.ui.login.LoginScreen
import com.example.misi_budaya.ui.signup.SignUpScreen
import com.example.misi_budaya.ui.theme.MisibudayaTheme
import com.example.misi_budaya.ui.login.*

class MainActivity : ComponentActivity(), LoginContract.View {

    private lateinit var presenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = LoginPresenter(this)

        setContent {
            MisibudayaTheme {
                MyApp()
            }
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("signup") {
            SignUpScreen(navController = navController)
        }
        composable("home") {
            HomeScreen()
        }
    }
}