package com.gabriel.controlfinanciero.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabriel.controlfinanciero.viewmodel.FinanceViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
fun ReportesScreen(
    isDarkMode: Boolean,
    viewModel: FinanceViewModel = viewModel(factory = FinanceViewModel.Factory)
) {

    val bgColor = if (isDarkMode) ReportsBackground else Color(0xFFF3F6FF)
    val cardColor = if (isDarkMode) CardDark else Color.White
    val softCardColor = if (isDarkMode) CardDarkSoft else Color(0xFFE5E7EB)
    val textMutedColor = if (isDarkMode) TextMuted else Color.Gray
    val textPrimary = if (isDarkMode) Color.White else Color.Black
    val context = LocalContext.current
    var exportMenuExpanded by remember { mutableStateOf(false) }
    var pendingCsv by remember { mutableStateOf("") }

    val transacciones by viewModel.todasTransacciones.collectAsState()
    val cuentas by viewModel.cuentas.collectAsState()
    val cuentaActualId by viewModel.cuentaActualId.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Mes actual", "Mes anterior", "Año actual", "Personalizado")

    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now()
    val selectedRange by remember(selectedTab, today) {
        derivedStateOf {
            when (selectedTab) {
                0 -> rangeForMonth(YearMonth.from(today))
                1 -> rangeForMonth(YearMonth.from(today).minusMonths(1))
                2 -> rangeForYear(today.year)
                else -> rangeForMonth(YearMonth.from(today))
            }
        }
    }
    val previousRange by remember(selectedTab, today) {
        derivedStateOf {
            when (selectedTab) {
                0 -> rangeForMonth(YearMonth.from(today).minusMonths(1))
                1 -> rangeForMonth(YearMonth.from(today).minusMonths(2))
                2 -> rangeForYear(today.year - 1)
                else -> rangeForMonth(YearMonth.from(today).minusMonths(1))
            }
        }
    }

    val cuentaIds = remember(cuentas, cuentaActualId) {
        if (cuentaActualId != 0) {
            setOf(cuentaActualId)
        } else {
            cuentas.map { it.id }.toSet()
        }
    }
    val transaccionesRango = remember(transacciones, selectedRange, cuentaIds) {
        transacciones.filter { transaccion ->
            val fecha = Instant.ofEpochMilli(transaccion.fecha).atZone(zoneId).toLocalDate()
            !fecha.isBefore(selectedRange.first) &&
                !fecha.isAfter(selectedRange.second) &&
                transaccion.cuentaId in cuentaIds
        }
    }
    val transaccionesPrevias = remember(transacciones, previousRange, cuentaIds) {
        transacciones.filter { transaccion ->
            val fecha = Instant.ofEpochMilli(transaccion.fecha).atZone(zoneId).toLocalDate()
            !fecha.isBefore(previousRange.first) &&
                !fecha.isAfter(previousRange.second) &&
                transaccion.cuentaId in cuentaIds
        }
    }

    val ingresos = transaccionesRango.filter { it.tipo == "INGRESO" }.sumOf { it.monto }
    val egresos = transaccionesRango.filter { it.tipo == "EGRESO" }.sumOf { it.monto }
    val saldoNeto = ingresos - egresos

    val ingresosPrev = transaccionesPrevias.filter { it.tipo == "INGRESO" }.sumOf { it.monto }
    val egresosPrev = transaccionesPrevias.filter { it.tipo == "EGRESO" }.sumOf { it.monto }
    val saldoPrev = ingresosPrev - egresosPrev

    val variacionIngresos = porcentajeVariacion(ingresos, ingresosPrev)
    val variacionEgresos = porcentajeVariacion(egresos, egresosPrev)
    val variacionSaldo = porcentajeVariacion(saldoNeto, saldoPrev)

    val egresosPorCategoria = transaccionesRango
        .filter { it.tipo == "EGRESO" }
        .groupBy { it.categoria.ifBlank { "Otros" } }
        .mapValues { (_, items) -> items.sumOf { it.monto } }
        .toList()
        .sortedByDescending { it.second }

    val topCategorias = egresosPorCategoria.take(3)
    val totalEgresos = egresos.takeIf { it > 0.0 } ?: 1.0

    val chartPoints = remember(transaccionesRango, selectedRange) {
        buildSaldoSeries(transaccionesRango, selectedRange.first, selectedRange.second)
    }
    val chartLabels = remember(selectedRange) {
        buildChartLabels(selectedRange.first, selectedRange.second)
    }

    val mesesSerie = remember(transacciones, cuentaIds) {
        val meses = (0..3).map { YearMonth.from(today).minusMonths((3 - it).toLong()) }
        val ingresosSerie = meses.map { month ->
            transacciones
                .filter { it.tipo == "INGRESO" }
                .filter { Instant.ofEpochMilli(it.fecha).atZone(zoneId).toLocalDate().let { date ->
                    YearMonth.from(date) == month
                } && it.cuentaId in cuentaIds }
                .sumOf { it.monto }
                .toFloat()
        }
        val egresosSerie = meses.map { month ->
            transacciones
                .filter { it.tipo == "EGRESO" }
                .filter { Instant.ofEpochMilli(it.fecha).atZone(zoneId).toLocalDate().let { date ->
                    YearMonth.from(date) == month
                } && it.cuentaId in cuentaIds }
                .sumOf { it.monto }
                .toFloat()
        }
        Triple(
            meses.map { it.format(DateTimeFormatter.ofPattern("MMM")) },
            ingresosSerie,
            egresosSerie
        )
    }
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val csvContent = remember(transaccionesRango, cuentas, formatter) {
        buildCsvContent(transaccionesRango, cuentas, zoneId, formatter)
    }
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val writeResult = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(pendingCsv.toByteArray())
            }
        }
        if (writeResult.isFailure) {
            Toast.makeText(context, "No se pudo guardar el archivo.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Archivo guardado.", Toast.LENGTH_SHORT).show()
        }
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
                    monto = ingresos,
                    variacion = variacionIngresos,
                    positiveColor = AccentGreen,
                    icon = Icons.Default.ArrowUpward,
                    modifier = Modifier.weight(1f),
                    cardColor = cardColor,
                    textMutedColor = textMutedColor,
                    textPrimary = textPrimary
                )
                ResumenMiniCard(
                    titulo = "Egresos Totales",
                    monto = egresos,
                    variacion = variacionEgresos,
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
                        text = "S/ ${"%,.2f".format(saldoNeto)}",
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatVariacion(variacionSaldo),
                        color = if (variacionSaldo >= 0) AccentGreen else AccentRed,
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
                        text = "S/ ${"%,.2f".format(saldoNeto)}",
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatRangeLabel(selectedRange.first, selectedRange.second)}   ${formatSaldoDelta(saldoNeto - saldoPrev)}",
                        color = if (saldoNeto - saldoPrev >= 0) AccentGreen else AccentRed,
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
                            points = chartPoints,
                            lineColor = AccentGreen
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            chartLabels.forEach { label ->
                                Text(label, color = textMutedColor, fontSize = 10.sp)
                            }
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
                        meses = mesesSerie.first,
                        ingresos = mesesSerie.second,
                        egresos = mesesSerie.third,
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

            if (topCategorias.isEmpty()) {
                Text(
                    text = "Sin egresos registrados en este periodo.",
                    color = textMutedColor,
                    fontSize = 13.sp
                )
            } else {
                topCategorias.forEachIndexed { index, (nombre, monto) ->
                    val porcentaje = monto / totalEgresos * 100
                    CategoriaRow(
                        icon = when (index) {
                            0 -> Icons.Default.DirectionsCar
                            1 -> Icons.Default.Restaurant
                            else -> Icons.Default.CreditCard
                        },
                        nombre = nombre,
                        porcentaje = porcentaje,
                        monto = monto,
                        variacion = 0.0,
                        cardColor = cardColor,
                        textMutedColor = textMutedColor,
                        textPrimary = textPrimary
                    )
                }
            }

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
                    val comparacionTexto = if (variacionEgresos >= 0) "gastaste más" else "gastaste menos"
                    Text(
                        text = "Comparado al periodo anterior, $comparacionTexto:",
                        color = textMutedColor,
                        fontSize = 13.sp
                    )
                    Text(
                        text = formatVariacion(kotlin.math.abs(variacionEgresos)),
                        color = if (variacionEgresos >= 0) AccentRed else AccentGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
                        val promedioTexto = if (variacionIngresos >= 0) "por encima" else "por debajo"
                        Text(
                            text = "Tus ingresos están ${formatVariacion(kotlin.math.abs(variacionIngresos))} $promedioTexto del periodo anterior.",
                            color = if (variacionIngresos >= 0) AccentGreen else AccentRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
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
                Box {
                    Button(
                        onClick = { exportMenuExpanded = true },
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

                    DropdownMenu(
                        expanded = exportMenuExpanded,
                        onDismissRequest = { exportMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Compartir") },
                            onClick = {
                                exportMenuExpanded = false
                                if (transaccionesRango.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "No hay transacciones para exportar.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@DropdownMenuItem
                                }
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "Reporte de transacciones")
                                    putExtra(Intent.EXTRA_TEXT, csvContent)
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, "Exportar reporte")
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Guardar") },
                            onClick = {
                                exportMenuExpanded = false
                                if (transaccionesRango.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "No hay transacciones para exportar.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@DropdownMenuItem
                                }
                                pendingCsv = csvContent
                                createDocumentLauncher.launch("reporte-transacciones.csv")
                            }
                        )
                    }
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

private fun csvEscape(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
        "\"$escaped\""
    } else {
        escaped
    }
}

private fun buildCsvContent(
    transacciones: List<com.gabriel.controlfinanciero.data.local.entities.TransaccionEntity>,
    cuentas: List<com.gabriel.controlfinanciero.data.local.entities.CuentaEntity>,
    zoneId: ZoneId,
    formatter: DateTimeFormatter
): String {
    val cuentasPorId = cuentas.associateBy { it.id }
    return buildString {
        append("Fecha,Título,Categoría,Tipo,Cuenta,Monto,Nota\n")
        transacciones.sortedBy { it.fecha }.forEach { transaccion ->
            val fecha = Instant.ofEpochMilli(transaccion.fecha)
                .atZone(zoneId)
                .toLocalDate()
                .format(formatter)
            val cuentaNombre = cuentasPorId[transaccion.cuentaId]?.nombre
                ?: "Cuenta ${transaccion.cuentaId}"
            val nota = transaccion.nota ?: ""
            append(
                listOf(
                    fecha,
                    transaccion.titulo,
                    transaccion.categoria,
                    transaccion.tipo,
                    cuentaNombre,
                    "%.2f".format(transaccion.monto),
                    nota
                ).joinToString(",") { value -> csvEscape(value) }
            )
            append("\n")
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

private fun rangeForMonth(month: YearMonth): Pair<LocalDate, LocalDate> {
    return month.atDay(1) to month.atEndOfMonth()
}

private fun rangeForYear(year: Int): Pair<LocalDate, LocalDate> {
    return LocalDate.of(year, 1, 1) to LocalDate.of(year, 12, 31)
}

private fun porcentajeVariacion(actual: Double, anterior: Double): Double {
    if (anterior == 0.0) return if (actual == 0.0) 0.0 else 100.0
    return ((actual - anterior) / anterior) * 100
}

private fun formatVariacion(valor: Double): String {
    val signo = if (valor >= 0) "+" else ""
    return "$signo${"%.1f".format(valor)}%"
}

private fun formatSaldoDelta(delta: Double): String {
    val signo = if (delta >= 0) "+" else "-"
    return "$signo S/ ${"%,.2f".format(kotlin.math.abs(delta))}"
}

private fun formatRangeLabel(desde: LocalDate, hasta: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM")
    return "${desde.format(formatter)} – ${hasta.format(formatter)}"
}

private fun buildSaldoSeries(
    transacciones: List<com.gabriel.controlfinanciero.data.local.entities.TransaccionEntity>,
    desde: LocalDate,
    hasta: LocalDate
): List<Float> {
    val zoneId = ZoneId.systemDefault()
    val days = generateSequence(desde) { date ->
        date.plusDays(1).takeIf { it <= hasta }
    }.toList()

    if (days.isEmpty()) return listOf(0f)

    val transaccionesPorDia = transacciones.groupBy { transaccion ->
        Instant.ofEpochMilli(transaccion.fecha).atZone(zoneId).toLocalDate()
    }

    return days.map { dia ->
        val movimientos = transaccionesPorDia[dia].orEmpty()
        val ingresos = movimientos.filter { it.tipo == "INGRESO" }.sumOf { it.monto }
        val egresos = movimientos.filter { it.tipo == "EGRESO" }.sumOf { it.monto }
        (ingresos - egresos).toFloat()
    }
}

private fun buildChartLabels(desde: LocalDate, hasta: LocalDate): List<String> {
    val totalDays = java.time.temporal.ChronoUnit.DAYS.between(desde, hasta).toInt().coerceAtLeast(1)
    val step = (totalDays / 4).coerceAtLeast(1)
    val labels = mutableListOf<String>()
    val formatter = DateTimeFormatter.ofPattern("d")

    var current = desde
    while (current <= hasta && labels.size < 5) {
        labels.add(current.format(formatter))
        current = current.plusDays(step.toLong())
    }

    if (labels.isEmpty() || labels.last() != hasta.format(formatter)) {
        labels.add(hasta.format(formatter))
    }

    return labels.take(5)
}
