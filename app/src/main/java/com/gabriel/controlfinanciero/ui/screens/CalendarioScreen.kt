package com.gabriel.controlfinanciero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabriel.controlfinanciero.viewmodel.CalendarEvent
import com.gabriel.controlfinanciero.viewmodel.DayMarker
import com.gabriel.controlfinanciero.viewmodel.FinanceViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// Colores base
private val CalendarBackground = Color(0xFF021712)
private val CardDark = Color(0xFF071E1A)
private val CardDarkSoft = Color(0xFF0A2320)
private val AccentGreen = Color(0xFF22C55E)
private val AccentRed = Color(0xFFEF4444)
private val AccentYellow = Color(0xFFFBBF24)
private val TextMuted = Color(0xFF9CA3AF)

// =============================================================
//                      PANTALLA CALENDARIO
// =============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    isDarkMode: Boolean,
    viewModel: FinanceViewModel = viewModel(factory = FinanceViewModel.Factory),
    onNuevaTransaccion: () -> Unit = {}
) {

    val bgColor = if (isDarkMode) CalendarBackground else Color(0xFFF3F6FF)
    val cardColor = if (isDarkMode) CardDark else Color.White
    val softCardColor = if (isDarkMode) CardDarkSoft else Color(0xFFE5E7EB)
    val textPrimary = if (isDarkMode) Color.White else Color.Black
    val textMutedColor = if (isDarkMode) TextMuted else Color.Gray

    val selectedDate by viewModel.selectedDate.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val dayMarkers by viewModel.dayMarkers.collectAsState()
    val eventosDelDia by viewModel.eventosDelDia.collectAsState()
    val totalIngresosDia by viewModel.totalIngresosDia.collectAsState()
    val totalEgresosDia by viewModel.totalEgresosDia.collectAsState()
    val balanceDia by viewModel.balanceDia.collectAsState()

    val selectedMonth = remember(selectedDate) { YearMonth.from(selectedDate) }

    var showFabMenu by remember { mutableStateOf(false) }
    var showRecordatorioSheet by remember { mutableStateOf(false) }
    var showMetaSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var editingRecordatorio by remember { mutableStateOf<CalendarEvent.Rem?>(null) }
    var recordatorioTitulo by remember { mutableStateOf("") }
    var recordatorioTipo by remember { mutableStateOf("PAGO") }
    var recordatorioMonto by remember { mutableStateOf("") }
    var recordatorioNota by remember { mutableStateOf("") }
    var recordatorioFecha by remember { mutableStateOf(selectedDate) }

    var metas by remember { mutableStateOf(listOf<MetaItem>()) }
    var editingMeta by remember { mutableStateOf<MetaItem?>(null) }
    var metaTitulo by remember { mutableStateOf("") }
    var metaObjetivo by remember { mutableStateOf("") }

    LaunchedEffect(selectedDate, mode) {
        if (mode == "MES") {
            viewModel.setMes(selectedDate)
        }
    }

    LaunchedEffect(showRecordatorioSheet, selectedDate, editingRecordatorio) {
        if (showRecordatorioSheet) {
            recordatorioFecha = editingRecordatorio?.date ?: selectedDate
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

            // ================= HEADER =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Calendario",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Vista Calendario",
                        tint = textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= TOGGLE MES / SEMANA =================
            CalendarModeToggle(
                currentMode = mode,
                onModeChange = { newMode ->
                    if (newMode == "MES") {
                        viewModel.setMes(selectedDate)
                    } else {
                        viewModel.setSemana(selectedDate)
                    }
                },
                cardColor = cardColor,
                textPrimary = textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ================= CALENDARIO =================
            if (mode == "SEMANA") {
                CalendarWeekView(
                    selectedDate = selectedDate,
                    dayMarkers = dayMarkers,
                    selectedFilter = filter,
                    onWeekChange = { baseDate -> viewModel.setSemana(baseDate) },
                    onSelectDay = { viewModel.seleccionarDia(it) },
                    textMutedColor = textMutedColor,
                    textPrimary = textPrimary,
                    accentGreen = AccentGreen,
                    accentRed = AccentRed,
                    accentYellow = AccentYellow
                )
            } else {
                CalendarMonthView(
                    month = selectedMonth,
                    selectedDate = selectedDate,
                    dayMarkers = dayMarkers,
                    selectedFilter = filter,
                    onMonthChange = { newMonth -> viewModel.setMes(newMonth.atDay(1)) },
                    onSelectDay = { viewModel.seleccionarDia(it) },
                    textMutedColor = textMutedColor,
                    textPrimary = textPrimary,
                    accentGreen = AccentGreen,
                    accentRed = AccentRed,
                    accentYellow = AccentYellow
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= FILTROS =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipCalendar(
                    label = "Todos",
                    selected = filter == "TODOS",
                    onClick = { viewModel.setFiltro("TODOS") },
                    softCardColor = softCardColor,
                    textPrimary = textPrimary
                )
                FilterChipCalendar(
                    label = "Ingresos",
                    selected = filter == "INGRESOS",
                    onClick = { viewModel.setFiltro("INGRESOS") },
                    softCardColor = softCardColor,
                    textPrimary = textPrimary
                )
                FilterChipCalendar(
                    label = "Egresos",
                    selected = filter == "EGRESOS",
                    onClick = { viewModel.setFiltro("EGRESOS") },
                    softCardColor = softCardColor,
                    textPrimary = textPrimary
                )
                FilterChipCalendar(
                    label = "Recordatorios",
                    selected = filter == "RECORDATORIOS",
                    onClick = { viewModel.setFiltro("RECORDATORIOS") },
                    softCardColor = softCardColor,
                    textPrimary = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ================= EVENTOS DEL DÍA =================
            Text(
                text = "Eventos del ${selectedDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES")))}",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ingresos: ${formatCurrency(totalIngresosDia)}",
                    color = AccentGreen,
                    fontSize = 13.sp
                )
                Text(
                    text = "Egresos: ${formatCurrency(totalEgresosDia)}",
                    color = AccentRed,
                    fontSize = 13.sp
                )
                Text(
                    text = "Balance: ${formatCurrency(balanceDia)}",
                    color = if (balanceDia >= 0) AccentGreen else AccentRed,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (eventosDelDia.isEmpty()) {
                Text(
                    text = "No hay eventos programados en esta fecha.",
                    color = textMutedColor,
                    fontSize = 14.sp
                )
            } else {
                val lastIndex = eventosDelDia.lastIndex
                eventosDelDia.forEachIndexed { index, event ->
                    val eventCard: @Composable () -> Unit = {
                        val icon: ImageVector
                        val circleColor: Color
                        val iconTint: Color
                        val amountColor: Color
                        val amountText: String

                        when (event) {
                            is CalendarEvent.Tx -> {
                                if (event.tipo == "INGRESO") {
                                    icon = Icons.Default.ArrowUpward
                                    circleColor = Color(0xFF064E3B)
                                    iconTint = AccentGreen
                                    amountColor = AccentGreen
                                    amountText = "+ ${formatCurrency(event.amount)}"
                                } else {
                                    icon = Icons.Default.ArrowDownward
                                    circleColor = Color(0xFF450A0A)
                                    iconTint = AccentRed
                                    amountColor = AccentRed
                                    amountText = "- ${formatCurrency(event.amount)}"
                                }
                            }

                            is CalendarEvent.Rem -> {
                                icon = Icons.Default.Notifications
                                circleColor = Color(0xFF78350F)
                                iconTint = AccentYellow
                                amountColor = AccentYellow
                                amountText = formatCurrency(event.amount)
                            }

                            is CalendarEvent.VenceDeuda -> {
                                icon = Icons.Default.Notifications
                                circleColor = Color(0xFF78350F)
                                iconTint = AccentYellow
                                amountColor = AccentYellow
                                amountText = formatCurrency(event.amount)
                            }
                        }

                        EventCard(
                            title = event.title,
                            subtitle = event.subtitle,
                            amountText = amountText,
                            amountColor = amountColor,
                            circleColor = circleColor,
                            icon = icon,
                            iconTint = iconTint,
                            cardColor = cardColor,
                            textPrimary = textPrimary,
                            textMutedColor = textMutedColor
                        )
                    }

                    if (event is CalendarEvent.Rem) {
                        SwipeActionRow(
                            onEdit = {
                                editingRecordatorio = event
                                recordatorioTitulo = event.title
                                recordatorioTipo = event.tipo
                                recordatorioMonto = event.amount?.toString().orEmpty()
                                recordatorioNota = event.nota.orEmpty()
                                showRecordatorioSheet = true
                            },
                            onDelete = { viewModel.eliminarRecordatorio(event.id) },
                            backgroundColor = cardColor,
                            content = eventCard
                        )
                    } else {
                        eventCard()
                    }

                    if (index != lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ================= MIS METAS =================
            Text(
                text = "Mis Metas",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (metas.isEmpty()) {
                Text(
                    text = "Aún no tienes metas registradas.",
                    color = textMutedColor,
                    fontSize = 14.sp
                )
            } else {
                metas.forEachIndexed { index, meta ->
                    SwipeActionRow(
                        onEdit = {
                            editingMeta = meta
                            metaTitulo = meta.title
                            metaObjetivo = meta.goal.toString()
                            showMetaSheet = true
                        },
                        onDelete = {
                            metas = metas.filterNot { it.id == meta.id }
                        },
                        backgroundColor = cardColor
                    ) {
                        GoalCard(
                            title = meta.title,
                            progress = meta.progress,
                            goal = meta.goal,
                            cardColor = cardColor,
                            softCardColor = softCardColor,
                            textPrimary = textPrimary,
                            textMutedColor = textMutedColor
                        )
                    }

                    if (index != metas.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showFabMenu = true },
            containerColor = AccentGreen,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar"
            )
        }

        DropdownMenu(
            expanded = showFabMenu,
            onDismissRequest = { showFabMenu = false },
            modifier = Modifier.align(Alignment.BottomEnd),
            offset = DpOffset(0.dp, (-56).dp)
        ) {
            DropdownMenuItem(
                text = { Text("Nuevo evento") },
                onClick = {
                    showFabMenu = false
                    editingRecordatorio = null
                    recordatorioTitulo = ""
                    recordatorioMonto = ""
                    recordatorioNota = ""
                    recordatorioTipo = "PAGO"
                    showRecordatorioSheet = true
                }
            )
            DropdownMenuItem(
                text = { Text("Nueva meta") },
                onClick = {
                    showFabMenu = false
                    editingMeta = null
                    metaTitulo = ""
                    metaObjetivo = ""
                    showMetaSheet = true
                }
            )
        }
    }

    if (showRecordatorioSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showRecordatorioSheet = false
                editingRecordatorio = null
            },
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
                    text = if (editingRecordatorio == null) "Nuevo evento" else "Editar evento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                OutlinedTextField(
                    value = recordatorioTitulo,
                    onValueChange = { recordatorioTitulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TipoChip(
                        label = "PAGO",
                        selected = recordatorioTipo == "PAGO",
                        onClick = { recordatorioTipo = "PAGO" },
                        softCardColor = softCardColor,
                        textPrimary = textPrimary
                    )
                    TipoChip(
                        label = "COBRO",
                        selected = recordatorioTipo == "COBRO",
                        onClick = { recordatorioTipo = "COBRO" },
                        softCardColor = softCardColor,
                        textPrimary = textPrimary
                    )
                    TipoChip(
                        label = "NOTA",
                        selected = recordatorioTipo == "NOTA",
                        onClick = { recordatorioTipo = "NOTA" },
                        softCardColor = softCardColor,
                        textPrimary = textPrimary
                    )
                }

                OutlinedTextField(
                    value = recordatorioMonto,
                    onValueChange = { recordatorioMonto = it },
                    label = { Text("Monto (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = recordatorioNota,
                    onValueChange = { recordatorioNota = it },
                    label = { Text("Nota (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { recordatorioFecha = recordatorioFecha.minusDays(1) }) {
                        Text(text = "◀ Día anterior", color = AccentGreen)
                    }
                    Text(
                        text = recordatorioFecha.format(DateTimeFormatter.ofPattern("d 'de' MMM", Locale("es", "ES"))),
                        color = textMutedColor,
                        fontSize = 12.sp
                    )
                    TextButton(onClick = { recordatorioFecha = recordatorioFecha.plusDays(1) }) {
                        Text(text = "Día siguiente ▶", color = AccentGreen)
                    }
                }

                TextButton(
                    onClick = {
                        onNuevaTransaccion()
                        showRecordatorioSheet = false
                        editingRecordatorio = null
                    }
                ) {
                    Text(text = "Nueva Transacción", color = AccentGreen)
                }

                TextButton(
                    onClick = {
                        val monto = recordatorioMonto.toDoubleOrNull()
                        val nota = recordatorioNota.takeIf { it.isNotBlank() }
                        val editing = editingRecordatorio
                        if (editing == null) {
                            viewModel.crearRecordatorio(
                                titulo = recordatorioTitulo,
                                tipo = recordatorioTipo,
                                monto = monto,
                                nota = nota,
                                fechaSeleccionada = recordatorioFecha
                            )
                        } else {
                            viewModel.actualizarRecordatorio(
                                id = editing.id,
                                titulo = recordatorioTitulo,
                                tipo = recordatorioTipo,
                                monto = monto,
                                nota = nota,
                                fechaSeleccionada = recordatorioFecha
                            )
                        }
                        recordatorioTitulo = ""
                        recordatorioMonto = ""
                        recordatorioNota = ""
                        recordatorioTipo = "PAGO"
                        showRecordatorioSheet = false
                        editingRecordatorio = null
                    },
                    enabled = recordatorioTitulo.isNotBlank()
                ) {
                    Text(text = "Guardar", color = AccentGreen)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showMetaSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showMetaSheet = false
                editingMeta = null
            },
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
                    text = if (editingMeta == null) "Nueva meta" else "Editar meta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                OutlinedTextField(
                    value = metaTitulo,
                    onValueChange = { metaTitulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = metaObjetivo,
                    onValueChange = { metaObjetivo = it },
                    label = { Text("Meta (monto)") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = {
                        val objetivo = metaObjetivo.toFloatOrNull() ?: 0f
                        val existing = editingMeta
                        if (existing == null) {
                            val newMeta = MetaItem(
                                id = (metas.maxOfOrNull { it.id } ?: 0) + 1,
                                title = metaTitulo,
                                goal = objetivo,
                                progress = 0f
                            )
                            metas = metas + newMeta
                        } else {
                            metas = metas.map { meta ->
                                if (meta.id == existing.id) {
                                    meta.copy(title = metaTitulo, goal = objetivo)
                                } else {
                                    meta
                                }
                            }
                        }
                        metaTitulo = ""
                        metaObjetivo = ""
                        showMetaSheet = false
                        editingMeta = null
                    },
                    enabled = metaTitulo.isNotBlank()
                ) {
                    Text(text = "Guardar", color = AccentGreen)
                }
            }
        }
    }
}

@Composable
private fun CalendarModeToggle(
    currentMode: String,
    onModeChange: (String) -> Unit,
    cardColor: Color,
    textPrimary: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardColor)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ModeChip(
                label = "Mes",
                selected = currentMode == "MES",
                onClick = { onModeChange("MES") },
                modifier = Modifier.weight(1f),
                textPrimary = textPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            ModeChip(
                label = "Semana",
                selected = currentMode == "SEMANA",
                onClick = { onModeChange("SEMANA") },
                modifier = Modifier.weight(1f),
                textPrimary = textPrimary
            )
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textPrimary: Color
) {
    val bg = if (selected) Color(0xFF111827) else Color.Transparent
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = onClick) {
            Text(
                text = label,
                color = textPrimary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun CalendarMonthView(
    month: YearMonth,
    selectedDate: LocalDate,
    dayMarkers: Map<LocalDate, DayMarker>,
    selectedFilter: String,
    onMonthChange: (YearMonth) -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    textMutedColor: Color,
    textPrimary: Color,
    accentGreen: Color,
    accentRed: Color,
    accentYellow: Color
) {
    val monthLabel = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES")))
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    val daysInMonth = month.lengthOfMonth()
    val firstDayIndex = month.atDay(1).dayOfWeek.value % 7 // Domingo = 0
    val totalCells = firstDayIndex + daysInMonth
    val weeks = (0 until totalCells).map { index ->
        val dayNumber = index - firstDayIndex + 1
        dayNumber.takeIf { it in 1..daysInMonth }
    }.chunked(7)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        // Header mes + flechas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onMonthChange(month.minusMonths(1)) }) {
                Text("<", color = textPrimary, fontSize = 18.sp)
            }

            Text(
                text = monthLabel,
                color = textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            TextButton(onClick = { onMonthChange(month.plusMonths(1)) }) {
                Text(">", color = textPrimary, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Días de la semana
        val weekDays = listOf("D", "L", "M", "M", "J", "V", "S")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach {
                Text(
                    text = it,
                    color = textMutedColor,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    week.forEach { day ->
                        val date = day?.let { month.atDay(it) }
                        DayCell(
                            dayNumber = day,
                            selected = date == selectedDate,
                            marker = date?.let { dayMarkers[it] },
                            selectedFilter = selectedFilter,
                            onClick = { date?.let { onSelectDay(it) } },
                            modifier = Modifier.weight(1f),
                            textPrimary = textPrimary,
                            accentGreen = accentGreen,
                            accentRed = accentRed,
                            accentYellow = accentYellow
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarWeekView(
    selectedDate: LocalDate,
    dayMarkers: Map<LocalDate, DayMarker>,
    selectedFilter: String,
    onWeekChange: (LocalDate) -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    textMutedColor: Color,
    textPrimary: Color,
    accentGreen: Color,
    accentRed: Color,
    accentYellow: Color
) {
    val startOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value % 7).toLong())
    val endOfWeek = startOfWeek.plusDays(6)
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))
    val weekLabel = "${formatter.format(startOfWeek)} - ${formatter.format(endOfWeek)}"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onWeekChange(startOfWeek.minusDays(7)) }) {
                Text("<", color = textPrimary, fontSize = 18.sp)
            }

            Text(
                text = weekLabel,
                color = textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            TextButton(onClick = { onWeekChange(startOfWeek.plusDays(7)) }) {
                Text(">", color = textPrimary, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val weekDays = listOf("D", "L", "M", "M", "J", "V", "S")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach {
                Text(
                    text = it,
                    color = textMutedColor,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (0..6).forEach { offset ->
                val date = startOfWeek.plusDays(offset.toLong())
                DayCell(
                    dayNumber = date.dayOfMonth,
                    selected = date == selectedDate,
                    marker = dayMarkers[date],
                    selectedFilter = selectedFilter,
                    onClick = { onSelectDay(date) },
                    modifier = Modifier.weight(1f),
                    textPrimary = textPrimary,
                    accentGreen = accentGreen,
                    accentRed = accentRed,
                    accentYellow = accentYellow
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    dayNumber: Int?,
    selected: Boolean,
    marker: DayMarker?,
    selectedFilter: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textPrimary: Color,
    accentGreen: Color,
    accentRed: Color,
    accentYellow: Color
) {
    Box(
        modifier = modifier
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        if (dayNumber != null) {
            val showIngreso = marker?.ingreso == true && (selectedFilter == "TODOS" || selectedFilter == "INGRESOS")
            val showEgreso = marker?.egreso == true && (selectedFilter == "TODOS" || selectedFilter == "EGRESOS")
            val showRecordatorio = marker?.recordatorio == true && (selectedFilter == "TODOS" || selectedFilter == "RECORDATORIOS")

            val dayText = dayNumber.toString()
            val dayContent: @Composable () -> Unit = {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accentGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayText,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = dayText,
                        color = textPrimary,
                        fontSize = 14.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Transparent)
                        .padding(4.dp)
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    dayContent()
                }

                if (showIngreso || showEgreso || showRecordatorio) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (showIngreso) {
                            MarkerDot(color = accentGreen)
                        }
                        if (showEgreso) {
                            MarkerDot(color = accentRed)
                        }
                        if (showRecordatorio) {
                            MarkerDot(color = accentYellow)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkerDot(color: Color) {
    Box(
        modifier = Modifier
            .offset(y = 2.dp)
            .size(6.dp)
            .clip(CircleShape)
            .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeActionRow(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    backgroundColor: Color,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { false }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Text(text = "Editar", color = AccentGreen)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDelete) {
                    Text(text = "Eliminar", color = AccentRed)
                }
            }
        }
    ) {
        content()
    }
}

private fun formatCurrency(amount: Double?): String {
    return amount?.let { "S/ ${"%,.2f".format(it)}" } ?: "--"
}

private data class MetaItem(
    val id: Int,
    val title: String,
    val goal: Float,
    val progress: Float
)

// =============================================================
//                      EVENTOS Y METAS
// =============================================================

@Composable
private fun FilterChipCalendar(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    softCardColor: Color,
    textPrimary: Color
) {
    val bg = if (selected) AccentGreen else softCardColor
    val textColor = if (selected) Color.Black else textPrimary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TipoChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    softCardColor: Color,
    textPrimary: Color
) {
    val bg = if (selected) AccentGreen else softCardColor
    val textColor = if (selected) Color.Black else textPrimary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EventCard(
    title: String,
    subtitle: String,
    amountText: String,
    amountColor: Color,
    circleColor: Color,
    icon: ImageVector,
    iconTint: Color,
    cardColor: Color,
    textPrimary: Color,
    textMutedColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = subtitle,
                    color = if (subtitle.contains("Vence")) AccentRed else textMutedColor,
                    fontSize = 13.sp
                )
            }

            Text(
                text = amountText,
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun GoalCard(
    title: String,
    progress: Float,
    goal: Float,
    cardColor: Color,
    softCardColor: Color,
    textPrimary: Color,
    textMutedColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = "S/ ${"%,.0f".format(progress)} / S/ ${"%,.0f".format(goal)}",
                    color = textMutedColor,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(softCardColor)
            ) {
                val fraction = (progress / goal).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(AccentGreen)
                )
            }
        }
    }
}
