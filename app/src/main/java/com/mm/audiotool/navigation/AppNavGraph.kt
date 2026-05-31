package com.mm.audiotool.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mm.audiotool.ui.screen.AudioEditorScreen
import com.mm.audiotool.ui.screen.DashboardScreen
import com.mm.audiotool.ui.screen.ProcessingScreen
import com.mm.audiotool.ui.screen.SettingsScreen
import com.mm.audiotool.ui.screen.SuccessScreen
import com.mm.audiotool.viewmodel.SettingsViewModel

/** Route constants */
object Routes {
    const val DASHBOARD  = "dashboard"
    const val EDITOR     = "editor"
    const val PROCESSING = "processing"
    const val SUCCESS    = "success/{savedPath}"
    const val SETTINGS   = "settings"

    fun successRoute(savedPath: String) =
        "success/${java.net.URLEncoder.encode(savedPath, "UTF-8")}"
}

@Composable
fun AppNavGraph(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToEditor   = { ckbUri -> navController.navigate("editor?uri=${java.net.URLEncoder.encode(ckbUri, "UTF-8")}") },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                settingsViewModel    = settingsViewModel
            )
        }

        composable(
            route = "editor?uri={uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType; defaultValue = "" })
        ) { backStack ->
            val encodedUri = backStack.arguments?.getString("uri") ?: ""
            val ckbUri = java.net.URLDecoder.decode(encodedUri, "UTF-8")
            AudioEditorScreen(
                ckbUri            = ckbUri,
                settingsViewModel = settingsViewModel,
                onNavigateToProcessing = { navController.navigate(Routes.PROCESSING) },
                onBack            = { navController.popBackStack() }
            )
        }

        composable(Routes.PROCESSING) {
            // Retrieve the shared editor state via the back-stack entry that owns it
            val editorEntry = navController.getBackStackEntry("editor?uri={uri}")
            ProcessingScreen(
                settingsViewModel = settingsViewModel,
                editorBackStackEntry = editorEntry,
                onComplete = { savedPath ->
                    navController.navigate(Routes.successRoute(savedPath)) {
                        popUpTo(Routes.DASHBOARD) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Routes.SUCCESS,
            arguments = listOf(navArgument("savedPath") { type = NavType.StringType })
        ) { backStack ->
            val encodedPath = backStack.arguments?.getString("savedPath") ?: ""
            val savedPath   = java.net.URLDecoder.decode(encodedPath, "UTF-8")
            SuccessScreen(
                savedPath = savedPath,
                onDone    = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onBack            = { navController.popBackStack() }
            )
        }
    }
}
