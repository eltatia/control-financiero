package com.gabriel.controlfinanciero.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabriel.controlfinanciero.viewmodel.FinanceViewModel

// Colores modo oscuro para Home (parecidos a Reportes/Calendario)
private val HomeDarkBackground = Color(0xFF021712)
private val HomeDarkCard = Color(0xFF071E1A)
private val HomeDarkSoft = Color(0xFF0A2320)
private val HomeAccentGreen = Color(0xFF22C55E)
private val HomeTextMuted = Color(0xFF9CA3AF)

// =============================================================
//                 DONUT CHART PREMIUM (ANIMADO)
// =============================================================
@Composable
fun DonutChartPremium(
    values: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 45f,
    animate: Boolean = true,
    innerColor: Color = Color.White
) {
    val total = values.sum().takeIf { it > 0 } ?: 1f
    val animatedSweep = remember { Animatable(0f) }

    LaunchedEffect(values, animate) {
        if (animate) {
            animatedSweep.snapTo(0f)
            animatedSweep.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1200,
                    easing = FastOutSlowInEasing
                )
            )
        } else {
            animatedSweep.snapTo(1f)
        }
    }

    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val radius = diameter / 2f
        val arcSize = Size(diameter, diameter)
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )

        var startAngle = -90f

        values.forEachIndexed { index, value ->
            val proportion = value / total
            val sweep = proportion * 360f * animatedSweep.value

            drawArc(
                color = colors.getOrElse(index) { Color.LightGray },
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            startAngle += proportion * 360f
        }

        // Centro del donut según modo
        drawCircle(
            color = innerColor,
            radius = radius - strokeWidth,
            center = center
        )

        drawCircle(
            color = Color(0x11000000),
            radius = radius - strokeWidth - 6,
            center = center
        )
    }
}

// =============================================================
//                  MODELO: ÚLTIMOS MOVIMIENTOS
// =============================================================
data class Movimiento(
    val titulo: String,
    val fecha: String,
    val monto: Float,
    val tipo: String // "ingreso" o "egreso"
)

val movimientosRecientes = listOf(
    Movimiento("Salario", "Hoy", 2500f, "ingreso"),
    Movimiento("Gasolina", "Ayer", 45.50f, "egreso"),
    Movimiento("Cena", "Hace 2 días", 82.30f, "egreso")
)

@Composable
fun MovimientoItem(mov: Movimiento, darkMode: Boolean) {
    val colorIngreso = if (darkMode) HomeAccentGreen else Color(0xFF16A34A)
    val colorEgreso = Color(0xFFEF4444)

    val colorMonto = if (mov.tipo == "ingreso") colorIngreso else colorEgreso
    val signo = if (mov.tipo == "ingreso") "+" else "−"

    val circleBg = if (mov.tipo == "ingreso") {
        if (darkMode) Color(0xFF064E3B) else Color(0xFFDFF6E6)
    } else {
        if (darkMode) Color(0xFF7F1D1D) else Color(0xFFFBE3E3)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(circleBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                signo,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorMonto
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                mov.titulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (darkMode) Color.White else Color.Black
            )
            Text(
                mov.fecha,
                color = if (darkMode) HomeTextMuted else Color.Gray,
                fontSize = 13.sp
            )
        }

        Text(
            text = "$signo S/ ${"%,.2f".format(mov.monto)}",
            fontWeight = FontWeight.Bold,
            color = colorMonto,
            fontSize = 15.sp
        )
    }
}

// =============================================================
//                  MODELO: RECORDATORIOS
// =============================================================
data class Recordatorio(
    val titulo: String,
    val fecha: String,
    val monto: Float
)

val listaRecordatorios = listOf(
    Recordatorio("Pago Internet", "Mañana", 60f),
    Recordatorio("Pago Luz", "En 3 días", 45f),
    Recordatorio("Cuota Moto", "En 5 días", 150f)
)

@Composable
fun RecordatorioItem(rec: Recordatorio, darkMode: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkMode) HomeDarkSoft else Color(0xFFF7F8FA)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(
                        if (darkMode) Color(0xFF1E293B) else Color(0xFFCCE0FF),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("⏰", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    rec.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (darkMode) Color.White else Color.Black
                )
                Text(
                    "Vence: ${rec.fecha}",
                    color = if (darkMode) HomeTextMuted else Color.Gray,
                    fontSize = 13.sp
                )
            }

            Text(
                "S/ ${rec.monto}",
                fontWeight = FontWeight.Bold,
                color = if (darkMode) HomeAccentGreen else Color(0xFF3C67FF)
            )
        }
    }
}

// =============================================================
//                    LEYENDA DEL DONUT
// =============================================================
@Composable
private fun LegendRow(label: String, amount: String, color: Color, darkMode: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            label,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = if (darkMode) Color.White else Color.Black
        )
        Text(
            amount,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (darkMode) Color.White else Color.Black
        )
    }
}

// =============================================================
//                      HOME SCREEN COMPLETO
// =============================================================
@Composable
fun HomeScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    viewModel: FinanceViewModel = viewModel()
) {
    // ▶️ Datos reales desde el ViewModel / Room
    val totalIngresos by viewModel.totalIngresosMes.collectAsState()
    val totalEgresos by viewModel.totalEgresosMes.collectAsState()
    val balance by viewModel.balanceMes.collectAsState()

    // Cargar datos del mes actual al entrar a Home
    LaunchedEffect(Unit) {
        viewModel.cargarDatosMes()
    }

    val bgColor = if (isDarkMode) HomeDarkBackground else Color(0xFFF3F6FF)
    val cardColor = if (isDarkMode) HomeDarkCard else Color.White
    val softCard = if (isDarkMode) HomeDarkSoft else Color(0xFFECECEC)
    val textPrimary = if (isDarkMode) Color.White else Color.Black
    val textSecondary = if (isDarkMode) HomeTextMuted else Color.Gray

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // ================= ENCABEZADO =================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(
                            if (isDarkMode) Color(0xFF1E293B) else Color(0xFFCDE2FF),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) HomeAccentGreen else Color(0xFF1D4ED8)
                    )
                }

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        "Hola,",
                        color = textSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        "Alex",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón modo claro/oscuro 🌙 / ☀️
                IconButton(onClick = { onToggleDarkMode() }) {
                    Text(if (isDarkMode) "☀️" else "🌙", fontSize = 20.sp)
                }

                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Config",
                        tint = if (isDarkMode) Color.White else Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ================= BALANCE GENERAL =================
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Balance General", color = textSecondary)
                    Text(
                        // 👉 Balance real del mes
                        "S/ ${"%,.2f".format(balance)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ================= BOTONES =================
            Row(Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        // Aquí luego llamaremos a registrarTransaccion (INGRESO)
                        // viewModel.registrarTransaccion(...)
                    },
                    colors = ButtonDefaults.buttonColors(HomeAccentGreen),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("+ Añadir Ingreso") }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        // Aquí luego llamaremos a registrarTransaccion (EGRESO)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = softCard,
                        contentColor = if (isDarkMode) Color.White else Color.Black
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("− Añadir Egreso") }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ================= DISTRIBUCIÓN DE GASTOS =================
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Distribución de Gastos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textPrimary
                        )
                        TextButton(onClick = { }) {
                            Text(
                                "Ver todo",
                                color = if (isDarkMode) HomeAccentGreen else Color(0xFF3C67FF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // DONUT
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChartPremium(
                                // Por ahora valores de ejemplo; luego se pueden mapear a categorías reales
                                values = listOf(350f, 280f, 150f, 60f),
                                colors = listOf(
                                    Color(0xFFE74C3C), // Transporte
                                    Color(0xFF3498DB), // Comida
                                    Color(0xFFF1C40F), // Ocio
                                    Color(0xFF9B59B6)  // Otros
                                ),
                                modifier = Modifier.size(140.dp),
                                innerColor = if (isDarkMode) cardColor else Color.White
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total gastado", color = textSecondary, fontSize = 12.sp)
                                Text(
                                    // 👉 Total de Egresos del mes
                                    "S/ ${"%,.2f".format(totalEgresos)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // LEYENDA
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LegendRow("Transporte", "S/ 350.00", Color(0xFFE74C3C), isDarkMode)
                            LegendRow("Comida", "S/ 280.25", Color(0xFF3498DB), isDarkMode)
                            LegendRow("Ocio", "S/ 150.25", Color(0xFFF1C40F), isDarkMode)
                            LegendRow("Otros", "S/ 60.00", Color(0xFF9B59B6), isDarkMode)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ================= ÚLTIMOS MOVIMIENTOS =================
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Últimos Movimientos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textPrimary
                        )
                        TextButton(onClick = {}) {
                            Text(
                                "Ver todo",
                                color = if (isDarkMode) HomeAccentGreen else Color(0xFF3C67FF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    movimientosRecientes.forEachIndexed { index, mov ->
                        MovimientoItem(mov, darkMode = isDarkMode)
                        if (index != movimientosRecientes.lastIndex)
                            Divider(
                                color = if (isDarkMode) HomeDarkSoft else Color(0xFFECECEC),
                                thickness = 1.dp
                            )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ================= RECORDATORIOS =================
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {

                    Text(
                        "Recordatorios",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    listaRecordatorios.forEach { rec ->
                        RecordatorioItem(rec, darkMode = isDarkMode)
                    }
                }
            }
        }
    }
}

