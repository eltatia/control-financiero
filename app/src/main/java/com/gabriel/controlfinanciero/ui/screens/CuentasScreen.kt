package com.gabriel.controlfinanciero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores coherentes
private val AccountsBackground = Color(0xFF021712)
private val CardDark = Color(0xFF071E1A)
private val CardDarkSoft = Color(0xFF0A2320)
private val AccentGreen = Color(0xFF22C55E)
private val AccentRed = Color(0xFFEF4444)
private val TextMuted = Color(0xFF9CA3AF)

// =============================================================
//                      PANTALLA CUENTAS
// =============================================================
@Composable
fun CuentasScreen(isDarkMode: Boolean) {

    val bgColor = if (isDarkMode) AccountsBackground else Color(0xFFF3F6FF)
    val cardColor = if (isDarkMode) CardDark else Color.White
    val softCardColor = if (isDarkMode) CardDarkSoft else Color(0xFFE5E7EB)
    val textPrimary = if (isDarkMode) Color.White else Color.Black
    val textMutedColor = if (isDarkMode) TextMuted else Color.Gray

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            // ================= HEADER =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(40.dp))

                Text(
                    text = "Cuentas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    modifier = Modifier.weight(1f),
                )

                IconButton(onClick = { /* ajustes */ }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración",
                        tint = textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ================= TU PATRIMONIO =================
            Text(
                text = "Tu Patrimonio",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PatrimonioCard(
                    titulo = "Total Activos",
                    monto = 15750.0,
                    colorMonto = AccentGreen,
                    modifier = Modifier.weight(1f),
                    cardColor = cardColor,
                    textMutedColor = textMutedColor
                )
                PatrimonioCard(
                    titulo = "Total Pasivos",
                    monto = 8300.0,
                    colorMonto = AccentRed,
                    modifier = Modifier.weight(1f),
                    cardColor = cardColor,
                    textMutedColor = textMutedColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ================= MIS CUENTAS =================
            Text(
                text = "Mis Cuentas",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            AccountItemCard(
                icon = Icons.Default.AccountBalanceWallet,
                iconBgColor = Color(0xFF16A34A),
                titulo = "Efectivo",
                subtitulo = "Dinero en mano",
                monto = 500.0,
                cardColor = cardColor,
                textPrimary = textPrimary,
                textMutedColor = textMutedColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            AccountItemCard(
                icon = Icons.Default.AccountBalance,
                iconBgColor = Color(0xFF2563EB),
                titulo = "Banco Principal",
                subtitulo = "Cuenta de nómina",
                monto = 12250.0,
                cardColor = cardColor,
                textPrimary = textPrimary,
                textMutedColor = textMutedColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            AccountItemCard(
                icon = Icons.Default.Savings,
                iconBgColor = Color(0xFFEAB308),
                titulo = "Ahorros Viaje",
                subtitulo = "Fondo para vacaciones",
                monto = 3000.0,
                cardColor = cardColor,
                textPrimary = textPrimary,
                textMutedColor = textMutedColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ================= DEUDAS Y PRÉSTAMOS =================
            Text(
                text = "Deudas y Préstamos",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            DebtCardCredit(
                cardColor = cardColor,
                softCardColor = softCardColor,
                textPrimary = textPrimary,
                textMutedColor = textMutedColor
            )

            Spacer(modifier = Modifier.height(80.dp)) // espacio para el FAB
        }

        // ================= FAB (+) =================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AccentGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir cuenta o deuda",
                    tint = Color.Black
                )
            }
        }
    }
}

// =============================================================
//                      COMPONENTES REUTILIZABLES
// =============================================================

@Composable
private fun PatrimonioCard(
    titulo: String,
    monto: Double,
    colorMonto: Color,
    modifier: Modifier = Modifier,
    cardColor: Color,
    textMutedColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = titulo,
                color = textMutedColor,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "S/ ${"%,.2f".format(monto)}",
                color = colorMonto,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun AccountItemCard(
    icon: ImageVector,
    iconBgColor: Color,
    titulo: String,
    subtitulo: String,
    monto: Double,
    cardColor: Color,
    textPrimary: Color,
    textMutedColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBgColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconBgColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    color = textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = subtitulo,
                    color = textMutedColor,
                    fontSize = 13.sp
                )
            }

            Text(
                text = "S/ ${"%,.2f".format(monto)}",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun DebtCardCredit(
    cardColor: Color,
    softCardColor: Color,
    textPrimary: Color,
    textMutedColor: Color
) {
    val pagado = 5200f
    val limite = 10000f
    val pendiente = 4800f
    val progreso = (pagado / limite).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Título + saldo pendiente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF7F1D1D).copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = AccentRed
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Tarjeta de Crédito",
                            color = textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }

                Text(
                    text = "S/ ${"%,.2f".format(pendiente)}",
                    color = AccentRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pagado / límite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pagado S/ ${"%,.2f".format(pagado)}",
                    color = textMutedColor,
                    fontSize = 13.sp
                )
                Text(
                    text = "de S/ ${"%,.2f".format(limite)}",
                    color = textMutedColor,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(softCardColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progreso)
                        .fillMaxHeight()
                        .background(AccentGreen)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Próximo pago + botón Pagar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Próximo pago: 15 de Julio",
                        color = textPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Cuota mensual: S/ 450.00",
                        color = textMutedColor,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = { /* TODO: acción de pagar */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Pagar",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
