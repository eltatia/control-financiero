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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabriel.controlfinanciero.ui.components.PrimaryActionButton
import com.gabriel.controlfinanciero.ui.components.SecondaryActionButton
import com.gabriel.controlfinanciero.viewmodel.FinanceViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.YearMonth

// Colores modo oscuro para Home (parecidos a Reportes/Calendario)
private val HomeDarkBackground = Color(0xFF0B111A)
private val HomeDarkCard = Color(0xFF111827)
private val HomeDarkSoft = Color(0xFF1F2A37)
private val HomeAccentGreen = Color(0xFF3AD19F)
private val HomeAccentRed = Color(0xFFF87171)
private val HomeTextMuted = Color(0xFFB6C2D2)

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
    val colorScheme = MaterialTheme.colorScheme
    val colorIngreso = colorScheme.secondary
    val colorEgreso = colorScheme.error

    val colorMonto = if (mov.tipo == "INGRESO") colorIngreso else colorEgreso
    val signo = if (mov.tipo == "INGRESO") "+" else "−"

    val circleBg = if (mov.tipo == "INGRESO") {
        if (darkMode) Color(0xFF0B3F2E) else Color(0xFFDDF5EC)
    } else {
        if (darkMode) Color(0xFF4C1D1D) else Color(0xFFFCE8E8)
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
                color = colorScheme.onSurface
            )
            Text(
                mov.fechaTexto,
                color = colorScheme.onSurfaceVariant,
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
@OptIn(ExperimentalMaterial3Api::class)
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
    val saldoActualCuentas by viewModel.saldoActualCuentas.collectAsState()
    val cuentas by viewModel.cuentas.collectAsState()
    val todasTransacciones by viewModel.todasTransacciones.collectAsState()
    val recordatoriosProximos by viewModel.recordatoriosProximos.collectAsState()
    val nombreUsuario by viewModel.nombreUsuario.collectAsState()
    val cuentaActual by viewModel.cuentaActual.collectAsState()
    val cuentaActualId by viewModel.cuentaActualId.collectAsState()

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
    var showSettingsSheet by remember { mutableStateOf(false) }
    var nombreUsuarioInput by remember { mutableStateOf("") }
    var showAccountMenu by remember { mutableStateOf(false) }
    var showGastosAll by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val bgColor = colorScheme.background
    val cardColor = colorScheme.surface
    val softCard = colorScheme.surfaceVariant
    val textPrimary = colorScheme.onBackground
    val textSecondary = colorScheme.onSurfaceVariant

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

    val cuentaIds = cuentas.map { it.id }.toSet()
    val ahora = YearMonth.now()
    val zoneId = ZoneId.systemDefault()
    val monthStart = ahora.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val monthEnd = ahora.atEndOfMonth().plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

    val transaccionesMesTodas = todasTransacciones.filter { transaccion ->
        transaccion.fecha in monthStart..monthEnd && transaccion.cuentaId in cuentaIds
    }

    val egresosPorCategoriaCuenta = transaccionesMes
        .filter { it.tipo == "EGRESO" }
        .groupBy { it.categoria.ifBlank { "Otros" } }
        .mapValues { (_, items) -> items.sumOf { it.monto } }
        .toList()
        .sortedByDescending { it.second }

    val egresosPorCategoriaTodas = transaccionesMesTodas
        .filter { it.tipo == "EGRESO" }
        .groupBy { it.categoria.ifBlank { "Otros" } }
        .mapValues { (_, items) -> items.sumOf { it.monto } }
        .toList()
        .sortedByDescending { it.second }

    val topCategoriasCuenta = egresosPorCategoriaCuenta.take(4).toMutableList()
    val restantesCuenta = egresosPorCategoriaCuenta.drop(4).sumOf { it.second }
    if (restantesCuenta > 0.0) {
        topCategoriasCuenta.add("Otros" to restantesCuenta)
    }

    val topCategoriasTodas = egresosPorCategoriaTodas.take(4).toMutableList()
    val restantesTodas = egresosPorCategoriaTodas.drop(4).sumOf { it.second }
    if (restantesTodas > 0.0) {
        topCategoriasTodas.add("Otros" to restantesTodas)
    }

    val donutColors = listOf(
        colorScheme.primary,
        colorScheme.secondary,
        colorScheme.tertiary,
        Color(0xFF7C3AED),
        Color(0xFFF59E0B)
    )

    val donutValuesCuenta = if (topCategoriasCuenta.isNotEmpty()) {
        topCategoriasCuenta.map { it.second.toFloat() }
    } else {
        listOf(0f)
    }

    val donutValuesTodas = if (topCategoriasTodas.isNotEmpty()) {
        topCategoriasTodas.map { it.second.toFloat() }
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

    val saldoCuentaActual = cuentaActual?.let { cuenta ->
        val movimiento = todasTransacciones
            .filter { it.cuentaId == cuenta.id }
            .sumOf { trans -> if (trans.tipo == "INGRESO") trans.monto else -trans.monto }
        cuenta.saldoInicial + movimiento
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
                            colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nombreUsuario.firstOrNull()?.uppercase() ?: "C",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimaryContainer
                    )
                }

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        "Hola,",
                        color = textSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        nombreUsuario,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    cuentaActual?.let { cuenta ->
                        Text(
                            text = cuenta.nombre,
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón modo claro/oscuro 🌙 / ☀️
                IconButton(onClick = { onToggleDarkMode() }) {
                    Text(if (isDarkMode) "☀️" else "🌙", fontSize = 20.sp)
                }

                IconButton(onClick = {
                    nombreUsuarioInput = nombreUsuario
                    showSettingsSheet = true
                }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Config",
                        tint = colorScheme.onSurface
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Cuenta:",
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                        TextButton(onClick = { showAccountMenu = true }) {
                            Text(
                                text = cuentaActual?.nombre ?: "Todas",
                                color = HomeAccentGreen,
                                fontSize = 12.sp
                            )
                        }
                        DropdownMenu(
                            expanded = showAccountMenu,
                            onDismissRequest = { showAccountMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas") },
                                onClick = {
                                    showAccountMenu = false
                                    viewModel.setCuentaActual(0)
                                }
                            )
                            cuentas.forEach { cuenta ->
                                DropdownMenuItem(
                                    text = { Text(cuenta.nombre) },
                                    onClick = {
                                        showAccountMenu = false
                                        viewModel.setCuentaActual(cuenta.id)
                                    }
                                )
                            }
                        }
                    }
                    Text(
                        // 👉 Balance real del mes
                        "S/ ${"%,.2f".format(balance)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val saldoCuentaSeleccionada = if (cuentaActualId == 0) {
                        saldoActualCuentas
                    } else {
                        saldoCuentaActual ?: 0.0
                    }
                    Text(
                        text = "Saldo cuenta: S/ ${"%,.2f".format(saldoCuentaSeleccionada)}",
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Saldo total: S/ ${"%,.2f".format(saldoActualCuentas)}",
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ================= BOTONES =================
            Row(Modifier.fillMaxWidth()) {
                PrimaryActionButton(
                    onClick = {
                        esIngreso = true
                        cuentaIndex = cuentas.indexOfFirst { it.id == (cuentaActual?.id ?: 0) }
                            .takeIf { it >= 0 } ?: 0
                        showMovimientoDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(HomeAccentGreen),
                    text = "+ Añadir Ingreso"
                )

                Spacer(modifier = Modifier.width(10.dp))

                SecondaryActionButton(
                    onClick = {
                        esIngreso = false
                        cuentaIndex = cuentas.indexOfFirst { it.id == (cuentaActual?.id ?: 0) }
                            .takeIf { it >= 0 } ?: 0
                        showMovimientoDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = softCard,
                        contentColor = if (isDarkMode) Color.White else Color.Black
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    text = "− Añadir Egreso"
                )
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
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { showGastosAll = false }) {
                                Text(
                                    text = "Cuenta",
                                    color = if (!showGastosAll) HomeAccentGreen else textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            TextButton(onClick = { showGastosAll = true }) {
                                Text(
                                    text = "Todas",
                                    color = if (showGastosAll) HomeAccentGreen else textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Text(
                            "Ver todo",
                            color = if (isDarkMode) HomeAccentGreen else Color(0xFF3C67FF),
                            fontSize = 12.sp
                        )
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
                                values = if (showGastosAll) donutValuesTodas else donutValuesCuenta,
                                colors = donutColors,
                                modifier = Modifier.size(140.dp),
                                innerColor = if (isDarkMode) cardColor else Color.White
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total gastado", color = textSecondary, fontSize = 12.sp)
                                Text(
                                    // 👉 Total de Egresos del mes
                                    "S/ ${"%,.2f".format(if (showGastosAll) transaccionesMesTodas.filter { it.tipo == "EGRESO" }.sumOf { it.monto } else totalEgresos)}",
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
                            val categorias = if (showGastosAll) topCategoriasTodas else topCategoriasCuenta
                            if (categorias.isEmpty()) {
                                Text(
                                    text = "Sin egresos este mes.",
                                    color = textSecondary,
                                    fontSize = 13.sp
                                )
                            } else {
                                categorias.forEachIndexed { index, (label, amount) ->
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
                        Text(
                            "Ver todo",
                            color = if (isDarkMode) HomeAccentGreen else Color(0xFF3C67FF),
                            fontSize = 12.sp
                        )
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

    if (showSettingsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = sheetState,
            containerColor = cardColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ajustes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                cuentaActual?.let { cuenta ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = softCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Cuenta actual",
                                color = textSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = cuenta.nombre,
                                color = textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Saldo: S/ ${"%,.2f".format(saldoCuentaActual ?: cuenta.saldoInicial)}",
                                color = textSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = nombreUsuarioInput,
                    onValueChange = { nombreUsuarioInput = it },
                    label = { Text("Nombre de usuario") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = {
                        viewModel.logout()
                        showSettingsSheet = false
                    }
                ) {
                    Text(text = "Cerrar sesión", color = HomeAccentRed)
                }

                TextButton(
                    onClick = {
                        viewModel.setNombreUsuario(nombreUsuarioInput.trim().ifBlank { "Usuario" })
                        showSettingsSheet = false
                    }
                ) {
                    Text(text = "Guardar", color = HomeAccentGreen)
                }
            }
        }
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
