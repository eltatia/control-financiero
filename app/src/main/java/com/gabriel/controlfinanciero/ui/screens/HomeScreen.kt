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
import androidx.compose.runtime.*
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
//                  MODELO: ÚLTIMOS MOVIMIENTOS (UI)
// =============================================================
data class MovimientoUi(
    val titulo: String,
    val fechaTexto: String,
    val monto: Double,
    val tipo: String // "INGRESO" o "EGRESO"
)

@Composable
fun MovimientoItem(mov: MovimientoUi, darkMode: Boolean) {
    val colorIngreso = if (darkMode) HomeAccentGreen else Color(0xFF16A34A)
    val colorEgreso = Color(0xFFEF4444)

    val colorMonto = if (mov.tipo == "INGRESO") colorIngreso else colorEgreso
    val signo = if (mov.tipo == "INGRESO") "+" else "−"

    val circleBg = if (mov.tipo == "INGRESO") {
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
                mov.fechaTexto,
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
data class RecordatorioUi(
    val titulo: String,
    val fecha: String,
    val monto: Double?
)

@Composable
fun RecordatorioItem(rec: RecordatorioUi, darkMode: Boolean) {
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
                rec.monto?.let { "S/ ${"%,.2f".format(it)}" } ?: "--",
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
//                 FORMATEO SENCILLO DE FECHAS
// =============================================================
private fun formatFechaCorta(millis: Long): String {
    val zone = ZoneId.systemDefault()
    val fecha = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    val hoy = LocalDate.now()
    val ayer = hoy.minusDays(1)

    return when (fecha) {
        hoy -> "Hoy"
        ayer -> "Ayer"
        else -> fecha.format(DateTimeFormatter.ofPattern("dd/MM"))
    }
}

// =============================================================
//                      HOME SCREEN COMPLETO
// =============================================================
@Composable
fun HomeScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    viewModel: FinanceViewModel = viewModel(factory = FinanceViewModel.Factory)
) {
    // ▶️ Datos reales desde el ViewModel / Room
    val totalIngresos by viewModel.totalIngresosMes.collectAsState()
    val totalEgresos by viewModel.totalEgresosMes.collectAsState()
    val balance by viewModel.balanceMes.collectAsState()
    val transaccionesMes by viewModel.transaccionesMes.collectAsState()
    val cuentas by viewModel.cuentas.collectAsState()
    val recordatoriosProximos by viewModel.recordatoriosProximos.collectAsState()

    // Cargar datos del mes actual al entrar a Home
    LaunchedEffect(Unit) {
        viewModel.cargarDatosMes()
    }

    // Estado para el diálogo de nuevo movimiento
    var showMovimientoDialog by remember { mutableStateOf(false) }
    var esIngreso by remember { mutableStateOf(true) }
    var tituloMov by remember { mutableStateOf("") }
    var montoMovTexto by remember { mutableStateOf("") }
    var categoriaMov by remember { mutableStateOf("") }
    var cuentaIndex by remember { mutableStateOf(0) }

    val bgColor = if (isDarkMode) HomeDarkBackground else Color(0xFFF3F6FF)
    val cardColor = if (isDarkMode) HomeDarkCard else Color.White
    val softCard = if (isDarkMode) HomeDarkSoft else Color(0xFFECECEC)
    val textPrimary = if (isDarkMode) Color.White else Color.Black
    val textSecondary = if (isDarkMode) HomeTextMuted else Color.Gray

    val movimientosUi = transaccionesMes
        .sortedByDescending { it.fecha }
        .take(5)
        .map {
            MovimientoUi(
                titulo = it.titulo,
                fechaTexto = formatFechaCorta(it.fecha),
                monto = it.monto,
                tipo = it.tipo
            )
        }

    val egresosPorCategoria = transaccionesMes
        .filter { it.tipo == "EGRESO" }
        .groupBy { it.categoria.ifBlank { "Otros" } }
        .mapValues { (_, items) -> items.sumOf { it.monto } }
        .toList()
        .sortedByDescending { it.second }

    val topCategorias = egresosPorCategoria.take(4).toMutableList()
    val restantes = egresosPorCategoria.drop(4).sumOf { it.second }
    if (restantes > 0.0) {
        topCategorias.add("Otros" to restantes)
    }

    val donutColors = listOf(
        Color(0xFFE74C3C),
        Color(0xFF3498DB),
        Color(0xFFF1C40F),
        Color(0xFF9B59B6),
        Color(0xFF10B981)
    )

    val donutValues = if (topCategorias.isNotEmpty()) {
        topCategorias.map { it.second.toFloat() }
    } else {
        listOf(0f)
    }

    val recordatoriosUi = recordatoriosProximos.map { recordatorio ->
        val fecha = formatFechaCorta(recordatorio.fechaMillis)
        RecordatorioUi(
            titulo = recordatorio.titulo,
            fecha = fecha,
            monto = recordatorio.monto
        )
    }

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
                        esIngreso = true
                        showMovimientoDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(HomeAccentGreen),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("+ Añadir Ingreso") }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        esIngreso = false
                        showMovimientoDialog = true
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
                                // De momento valores de ejemplo; centro usa totalEgresos real
                                values = donutValues,
                                colors = donutColors,
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
                            if (topCategorias.isEmpty()) {
                                Text(
                                    text = "Sin egresos este mes.",
                                    color = textSecondary,
                                    fontSize = 13.sp
                                )
                            } else {
                                topCategorias.forEachIndexed { index, (label, amount) ->
                                    LegendRow(
                                        label = label,
                                        amount = "S/ ${"%,.2f".format(amount)}",
                                        color = donutColors[index % donutColors.size],
                                        darkMode = isDarkMode
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ================= ÚLTIMOS MOVIMIENTOS (REALES) =================
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

                    if (movimientosUi.isEmpty()) {
                        Text(
                            text = "Aún no has registrado movimientos este mes.",
                            color = textSecondary,
                            fontSize = 14.sp
                        )
                    } else {
                        movimientosUi.forEachIndexed { index, mov ->
                            MovimientoItem(mov, darkMode = isDarkMode)
                            if (index != movimientosUi.lastIndex)
                                Divider(
                                    color = if (isDarkMode) HomeDarkSoft else Color(0xFFECECEC),
                                    thickness = 1.dp
                                )
                        }
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

                    if (recordatoriosUi.isEmpty()) {
                        Text(
                            text = "No tienes recordatorios próximos.",
                            color = textSecondary,
                            fontSize = 14.sp
                        )
                    } else {
                        recordatoriosUi.forEach { rec ->
                            RecordatorioItem(rec, darkMode = isDarkMode)
                        }
                    }
                }
            }
        }
    }

    // ================= DIÁLOGO NUEVO MOVIMIENTO =================
    if (showMovimientoDialog) {
        AlertDialog(
            onDismissRequest = { showMovimientoDialog = false },
            title = { Text(if (esIngreso) "Nuevo ingreso" else "Nuevo egreso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tituloMov,
                        onValueChange = { tituloMov = it },
                        label = { Text("Título") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = montoMovTexto,
                        onValueChange = { montoMovTexto = it },
                        label = { Text("Monto (S/)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = categoriaMov,
                        onValueChange = { categoriaMov = it },
                        label = { Text("Categoría (ej. Comida)") },
                        singleLine = true
                    )

                    if (cuentas.isEmpty()) {
                        Text(
                            "Primero crea una cuenta en la pestaña Cuentas para poder asociar el movimiento.",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            "Cuenta",
                            style = MaterialTheme.typography.labelMedium
                        )
                        // selector súper simple por ahora (solo texto con índice)
                        DropdownMenuCuentaSelector(
                            cuentasNombres = cuentas.map { it.nombre },
                            selectedIndex = cuentaIndex,
                            onIndexSelected = { cuentaIndex = it }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = cuentas.isNotEmpty(),
                    onClick = {
                        val monto = montoMovTexto.replace(",", ".")
                            .toDoubleOrNull() ?: 0.0
                        val tituloFinal =
                            if (tituloMov.isBlank())
                                if (esIngreso) "Ingreso" else "Egreso"
                            else
                                tituloMov.trim()
                        val categoriaFinal =
                            if (categoriaMov.isBlank())
                                if (esIngreso) "General" else "Gasto"
                            else
                                categoriaMov.trim()

                        val cuentaId = cuentas.getOrNull(cuentaIndex)?.id ?: 0

                        viewModel.registrarTransaccion(
                            titulo = tituloFinal,
                            monto = monto,
                            tipo = if (esIngreso) "INGRESO" else "EGRESO",
                            categoria = categoriaFinal,
                            cuentaId = cuentaId
                        )

                        // reset
                        tituloMov = ""
                        montoMovTexto = ""
                        categoriaMov = ""
                        cuentaIndex = 0
                        showMovimientoDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMovimientoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// =============================================================
//        SELECTOR SIMPLE DE CUENTAS PARA EL DIÁLOGO
// =============================================================
@Composable
private fun DropdownMenuCuentaSelector(
    cuentasNombres: List<String>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { expanded = true }
    ) {
        Text(
            text = cuentasNombres.getOrNull(selectedIndex) ?: "Seleccionar cuenta",
            modifier = Modifier.weight(1f)
        )
        Text("▾")
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        cuentasNombres.forEachIndexed { index, nombre ->
            DropdownMenuItem(
                text = { Text(nombre) },
                onClick = {
                    onIndexSelected(index)
                    expanded = false
                }
            )
        }
    }
}
