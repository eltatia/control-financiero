package com.gabriel.controlfinanciero.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores base estilo dark-reportes
private val ReportsBackground = Color(0xFF021712)
private val CardDark = Color(0xFF071E1A)
private val CardDarkSoft = Color(0xFF0A2320)
private val AccentGreen = Color(0xFF22C55E)
private val AccentRed = Color(0xFFEF4444)
private val TextMuted = Color(0xFF9CA3AF)

// =============================================================
//                      PANTALLA REPORTES
// =============================================================
@Composable
fun ReportesScreen(isDarkMode: Boolean) {

    val bgColor = if (isDarkMode) ReportsBackground else Color(0xFFF3F6FF)
    val cardColor = if (isDarkMode) CardDark else Color.White
    val softCardColor = if (isDarkMode) CardDarkSoft else Color(0xFFE5E7EB)
    val textMutedColor = if (isDarkMode) TextMuted else Color.Gray
    val textPrimary = if (isDarkMode) Color.White else Color.Black

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Mes actual", "Mes anterior", "Año actual", "Personalizado")

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

            // ================== HEADER ==================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { /* back si quieres */ }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Reportes",
                    color = textPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================== TABS ==================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, label ->
                    val selected = index == selectedTab
                    Button(
                        onClick = { selectedTab = index },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) AccentGreen else Color.Transparent,
                            contentColor = if (selected) Color.Black else textPrimary
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================== CARDS RESUMEN ARRIBA ==================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResumenMiniCard(
                    titulo = "Ingresos Totales",
                    monto = 2500.00,
                    variacion = +5.2,
                    positiveColor = AccentGreen,
                    icon = Icons.Default.ArrowUpward,
                    modifier = Modifier.weight(1f),
                    cardColor = cardColor,
                    textMutedColor = textMutedColor,
                    textPrimary = textPrimary
                )
                ResumenMiniCard(
                    titulo = "Egresos Totales",
                    monto = 1850.50,
                    variacion = +8.1,
                    positiveColor = AccentGreen,
                    icon = Icons.Default.ArrowDownward,
                    modifier = Modifier.weight(1f),
                    cardColor = cardColor,
                    textMutedColor = textMutedColor,
                    textPrimary = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ================== SALDO NETO ==================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Saldo Neto",
                        color = textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "S/ 649.50",
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "-2.5%",
                        color = AccentRed,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================== EVOLUCIÓN DEL SALDO ==================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Evolución del saldo",
                        color = textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "S/ 649.50",
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "1 Oct – 31 Oct   +S/ 50.10",
                        color = AccentGreen,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(softCardColor, RoundedCornerShape(14.dp))
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        LineChartSaldo(
                            points = listOf(30f, 70f, 55f, 90f, 40f, 35f, 80f),
                            lineColor = AccentGreen
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1", color = textMutedColor, fontSize = 10.sp)
                            Text("7", color = textMutedColor, fontSize = 10.sp)
                            Text("14", color = textMutedColor, fontSize = 10.sp)
                            Text("21", color = textMutedColor, fontSize = 10.sp)
                            Text("31", color = textMutedColor, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================== INGRESOS VS EGRESOS ==================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Ingresos vs. Egresos",
                        color = textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Julio – Octubre",
                        color = textMutedColor,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    BarChartIngresosEgresos(
                        meses = listOf("Jul", "Ago", "Sep", "Oct"),
                        ingresos = listOf(1800f, 2200f, 2000f, 2500f),
                        egresos = listOf(1400f, 1600f, 1500f, 1850f),
                        barBg = softCardColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ================== TOP CATEGORÍAS ==================
            Text(
                text = "Top Categorías",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            CategoriaRow(
                icon = Icons.Default.DirectionsCar,
                nombre = "Transporte",
                porcentaje = 24.3,
                monto = 450.0,
                variacion = +2.1,
                cardColor = cardColor,
                textMutedColor = textMutedColor,
                textPrimary = textPrimary
            )
            CategoriaRow(
                icon = Icons.Default.Restaurant,
                nombre = "Comida",
                porcentaje = 20.5,
                monto = 380.75,
                variacion = -1.5,
                cardColor = cardColor,
                textMutedColor = textMutedColor,
                textPrimary = textPrimary
            )
            CategoriaRow(
                icon = Icons.Default.CreditCard,
                nombre = "Deudas",
                porcentaje = 16.2,
                monto = 300.0,
                variacion = 0.0,
                cardColor = cardColor,
                textMutedColor = textMutedColor,
                textPrimary = textPrimary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ================== ANÁLISIS Y COMPARACIONES ==================
            Text(
                text = "Análisis y Comparaciones",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Este mes vs. mes pasado",
                        color = textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gastaste un ",
                        color = textMutedColor,
                        fontSize = 13.sp
                    )
                    Row {
                        Text(
                            text = "15% más ",
                            color = AccentRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "en la categoría de Comida.",
                            color = textMutedColor,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Promedio últimos 3 meses",
                        color = textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        Text(
                            text = "Tus ingresos están un ",
                            color = textMutedColor,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "8% por encima ",
                            color = AccentGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "del promedio.",
                            color = textMutedColor,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ================== FILTROS Y EXPORTAR ==================
            Text(
                text = "Filtros",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroChip(text = "Efectivo", selected = true, cardBg = softCardColor)
                FiltroChip(text = "Tarjeta", selected = false, cardBg = softCardColor)
                FiltroChip(text = "Deudas", selected = false, cardBg = softCardColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { /* TODO exportar */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(30.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.IosShare,
                        contentDescription = "Exportar"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// =============================================================
//                      COMPONENTES REUSABLES
// =============================================================

@Composable
private fun ResumenMiniCard(
    titulo: String,
    monto: Double,
    variacion: Double,
    positiveColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    cardColor: Color = CardDark,
    textMutedColor: Color = TextMuted,
    textPrimary: Color = Color.White
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = positiveColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = titulo,
                    color = textMutedColor,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "S/ ${"%,.2f".format(monto)}",
                color = textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            val colorVar = if (variacion >= 0) positiveColor else AccentRed
            val signo = if (variacion >= 0) "+" else ""
            Text(
                text = "$signo${"%.1f".format(variacion)}%",
                color = colorVar,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun LineChartSaldo(
    points: List<Float>,
    lineColor: Color,
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        if (points.isEmpty()) return@Canvas

        val maxVal = points.maxOrNull() ?: 1f
        val minVal = points.minOrNull() ?: 0f
        val range = (maxVal - minVal).takeIf { it > 0 } ?: 1f

        val path = Path()
        val stepX = size.width / (points.size - 1).coerceAtLeast(1)

        points.forEachIndexed { index, value ->
            val x = stepX * index
            val yRatio = (value - minVal) / range
            val y = size.height - (size.height * yRatio)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun BarChartIngresosEgresos(
    meses: List<String>,
    ingresos: List<Float>,
    egresos: List<Float>,
    barBg: Color
) {
    val maxValue = (ingresos + egresos).maxOrNull() ?: 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {

        meses.indices.forEach { i ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(80.dp * (ingresos[i] / maxValue))
                        .background(AccentGreen, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(80.dp * (egresos[i] / maxValue))
                        .background(AccentRed, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = meses[i],
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun CategoriaRow(
    icon: ImageVector,
    nombre: String,
    porcentaje: Double,
    monto: Double,
    variacion: Double,
    cardColor: Color,
    textMutedColor: Color,
    textPrimary: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(CardDarkSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = nombre,
                    color = textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = "S/ ${"%,.2f".format(monto)}",
                    color = textMutedColor,
                    fontSize = 12.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${"%.1f".format(porcentaje)}%",
                    color = textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                val colorVar = when {
                    variacion > 0 -> AccentGreen
                    variacion < 0 -> AccentRed
                    else -> textMutedColor
                }
                val signo = if (variacion > 0) "+" else ""
                Text(
                    text = "$signo${"%.1f".format(variacion)}%",
                    color = colorVar,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun FiltroChip(
    text: String,
    selected: Boolean,
    cardBg: Color
) {
    val bg = if (selected) AccentGreen else cardBg
    val contentColor = if (selected) Color.Black else Color.White

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
