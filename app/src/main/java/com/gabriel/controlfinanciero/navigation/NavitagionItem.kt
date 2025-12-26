package com.gabriel.controlfinanciero.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Login : NavigationItem("login", "Login", Icons.Filled.Home)
    object CrearCuenta : NavigationItem("crear-cuenta", "Crear Cuenta", Icons.Filled.Home)
    object Home : NavigationItem("home", "Inicio", Icons.Filled.Home)
    object Reportes : NavigationItem("reportes", "Reportes", Icons.Filled.Assessment)
    object Calendario : NavigationItem("calendario", "Calendario", Icons.Filled.CalendarToday)
    object Cuentas : NavigationItem("cuentas", "Cuentas", Icons.Filled.AccountBalanceWallet)
}
