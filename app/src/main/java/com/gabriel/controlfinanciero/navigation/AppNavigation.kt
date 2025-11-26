package com.gabriel.controlfinanciero.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gabriel.controlfinanciero.ui.screens.CalendarioScreen
import com.gabriel.controlfinanciero.ui.screens.CuentasScreen
import com.gabriel.controlfinanciero.ui.screens.HomeScreen
import com.gabriel.controlfinanciero.ui.screens.ReportesScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationItem.Home.route
    ) {
        composable(NavigationItem.Home.route) {
            HomeScreen()
        }
        composable(NavigationItem.Reportes.route) {
            ReportesScreen()
        }
        composable(NavigationItem.Calendario.route) {
            CalendarioScreen()
        }
        composable(NavigationItem.Cuentas.route) {
            CuentasScreen()
        }
    }
}
