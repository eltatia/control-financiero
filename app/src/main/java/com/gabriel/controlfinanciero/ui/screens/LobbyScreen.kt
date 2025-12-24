package com.gabriel.controlfinanciero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabriel.controlfinanciero.viewmodel.FinanceViewModel

@Composable
fun LobbyScreen(
    isDarkMode: Boolean,
    viewModel: FinanceViewModel = viewModel(factory = FinanceViewModel.Factory)
) {
    val cuentas by viewModel.cuentas.collectAsState()
    val cuentaActual by viewModel.cuentaActual.collectAsState()

    var cuentaSeleccionadaId by remember { mutableStateOf(cuentaActual?.id ?: 0) }

    val bgColor = if (isDarkMode) Color(0xFF021712) else Color(0xFFF3F6FF)
    val cardColor = if (isDarkMode) Color(0xFF071E1A) else Color.White
    val softCard = if (isDarkMode) Color(0xFF0A2320) else Color(0xFFECECEC)
    val textPrimary = if (isDarkMode) Color.White else Color.Black
    val textSecondary = if (isDarkMode) Color(0xFF9CA3AF) else Color.Gray

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Selecciona tu cuenta",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (cuentas.isEmpty()) {
            Text(
                text = "No hay cuentas registradas. Crea una cuenta para continuar.",
                color = textSecondary,
                fontSize = 14.sp
            )
        } else {
            cuentas.forEach { cuenta ->
                val selected = cuenta.id == cuentaSeleccionadaId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { cuentaSeleccionadaId = cuenta.id },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) Color(0xFF22C55E) else cardColor
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cuenta.nombre,
                            color = if (selected) Color.Black else textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (cuentaSeleccionadaId != 0) {
                    viewModel.setCuentaActual(cuentaSeleccionadaId)
                }
                viewModel.setLoggedIn(true)
            },
            enabled = cuentas.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = Color.Black)
        ) {
            Text(text = "Ingresar")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Puedes cambiar la cuenta desde el engranaje.",
            color = textSecondary,
            fontSize = 12.sp
        )
    }
}
