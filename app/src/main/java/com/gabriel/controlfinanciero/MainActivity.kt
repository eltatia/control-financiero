package com.gabriel.controlfinanciero

import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
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
    var isDarkMode by rememberSaveable { mutableStateOf(false) }
    ControlFinancieroAppTheme(darkTheme = isDarkMode) {
        RequestNotificationPermission()
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
                    AppNavigation(
                        navController = navController,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { isDarkMode = !isDarkMode }
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    LaunchedEffect(Unit) {
        val permission = android.Manifest.permission.POST_NOTIFICATIONS
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            launcher.launch(permission)
        }
    }
}
