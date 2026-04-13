package sk.o2.scratchcard.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import sk.o2.scratchcard.presentation.activation.ActivationScreen
import sk.o2.scratchcard.presentation.main.MainScreen
import sk.o2.scratchcard.presentation.scratch.ScratchScreen

@Composable
fun ScratchCardNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN
    ) {
        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToScratch = { navController.navigate(Routes.SCRATCH) },
                onNavigateToActivation = { navController.navigate(Routes.ACTIVATION) }
            )
        }

        composable(Routes.SCRATCH) {
            ScratchScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ACTIVATION) {
            ActivationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
