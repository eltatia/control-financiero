package com.gabriel.controlfinanciero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gabriel.controlfinanciero.navigation.AppNavigation
import com.gabriel.controlfinanciero.navigation.NavigationItem
import com.gabriel.controlfinanciero.ui.components.BottomBar
import com.gabriel.controlfinanciero.ui.theme.ControlFinancieroAppTheme   // 👈 IMPORT CORRECTO

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ControlFinancieroApp()
        }
    }
}

@Composable
fun ControlFinancieroApp() {
    ControlFinancieroAppTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val showBottomBar = currentRoute in setOf(
            NavigationItem.Home.route,
            NavigationItem.Reportes.route,
            NavigationItem.Calendario.route,
            NavigationItem.Cuentas.route
        )

        Surface(color = MaterialTheme.colorScheme.background) {
            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        BottomBar(navController)
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AppNavigation(navController)
                }
            }
        }
    }
}
