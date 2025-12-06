package com.gabriel.controlfinanciero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabriel.controlfinanciero.viewmodel.FinanceViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
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
@Composable
fun CalendarioScreen(
    isDarkMode: Boolean,
    viewModel: FinanceViewModel = viewModel()
) {

    val bgColor = if (isDarkMode) CalendarBackground else Color(0xFFF3F6FF)
    val cardColor = if (isDarkMode) CardDark else Color.White
    val softCardColor = if (isDarkMode) CardDarkSoft else Color(0xFFE5E7EB)
    val textPrimary = if (isDarkMode) Color.White else Color.Black
    val textMutedColor = if (isDarkMode) TextMuted else Color.Gray

    var mode by remember { mutableStateOf(CalendarMode.MONTH) }
    var selectedFilter by remember { mutableStateOf(CalendarFilter.TODOS) }
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDay by remember { mutableStateOf(LocalDate.now().dayOfMonth) }

    val transaccionesMes by viewModel.transaccionesMes.collectAsState()
    val deudas by viewModel.deudas.collectAsState()

    LaunchedEffect(selectedMonth) {
        viewModel.cargarDatosMes(selectedMonth.atDay(1))
        selectedDay = selectedMonth.atDay(1).dayOfMonth
    }

    val eventsByDay = remember(transaccionesMes, deudas, selectedMonth) {
        val zoneId = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ofPattern("d 'de' MMM", Locale("es", "ES"))
        val events = mutableMapOf<Int, MutableList<CalendarEvent>>()

        transaccionesMes.forEach { transaccion ->
            val fecha = Instant.ofEpochMilli(transaccion.fecha).atZone(zoneId).toLocalDate()
            if (YearMonth.from(fecha) == selectedMonth) {
                val type = if (transaccion.tipo == "INGRESO") CalendarEventType.INGRESO else CalendarEventType.EGRESO
                events.getOrPut(fecha.dayOfMonth) { mutableListOf() }.add(
                    CalendarEvent(
                        title = transaccion.titulo,
                        subtitle = formatter.format(fecha),
                        amount = transaccion.monto,
                        type = type,
                        date = fecha
                    )
                )
            }
        }

        deudas.forEach { deuda ->
            val fecha = deuda.fechaVencimiento?.let {
                Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
            }
            if (fecha != null && YearMonth.from(fecha) == selectedMonth) {
                events.getOrPut(fecha.dayOfMonth) { mutableListOf() }.add(
                    CalendarEvent(
                        title = deuda.nombre,
                        subtitle = "Vence el ${formatter.format(fecha)}",
                        amount = deuda.montoPendiente,
                        type = CalendarEventType.RECORDATORIO,
                        date = fecha
                    )
                )
            }
        }

        events
    }

    val eventsForSelectedDay = remember(eventsByDay, selectedDay, selectedFilter) {
        val dayEvents = eventsByDay[selectedDay].orEmpty()
        dayEvents.filter { event ->
            when (selectedFilter) {
                CalendarFilter.TODOS -> true
                CalendarFilter.INGRESOS -> event.type == CalendarEventType.INGRESO
                CalendarFilter.EGRESOS -> event.type == CalendarEventType.EGRESO
                CalendarFilter.RECORDATORIOS -> event.type == CalendarEventType.RECORDATORIO
            }
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
                onModeChange = { mode = it },
                cardColor = cardColor,
                textPrimary = textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ================= CALENDARIO MENSUAL =================
            CalendarMonthView(
                month = selectedMonth,
                selectedDay = selectedDay,
                eventsByDay = eventsByDay,
                selectedFilter = selectedFilter,
                onMonthChange = { selectedMonth = it },
                onSelectDay = { selectedDay = it },
                textMutedColor = textMutedColor,
                textPrimary = textPrimary,
                accentGreen = AccentGreen,
                accentRed = AccentRed,
                accentYellow = AccentYellow
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ================= FILTROS =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipCalendar(
                    label = "Todos",
                    selected = selectedFilter == CalendarFilter.TODOS,
                    onClick = { selectedFilter = CalendarFilter.TODOS },
                    softCardColor = softCardColor
                )
                FilterChipCalendar(
                    label = "Ingresos",
                    selected = selectedFilter == CalendarFilter.INGRESOS,
                    onClick = { selectedFilter = CalendarFilter.INGRESOS },
                    softCardColor = softCardColor
                )
                FilterChipCalendar(
                    label = "Egresos",
                    selected = selectedFilter == CalendarFilter.EGRESOS,
                    onClick = { selectedFilter = CalendarFilter.EGRESOS },
                    softCardColor = softCardColor
                )
                FilterChipCalendar(
                    label = "Recordatorios",
                    selected = selectedFilter == CalendarFilter.RECORDATORIOS,
                    onClick = { selectedFilter = CalendarFilter.RECORDATORIOS },
                    softCardColor = softCardColor
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ================= EVENTOS DEL DÍA =================
            Text(
                text = "Eventos del ${selectedMonth.atDay(selectedDay).format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES")))}",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (eventsForSelectedDay.isEmpty()) {
                Text(
                    text = "No hay eventos programados en esta fecha.",
                    color = textMutedColor,
                    fontSize = 14.sp
                )
            } else {
                val lastIndex = eventsForSelectedDay.lastIndex
                eventsForSelectedDay.sortedBy { it.type.ordinal }.forEachIndexed { index, event ->
                    val icon: ImageVector
                    val circleColor: Color
                    val iconTint: Color
                    val amountColor: Color
                    val amountText: String

                    when (event.type) {
                        CalendarEventType.INGRESO -> {
                            icon = Icons.Default.ArrowUpward
                            circleColor = Color(0xFF064E3B)
                            iconTint = AccentGreen
                            amountColor = AccentGreen
                            amountText = "+ ${formatCurrency(event.amount)}"
                        }

                        CalendarEventType.EGRESO -> {
                            icon = Icons.Default.ArrowDownward
                            circleColor = Color(0xFF450A0A)
                            iconTint = AccentRed
                            amountColor = AccentRed
                            amountText = "- ${formatCurrency(event.amount)}"
                        }

                        CalendarEventType.RECORDATORIO -> {
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

            GoalCard(
                title = "Vacaciones de Verano",
                progress = 500f,
                goal = 2000f,
                cardColor = cardColor,
                softCardColor = softCardColor,
                textPrimary = textPrimary,
                textMutedColor = textMutedColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            GoalCard(
                title = "Nuevo Portátil",
                progress = 1200f,
                goal = 1500f,
                cardColor = cardColor,
                softCardColor = softCardColor,
                textPrimary = textPrimary,
                textMutedColor = textMutedColor
            )

            Spacer(modifier = Modifier.height(80.dp)) // espacio para el FAB
        }

        // ================= FAB (botón +) =================
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
                    contentDescription = "Añadir evento",
                    tint = Color.Black
                )
            }
        }
    }
}

// =============================================================
//                      COMPONENTES CALENDARIO
// =============================================================

private enum class CalendarMode { MONTH, WEEK }
private enum class CalendarFilter { TODOS, INGRESOS, EGRESOS, RECORDATORIOS }
private enum class CalendarEventType { INGRESO, EGRESO, RECORDATORIO }

private data class CalendarEvent(
    val title: String,
    val subtitle: String,
    val amount: Double?,
    val type: CalendarEventType,
    val date: LocalDate
)

@Composable
private fun CalendarModeToggle(
    currentMode: CalendarMode,
    onModeChange: (CalendarMode) -> Unit,
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
                selected = currentMode == CalendarMode.MONTH,
                onClick = { onModeChange(CalendarMode.MONTH) },
                modifier = Modifier.weight(1f),
                textPrimary = textPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            ModeChip(
                label = "Semana",
                selected = currentMode == CalendarMode.WEEK,
                onClick = { onModeChange(CalendarMode.WEEK) },
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
    selectedDay: Int,
    eventsByDay: Map<Int, List<CalendarEvent>>,
    selectedFilter: CalendarFilter,
    onMonthChange: (YearMonth) -> Unit,
    onSelectDay: (Int) -> Unit,
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
                        DayCell(
                            dayNumber = day,
                            selected = day == selectedDay,
                            events = day?.let { eventsByDay[it].orEmpty() }.orEmpty(),
                            selectedFilter = selectedFilter,
                            onClick = { day?.let { onSelectDay(it) } },
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
private fun DayCell(
    dayNumber: Int?,
    selected: Boolean,
    events: List<CalendarEvent>,
    selectedFilter: CalendarFilter,
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
            val filteredEvents = events.filter {
                when (selectedFilter) {
                    CalendarFilter.TODOS -> true
                    CalendarFilter.INGRESOS -> it.type == CalendarEventType.INGRESO
                    CalendarFilter.EGRESOS -> it.type == CalendarEventType.EGRESO
                    CalendarFilter.RECORDATORIOS -> it.type == CalendarEventType.RECORDATORIO
                }
            }

            val dotColor = when {
                filteredEvents.any { it.type == CalendarEventType.EGRESO } -> accentRed
                filteredEvents.any { it.type == CalendarEventType.INGRESO } -> accentGreen
                filteredEvents.any { it.type == CalendarEventType.RECORDATORIO } -> accentYellow
                else -> null
            }

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
                    .matchParentSize()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Transparent)
                        .padding(4.dp)
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    dayContent()
                }

                if (dotColor != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 2.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double?): String {
    return amount?.let { "S/ ${"%,.2f".format(it)}" } ?: "--"
}

// =============================================================
//                      EVENTOS Y METAS
// =============================================================

@Composable
private fun FilterChipCalendar(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    softCardColor: Color
) {
    val bg = if (selected) AccentGreen else softCardColor
    val textColor = if (selected) Color.Black else Color.White

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
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
