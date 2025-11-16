package com.example.easyjapanesey.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.easyjapanesey.ui.sidebar.SidebarScreen
import com.example.easyjapanesey.ui.flashcard.FlashcardScreen

sealed class Screen(val route: String) {
    object Sidebar : Screen("sidebar")
    object Flashcard : Screen("flashcard/{category}/{level1}?level2={level2}") {
        fun createRoute(category: String, level1: String, level2: String?): String {
            return if (level2 != null) {
                "flashcard/$category/$level1?level2=$level2"
            } else {
                "flashcard/$category/$level1"
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Sidebar.route
    ) {
        composable(Screen.Sidebar.route) {
            SidebarScreen(navController = navController)
        }
        
        composable(
            route = Screen.Flashcard.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("level1") { type = NavType.StringType },
                navArgument("level2") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val level1 = backStackEntry.arguments?.getString("level1") ?: ""
            val level2 = backStackEntry.arguments?.getString("level2")
            
            FlashcardScreen(
                navController = navController,
                category = category,
                level1 = level1,
                level2 = level2
            )
        }
    }
}
