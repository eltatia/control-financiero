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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// =============================================================
//                 DONUT CHART PREMIUM (ANIMADO)
// =============================================================
@Composable
fun DonutChartPremium(
    values: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 45f,
    animate: Boolean = true
) {
    val total = values.sum()
    val animatedSweep = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (animate) {
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

    Canvas(modifier = modifier.size(250.dp)) {

        val diameter = size.minDimension
        val radius = diameter / 2
        val arcSize = Size(diameter, diameter)

        var startAngle = -90f

        values.forEachIndexed { index, value ->
            val sweep = (value / total) * 360f * animatedSweep.value

            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                size = arcSize,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            startAngle += sweep
        }

        drawCircle(
            color = Color.White,
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
//                 BAR CHART PREMIUM (DINÁMICO)
// =============================================================
data class MonthData(
    val month: String,
    val ingresos: List<Float>,
    val egresos: List<Float>
)

val monthsData = listOf(
    MonthData("Noviembre 2025", listOf(500f, 450f, 700f, 650f), listOf(300f, 350f, 400f, 500f)),
    MonthData("Octubre 2025", listOf(400f, 420f, 380f, 500f), listOf(350f, 300f, 280f, 450f)),
    MonthData("Septiembre 2025", listOf(600f, 530f, 480f, 620f), listOf(320f, 310f, 400f, 450f))
)

@Composable
fun BarChartPremium(
    ingresos: List<Float>,
    egresos: List<Float>,
    modifier: Modifier = Modifier,
    barColorIngreso: Color = Color(0xFF3CC57C),
    barColorEgreso: Color = Color(0xFFE74C3C)
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(ingresos, egresos) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val maxValue = (ingresos + egresos).maxOrNull() ?: 1f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {

        ingresos.indices.forEach { i ->

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // INGRESOS
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height((140.dp * (ingresos[i] / maxValue) * animationProgress.value))
                        .background(barColorIngreso, RoundedCornerShape(6.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                // EGRESOS
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height((140.dp * (egresos[i] / maxValue) * animationProgress.value))
                        .background(barColorEgreso, RoundedCornerShape(6.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text("Sem ${i + 1}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}



// =============================================================
//                  MODELO: ÚLTIMOS MOVIMIENTOS
// =============================================================
data class Movimiento(
    val titulo: String,
    val fecha: String,
    val monto: Float,
    val tipo: String // ingreso / egreso
)

val movimientosRecientes = listOf(
    Movimiento("Entrada de ENLA", "Hoy, 12:30 PM", 120.50f, "ingreso"),
    Movimiento("Compra comida", "Ayer, 8:15 PM", 25.80f, "egreso"),
    Movimiento("Pago Moto", "Ayer, 5:00 PM", 54f, "egreso"),
    Movimiento("Ingreso Sra. Kelly", "2 Nov, 10:00 AM", 300f, "ingreso")
)


@Composable
fun MovimientoItem(mov: Movimiento) {
    val colorMonto = if (mov.tipo == "ingreso") Color(0xFF3CC57C) else Color(0xFFE74C3C)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(45.dp)
                .background(
                    if (mov.tipo == "ingreso") Color(0xFFDFF6E6) else Color(0xFFFBE3E3),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (mov.tipo == "ingreso") "+" else "−",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorMonto
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(mov.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(mov.fecha, color = Color.Gray, fontSize = 13.sp)
        }

        Text(
            text = (if (mov.tipo == "ingreso") "+" else "−") + " S/ " + mov.monto.toString(),
            fontWeight = FontWeight.Bold,
            color = colorMonto,
            fontSize = 16.sp
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
fun RecordatorioItem(rec: Recordatorio) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA)),
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
                    .background(Color(0xFFCCE0FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("⏰", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(rec.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Vence: ${rec.fecha}", color = Color.Gray, fontSize = 13.sp)
            }

            Text(
                "S/ ${rec.monto}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3C67FF)
            )
        }
    }
}



// =============================================================
//                      HOME SCREEN COMPLETO
// =============================================================
@Composable
fun HomeScreen() {

    var currentMonthIndex by remember { mutableStateOf(0) }
    val currentMonth = monthsData[currentMonthIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())   // 👈 scroll vertical
            .padding(16.dp)
    ) {

        // =============================================================
        //                        ENCABEZADO
        // =============================================================
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(Color(0xFFCDE2FF), CircleShape)
            )

            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text("Hola,", color = Color.Gray, fontSize = 14.sp)
                Text("Alex", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = {}) {
                Icon(Icons.Default.Settings, contentDescription = "Config")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))



        // =============================================================
        //                    BALANCE GENERAL
        // =============================================================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Balance General", color = Color.Gray)
                Text(
                    "S/ 1,250.75",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))



        // =============================================================
        //                        BOTONES
        // =============================================================
        Row(Modifier.fillMaxWidth()) {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(Color(0xFF3CC57C)),
                modifier = Modifier.weight(1f)
            ) { Text("+ Añadir Ingreso") }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFECECEC),
                    contentColor = Color.Black
                ),
                modifier = Modifier.weight(1f)
            ) { Text("− Añadir Egreso") }
        }

        Spacer(modifier = Modifier.height(20.dp))



        // =============================================================
        //                      DONUT CHART
        // =============================================================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {

                Text(
                    "Distribución de Gastos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                DonutChartPremium(
                    values = listOf(350f, 280f, 150f, 60f),
                    colors = listOf(
                        Color(0xFFE74C3C),
                        Color(0xFF3498DB),
                        Color(0xFFF1C40F),
                        Color(0xFF9B59B6)
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Total Gastos: S/ 840.50",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))



        // =============================================================
        //             INGRESOS VS EGRESOS (SELECTOR DE MES)
        // =============================================================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentMonthIndex < monthsData.lastIndex)
                                currentMonthIndex++
                        }
                    ) {
                        Text("<", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        currentMonth.month,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    IconButton(
                        onClick = {
                            if (currentMonthIndex > 0)
                                currentMonthIndex--
                        }
                    ) {
                        Text(">", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                BarChartPremium(
                    ingresos = currentMonth.ingresos,
                    egresos = currentMonth.egresos,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))



        // =============================================================
        //                    ÚLTIMOS MOVIMIENTOS
        // =============================================================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {

                Text(
                    "Últimos Movimientos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                movimientosRecientes.forEach { mov ->
                    MovimientoItem(mov)
                    Divider(color = Color(0xFFECECEC), thickness = 1.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))



        // =============================================================
        //                        RECORDATORIOS
        // =============================================================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {

                Text(
                    "Recordatorios",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                listaRecordatorios.forEach { rec ->
                    RecordatorioItem(rec)
                }
            }
        }
    }
}
