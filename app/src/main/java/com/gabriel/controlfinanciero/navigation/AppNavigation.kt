package com.gabriel.controlfinanciero.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gabriel.controlfinanciero.ui.screens.CalendarioScreen
import com.gabriel.controlfinanciero.ui.screens.CuentasScreen
import com.gabriel.controlfinanciero.ui.screens.HomeScreen
import com.gabriel.controlfinanciero.ui.screens.ReportesScreen

@Composable
fun AppNavigation(navController: NavHostController) {

    // 🔥 Estado GLOBAL del modo oscuro / claro
    var isDarkMode by rememberSaveable { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = NavigationItem.Home.route
    ) {
        composable(NavigationItem.Home.route) {
            HomeScreen(
                isDarkMode = isDarkMode,
                onToggleDarkMode = { isDarkMode = !isDarkMode }
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
