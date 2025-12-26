package com.gabriel.controlfinanciero.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gabriel.controlfinanciero.ui.screens.CalendarioScreen
import com.gabriel.controlfinanciero.ui.screens.CuentasScreen
import com.gabriel.controlfinanciero.ui.screens.HomeScreen
import com.gabriel.controlfinanciero.ui.screens.CreateAccountScreen
import com.gabriel.controlfinanciero.ui.screens.LoginScreen
import com.gabriel.controlfinanciero.ui.screens.ReportesScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabriel.controlfinanciero.viewmodel.FinanceViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val viewModel: FinanceViewModel = viewModel(factory = FinanceViewModel.Factory)
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        val target = if (isLoggedIn) NavigationItem.Home.route else NavigationItem.Login.route
        navController.navigate(target) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) NavigationItem.Home.route else NavigationItem.Login.route
    ) {
        composable(NavigationItem.Login.route) {
            LoginScreen(
                isDarkMode = isDarkMode,
                viewModel = viewModel,
                onCreateAccount = { navController.navigate(NavigationItem.CrearCuenta.route) }
            )
        }
        composable(NavigationItem.CrearCuenta.route) {
            CreateAccountScreen(
                isDarkMode = isDarkMode,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavigationItem.Home.route) {
            HomeScreen(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode
            )
        }
        composable(NavigationItem.Reportes.route) {
            ReportesScreen(isDarkMode = isDarkMode)
        }
        composable(NavigationItem.Calendario.route) {
            CalendarioScreen(isDarkMode = isDarkMode)
        }
        composable(NavigationItem.Cuentas.route) {
            CuentasScreen(isDarkMode = isDarkMode)
        }
    }
}
