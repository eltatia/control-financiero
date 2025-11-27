package com.gabriel.controlfinanciero.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
fun CalendarioScreen(isDarkMode: Boolean) {

    val bgColor = if (isDarkMode) CalendarBackground else Color(0xFFF3F6FF)
    val cardColor = if (isDarkMode) CardDark else Color.White
    val softCardColor = if (isDarkMode) CardDarkSoft else Color(0xFFE5E7EB)
    val textPrimary = if (isDarkMode) Color.White else Color.Black
    val textMutedColor = if (isDarkMode) TextMuted else Color.Gray

    var mode by remember { mutableStateOf(CalendarMode.MONTH) }
    var selectedFilter by remember { mutableStateOf(CalendarFilter.TODOS) }
    val selectedDay = 16 // fijo para maqueta

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
                monthLabel = "Octubre 2024",
                selectedDay = selectedDay,
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

            // ================= EVENTOS DE HOY =================
            Text(
                text = "Eventos de Hoy",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            EventCard(
                title = "Salario - Empresa X",
                subtitle = "Pagado",
                amountText = "+ S/ 2,500.00",
                amountColor = AccentGreen,
                circleColor = Color(0xFF064E3B),
                icon = Icons.Default.ArrowUpward,
                iconTint = AccentGreen,
                cardColor = cardColor,
                textPrimary = textPrimary,
                textMutedColor = textMutedColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            EventCard(
                title = "Alquiler",
                subtitle = "Vence en 3 días",
                amountText = "- S/ 850.00",
                amountColor = AccentRed,
                circleColor = Color(0xFF450A0A),
                icon = Icons.Default.ArrowDownward,
                iconTint = AccentRed,
                cardColor = cardColor,
                textPrimary = textPrimary,
                textMutedColor = textMutedColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            EventCard(
                title = "Pago Tarjeta de Crédito",
                subtitle = "Vence Hoy",
                amountText = "- S/ 120.50",
                amountColor = AccentRed,
                circleColor = Color(0xFF78350F),
                icon = Icons.Default.Notifications,
                iconTint = AccentYellow,
                cardColor = cardColor,
                textPrimary = textPrimary,
                textMutedColor = textMutedColor
            )

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

// Calendario estático para la maqueta (Octubre 2024)
@Composable
private fun CalendarMonthView(
    monthLabel: String,
    selectedDay: Int,
    textMutedColor: Color,
    textPrimary: Color,
    accentGreen: Color,
    accentRed: Color,
    accentYellow: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        // Header mes + flechas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { }) {
                Text("<", color = textPrimary, fontSize = 18.sp)
            }

            Text(
                text = monthLabel,
                color = textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            TextButton(onClick = { }) {
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

        // Matriz de días
        val daysMatrix = listOf(
            listOf("", "", "1", "2", "3", "4", "5"),
            listOf("6", "7", "8", "9", "10", "11", "12"),
            listOf("13", "14", "15", "16", "17", "18", "19"),
            listOf("20", "21", "22", "23", "24", "25", "26"),
            listOf("27", "28", "29", "30", "31", "", "")
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            daysMatrix.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    week.forEach { day ->
                        DayCell(
                            dayText = day,
                            selected = day.toIntOrNull() == selectedDay,
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
    dayText: String,
    selected: Boolean,
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
        if (dayText.isNotEmpty()) {
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

            val dayInt = dayText.toInt()
            val dotColor = when (dayInt) {
                2, 5 -> accentRed
                12 -> accentYellow
                15, 30 -> accentGreen
                else -> null
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
