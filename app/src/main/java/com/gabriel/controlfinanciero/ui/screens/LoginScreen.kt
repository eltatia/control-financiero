package com.gabriel.controlfinanciero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
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
fun LoginScreen(
    isDarkMode: Boolean,
    viewModel: FinanceViewModel = viewModel(factory = FinanceViewModel.Factory)
) {
    val loginError by viewModel.loginError.collectAsState()

    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mostrarCuenta by remember { mutableStateOf(false) }
    var cuentaNombre by remember { mutableStateOf("") }
    var cuentaTipo by remember { mutableStateOf("EFECTIVO") }
    var cuentaSaldo by remember { mutableStateOf("") }

    val bgColor = if (isDarkMode) Color(0xFF021712) else Color(0xFFF3F6FF)
    val cardColor = if (isDarkMode) Color(0xFF071E1A) else Color.White
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
            text = "Bienvenido",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = usuario,
                    onValueChange = { usuario = it },
                    label = { Text("Usuario") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "¿Quieres crear una cuenta al ingresar?",
                    color = textSecondary,
                    fontSize = 12.sp
                )

                Button(
                    onClick = { mostrarCuenta = !mostrarCuenta },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mostrarCuenta) Color(0xFF22C55E) else cardColor,
                        contentColor = if (mostrarCuenta) Color.Black else textPrimary
                    )
                ) {
                    Text(text = if (mostrarCuenta) "Crear cuenta: Sí" else "Crear cuenta: No")
                }

                if (mostrarCuenta) {
                    OutlinedTextField(
                        value = cuentaNombre,
                        onValueChange = { cuentaNombre = it },
                        label = { Text("Nombre de cuenta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cuentaTipo,
                        onValueChange = { cuentaTipo = it },
                        label = { Text("Tipo (EFECTIVO/BANCO/BILLETERA)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cuentaSaldo,
                        onValueChange = { cuentaSaldo = it },
                        label = { Text("Saldo inicial") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                loginError?.let { error ->
                    Text(
                        text = error,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        if (mostrarCuenta) {
                            val saldo = cuentaSaldo.replace(",", ".").toDoubleOrNull()
                            viewModel.loginWithAccount(
                                username = usuario.trim(),
                                password = password.trim(),
                                accountName = cuentaNombre,
                                accountType = cuentaTipo,
                                accountSaldo = saldo
                            )
                        } else {
                            viewModel.login(usuario.trim(), password.trim())
                        }
                    },
                    enabled = usuario.isNotBlank() && password.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF22C55E),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Ingresar")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Si el usuario no existe, se creará automáticamente.",
            color = textSecondary,
            fontSize = 12.sp
        )
        Text(
            text = "Puedes crear una cuenta desde este formulario.",
            color = textSecondary,
            fontSize = 12.sp
        )
    }
}
