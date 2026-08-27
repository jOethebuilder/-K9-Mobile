package com.joethebuilder.k9.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.joethebuilder.k9.ui.screens.MainMenuScreen
import com.joethebuilder.k9.ui.screens.anycubic.AnycubicScreen
import com.joethebuilder.k9.ui.screens.openspool.OpenSpoolScreen
import com.joethebuilder.k9.ui.screens.qidi.QidiScreen
import com.joethebuilder.k9.viewmodel.AnycubicViewModel
import com.joethebuilder.k9.viewmodel.OpenSpoolViewModel
import com.joethebuilder.k9.viewmodel.QidiViewModel

object Routes {
    const val MAIN = "main"
    const val QIDI = "qidi"
    const val OPENSPOOL = "openspool"
    const val ANYCUBIC = "anycubic"
}

@Composable
fun K9NavHost(
    navController: NavHostController,
    qidiViewModel: QidiViewModel,
    openSpoolViewModel: OpenSpoolViewModel,
    anycubicViewModel: AnycubicViewModel
) {
    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainMenuScreen(
                onSelectQidi = { navController.navigate(Routes.QIDI) },
                onSelectOpenSpool = { navController.navigate(Routes.OPENSPOOL) },
                onSelectAnycubic = { navController.navigate(Routes.ANYCUBIC) }
            )
        }
        composable(Routes.QIDI) {
            QidiScreen(
                viewModel = qidiViewModel,
                onBack = { navController.popBackStack(Routes.MAIN, inclusive = false) }
            )
        }
        composable(Routes.OPENSPOOL) {
            OpenSpoolScreen(
                viewModel = openSpoolViewModel,
                onBack = { navController.popBackStack(Routes.MAIN, inclusive = false) }
            )
        }
        composable(Routes.ANYCUBIC) {
            AnycubicScreen(
                viewModel = anycubicViewModel,
                onBack = { navController.popBackStack(Routes.MAIN, inclusive = false) }
            )
        }
    }
}
