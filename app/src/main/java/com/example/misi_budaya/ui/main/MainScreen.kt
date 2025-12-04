package com.example.misi_budaya.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.misi_budaya.ui.home.HomeScreen
import com.example.misi_budaya.ui.leaderboard.LeaderboardScreen
import com.example.misi_budaya.ui.profile.ProfileScreen
import com.example.misi_budaya.ui.quiz.QuestionScreen
import com.example.misi_budaya.ui.quiz.QuizScreen
import com.example.misi_budaya.ui.quiz.ResultScreen

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, "home_screen"),
    BottomNavItem("Quiz", Icons.Default.QuestionAnswer, "quiz_screen"),
    BottomNavItem("Leaderboard", Icons.Default.List, "leaderboard_screen"),
    BottomNavItem("Profile", Icons.Default.AccountCircle, "profile_screen"),
)

@Composable
fun MainScreen(
    rootNavController: NavController,
    isDarkTheme: Boolean = false,
    onThemeChange: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Daftar route yang tidak menampilkan bottom bar
    val routesWithoutBottomBar = listOf("question_screen/{quizPackId}", "result_screen/{score}")
    val shouldShowBottomBar = routesWithoutBottomBar.none { route ->
        currentRoute?.startsWith(route.split("/").first()) == true
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination

                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home_screen",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home_screen") { 
                HomeScreen(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange
                )
            }
            composable("quiz_screen") { QuizScreen(navController = navController) }
            composable("leaderboard_screen") { LeaderboardScreen() }
            composable("profile_screen") { ProfileScreen(rootNavController = rootNavController) }

            // Routes for the quiz flow
            composable(
                route = "question_screen/{quizPackId}",
                arguments = listOf(navArgument("quizPackId") { type = NavType.StringType })
            ) { backStackEntry ->
                QuestionScreen(
                    navController = navController,
                    quizPackId = backStackEntry.arguments?.getString("quizPackId")
                )
            }
            composable(
                route = "result_screen/{score}",
                arguments = listOf(navArgument("score") { type = NavType.IntType })
            ) { backStackEntry ->
                ResultScreen(
                    navController = navController,
                    score = backStackEntry.arguments?.getInt("score")
                )
            }
        }
    }
}
