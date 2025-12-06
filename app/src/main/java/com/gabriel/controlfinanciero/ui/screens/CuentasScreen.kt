package com.gabriel.controlfinanciero.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabriel.controlfinanciero.data.local.entities.CuentaEntity
import com.gabriel.controlfinanciero.data.local.entities.DeudaEntity
import com.gabriel.controlfinanciero.data.local.entities.TransaccionEntity
import com.gabriel.controlfinanciero.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
fun CuentasScreen(
    isDarkMode: Boolean,
    viewModel: FinanceViewModel = viewModel()
) {
    val cuentas by viewModel.cuentas.collectAsState()
    val deudas by viewModel.deudas.collectAsState()

    // Totales anuales (ingresos y egresos)
    val totalIngresosAnual by viewModel.totalIngresosAnual.collectAsState()
    val totalEgresosAnual by viewModel.totalEgresosAnual.collectAsState()

    // Todas las transacciones (para calcular saldo actual por cuenta)
    val todasTransacciones by viewModel.todasTransacciones.collectAsState()

    // Cargar datos del año actual cuando se entra a la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarDatosAnuales()
    }

    val bgColor = if (isDarkMode) AccountsBackground else Color(0xFFF3F6FF)
    val cardColor = if (isDarkMode) CardDark else Color.White
    val softCardColor = if (isDarkMode) CardDarkSoft else Color(0xFFE5E7EB)
    val textPrimary = if (isDarkMode) Color.White else Color.Black
    val textMutedColor = if (isDarkMode) TextMuted else Color.Gray

    // ================= ESTADOS FORMULARIO CUENTAS =================
    var showNuevaCuentaDialog by remember { mutableStateOf(false) }
    var nuevaCuentaNombre by remember { mutableStateOf("") }
    var nuevaCuentaSaldoText by remember { mutableStateOf("") }

    // chip seleccionado: EFECTIVO / BANCO / BILLETERA / OTROS
    var tipoCuentaSeleccionado by remember { mutableStateOf("EFECTIVO") }
    var tipoCuentaPersonalizado by remember { mutableStateOf("") }

    // modo edición: si es null, estamos creando; si tiene valor, editamos
    var indiceEdicion by remember { mutableStateOf<Int?>(null) }
    var cuentaEnEdicion by remember { mutableStateOf<CuentaEntity?>(null) }

    // ================= ESTADOS FORMULARIO DEUDAS =================
    var showNuevaDeudaDialog by remember { mutableStateOf(false) }
    var nuevaDeudaNombre by remember { mutableStateOf("") }
    var nuevaDeudaMontoText by remember { mutableStateOf("") }
    var tipoDeudaSeleccionado by remember { mutableStateOf("DEUDA") } // DEUDA o PRESTAMO

    // Abono de deudas
    var showAbonoDeudaDialog by remember { mutableStateOf(false) }
    var deudaSeleccionadaAbono by remember { mutableStateOf<DeudaEntity?>(null) }
    var montoAbonoText by remember { mutableStateOf("") }

    // ================= ESTADO MENÚ DEL FAB =================
    var fabMenuExpanded by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    // Confirmaciones de borrado
    var cuentaParaEliminar by remember { mutableStateOf<CuentaEntity?>(null) }
    var deudaParaEliminar by remember { mutableStateOf<DeudaEntity?>(null) }

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

                Box {
                    IconButton(onClick = { showSettingsMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = textPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showSettingsMenu,
                        onDismissRequest = { showSettingsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cargar datos demo") },
                            onClick = {
                                showSettingsMenu = false
                                viewModel.cargarDatosDemo()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ================= RESUMEN ANUAL =================
            Text(
                text = "Resumen anual",
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
                    titulo = "Ingresos del año",
                    monto = totalIngresosAnual,
                    colorMonto = AccentGreen,
                    modifier = Modifier.weight(1f),
                    cardColor = cardColor,
                    textMutedColor = textMutedColor
                )
                PatrimonioCard(
                    titulo = "Egresos del año",
                    monto = totalEgresosAnual,
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

            if (cuentas.isEmpty()) {
                Text(
                    text = "Aún no tienes cuentas registradas.\nToca el botón + para crear una.",
                    color = textMutedColor,
                    fontSize = 14.sp
                )
            } else {
                cuentas.forEachIndexed { index, cuenta ->

                    // 🔹 Saldo actual = saldoInicial + ingresos - egresos de esa cuenta
                    val saldoActual = calcularSaldoActual(cuenta, todasTransacciones)

                    SwipeableAccountItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        iconBgColor = Color(0xFF16A34A),
                        titulo = cuenta.nombre,
                        subtitulo = cuenta.tipo,
                        monto = saldoActual,
                        cardColor = cardColor,
                        textPrimary = textPrimary,
                        textMutedColor = textMutedColor,
                        onEdit = {
                            // Preparamos el formulario en modo edición
                            indiceEdicion = index
                            cuentaEnEdicion = cuenta
                            nuevaCuentaNombre = cuenta.nombre
                            nuevaCuentaSaldoText = cuenta.saldoInicial.toString()

                            if (cuenta.tipo in listOf("EFECTIVO", "BANCO", "BILLETERA")) {
                                tipoCuentaSeleccionado = cuenta.tipo
                                tipoCuentaPersonalizado = ""
                            } else {
                                tipoCuentaSeleccionado = "OTROS"
                                tipoCuentaPersonalizado = cuenta.tipo
                            }

                            showNuevaCuentaDialog = true
                        },
                        onDelete = {
                            cuentaParaEliminar = cuenta
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ================= DEUDAS Y PRÉSTAMOS =================
            Text(
                text = "Deudas y Préstamos",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (deudas.isEmpty()) {
                Text(
                    text = "Aún no tienes deudas registradas.\nUsa el botón + para agregar una.",
                    color = textMutedColor,
                    fontSize = 14.sp
                )
            } else {
                deudas.forEach { deuda ->
                    DeudaItem(
                        deuda = deuda,
                        cardColor = cardColor,
                        textPrimary = textPrimary,
                        textMutedColor = textMutedColor,
                        onAbonar = {
                            deudaSeleccionadaAbono = deuda
                            montoAbonoText = deuda.montoPendiente.toString()
                            showAbonoDeudaDialog = true
                        },
                        onEliminar = {
                            deudaParaEliminar = deuda
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // espacio para el FAB
        }

        // ================= FAB (+) CON MENÚ =================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AccentGreen)
                        .clickable {
                            fabMenuExpanded = !fabMenuExpanded
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir cuenta o deuda",
                        tint = Color.Black
                    )
                }

                DropdownMenu(
                    expanded = fabMenuExpanded,
                    onDismissRequest = { fabMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Nueva cuenta") },
                        onClick = {
                            fabMenuExpanded = false
                            // Modo creación cuenta
                            indiceEdicion = null
                            cuentaEnEdicion = null
                            nuevaCuentaNombre = ""
                            nuevaCuentaSaldoText = ""
                            tipoCuentaSeleccionado = "EFECTIVO"
                            tipoCuentaPersonalizado = ""
                            showNuevaCuentaDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Nueva deuda / préstamo") },
                        onClick = {
                            fabMenuExpanded = false
                            // Modo creación deuda
                            nuevaDeudaNombre = ""
                            nuevaDeudaMontoText = ""
                            tipoDeudaSeleccionado = "DEUDA"
                            showNuevaDeudaDialog = true
                        }
                    )
                }
            }
        }
    }

    // ================= CONFIRMAR ELIMINACIÓN DE CUENTA =================
    if (cuentaParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { cuentaParaEliminar = null },
            title = { Text("Eliminar cuenta") },
            text = {
                Text(
                    "Esta acción borrará la cuenta y sus movimientos asociados. ¿Deseas continuar?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        cuentaParaEliminar?.let { viewModel.eliminarCuenta(it) }
                        cuentaParaEliminar = null
                    }
                ) {
                    Text("Eliminar", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { cuentaParaEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ================= CONFIRMAR ELIMINACIÓN DE DEUDA =================
    if (deudaParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { deudaParaEliminar = null },
            title = { Text("Eliminar deuda / préstamo") },
            text = {
                Text("¿Seguro que deseas eliminar este registro? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deudaParaEliminar?.let { viewModel.eliminarDeuda(it) }
                        deudaParaEliminar = null
                    }
                ) {
                    Text("Eliminar", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { deudaParaEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ================= DIÁLOGO NUEVA / EDITAR CUENTA =================
    if (showNuevaCuentaDialog) {
        AlertDialog(
            onDismissRequest = {
                showNuevaCuentaDialog = false
                indiceEdicion = null
                cuentaEnEdicion = null
            },
            title = {
                Text(if (indiceEdicion == null) "Nueva cuenta" else "Editar cuenta")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nuevaCuentaNombre,
                        onValueChange = { nuevaCuentaNombre = it },
                        label = { Text("Nombre de la cuenta") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = nuevaCuentaSaldoText,
                        onValueChange = { nuevaCuentaSaldoText = it },
                        label = { Text("Saldo (S/)") },
                        singleLine = true
                    )

                    Text(
                        text = "Tipo de cuenta",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TipoChip(
                            label = "EFECTIVO",
                            selected = tipoCuentaSeleccionado == "EFECTIVO"
                        ) {
                            tipoCuentaSeleccionado = "EFECTIVO"
                            tipoCuentaPersonalizado = ""
                        }

                        TipoChip(
                            label = "BANCO",
                            selected = tipoCuentaSeleccionado == "BANCO"
                        ) {
                            tipoCuentaSeleccionado = "BANCO"
                            tipoCuentaPersonalizado = ""
                        }

                        TipoChip(
                            label = "BILLETERA",
                            selected = tipoCuentaSeleccionado == "BILLETERA"
                        ) {
                            tipoCuentaSeleccionado = "BILLETERA"
                            tipoCuentaPersonalizado = ""
                        }

                        TipoChip(
                            label = "OTROS",
                            selected = tipoCuentaSeleccionado == "OTROS"
                        ) {
                            tipoCuentaSeleccionado = "OTROS"
                        }
                    }

                    if (tipoCuentaSeleccionado == "OTROS") {
                        OutlinedTextField(
                            value = tipoCuentaPersonalizado,
                            onValueChange = { tipoCuentaPersonalizado = it },
                            label = { Text("Especifica el tipo de cuenta") },
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val saldo = nuevaCuentaSaldoText.replace(",", ".")
                            .toDoubleOrNull() ?: 0.0
                        val nombreFinal =
                            if (nuevaCuentaNombre.isBlank())
                                "Cuenta ${cuentas.size + 1}"
                            else
                                nuevaCuentaNombre.trim()

                        val tipoFinal = if (tipoCuentaSeleccionado == "OTROS") {
                            tipoCuentaPersonalizado.trim().ifBlank { "OTROS" }
                        } else {
                            tipoCuentaSeleccionado
                        }

                        if (indiceEdicion == null) {
                            // Crear nueva cuenta
                            viewModel.crearCuenta(
                                nombre = nombreFinal,
                                tipo = tipoFinal,
                                saldoInicial = saldo
                            )
                        } else {
                            // Actualizar cuenta existente
                            cuentaEnEdicion?.let { original ->
                                val actualizada = original.copy(
                                    nombre = nombreFinal,
                                    tipo = tipoFinal,
                                    saldoInicial = saldo
                                )
                                viewModel.actualizarCuenta(actualizada)
                            }
                        }

                        // Limpiamos estado
                        indiceEdicion = null
                        cuentaEnEdicion = null
                        nuevaCuentaNombre = ""
                        nuevaCuentaSaldoText = ""
                        tipoCuentaSeleccionado = "EFECTIVO"
                        tipoCuentaPersonalizado = ""
                        showNuevaCuentaDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNuevaCuentaDialog = false
                    indiceEdicion = null
                    cuentaEnEdicion = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ================= DIÁLOGO NUEVA DEUDA / PRÉSTAMO =================
    if (showNuevaDeudaDialog) {
        AlertDialog(
            onDismissRequest = {
                showNuevaDeudaDialog = false
            },
            title = { Text("Nueva deuda / préstamo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nuevaDeudaNombre,
                        onValueChange = { nuevaDeudaNombre = it },
                        label = { Text("Nombre (ej. Préstamo moto)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = nuevaDeudaMontoText,
                        onValueChange = { nuevaDeudaMontoText = it },
                        label = { Text("Monto total (S/)") },
                        singleLine = true
                    )

                    Text(
                        text = "Tipo",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TipoChip(
                            label = "DEUDA",
                            selected = tipoDeudaSeleccionado == "DEUDA"
                        ) {
                            tipoDeudaSeleccionado = "DEUDA"
                        }
                        TipoChip(
                            label = "PRESTAMO",
                            selected = tipoDeudaSeleccionado == "PRESTAMO"
                        ) {
                            tipoDeudaSeleccionado = "PRESTAMO"
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val monto = nuevaDeudaMontoText.replace(",", ".")
                            .toDoubleOrNull() ?: 0.0
                        val nombreFinal = nuevaDeudaNombre.trim()

                        if (nombreFinal.isNotBlank() && monto > 0) {
                            viewModel.crearDeuda(
                                nombre = nombreFinal,
                                montoTotal = monto,
                                tipo = tipoDeudaSeleccionado
                            )
                            // limpiar
                            nuevaDeudaNombre = ""
                            nuevaDeudaMontoText = ""
                            tipoDeudaSeleccionado = "DEUDA"
                            showNuevaDeudaDialog = false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNuevaDeudaDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ================= DIÁLOGO ABONAR DEUDA =================
    if (showAbonoDeudaDialog && deudaSeleccionadaAbono != null) {
        val deuda = deudaSeleccionadaAbono!!
        AlertDialog(
            onDismissRequest = {
                showAbonoDeudaDialog = false
                deudaSeleccionadaAbono = null
                montoAbonoText = ""
            },
            title = { Text("Abonar a ${deuda.nombre}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Pendiente actual: S/ ${"%,.2f".format(deuda.montoPendiente)}",
                        color = textMutedColor
                    )
                    OutlinedTextField(
                        value = montoAbonoText,
                        onValueChange = { montoAbonoText = it },
                        label = { Text("Monto del abono (S/)") },
                        singleLine = true
                    )
                    Text(
                        text = "Si el abono es igual o mayor al pendiente,\nla deuda se marcará como PAGADA.",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val monto = montoAbonoText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        if (monto > 0.0) {
                            viewModel.abonarDeuda(deuda, monto)
                            showAbonoDeudaDialog = false
                            deudaSeleccionadaAbono = null
                            montoAbonoText = ""
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAbonoDeudaDialog = false
                        deudaSeleccionadaAbono = null
                        montoAbonoText = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// =============================================================
//  CHIP SIMPLE PARA ELEGIR TIPO DE CUENTA / DEUDA
// =============================================================
@Composable
private fun TipoChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) AccentGreen.copy(alpha = 0.2f) else Color.Transparent,
        border = if (selected)
            ButtonDefaults.outlinedButtonBorder
        else
            null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.Black else TextMuted
        )
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
    textMutedColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
private fun DeudaItem(
    deuda: DeudaEntity,
    cardColor: Color,
    textPrimary: Color,
    textMutedColor: Color,
    onAbonar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
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
                    text = deuda.nombre,
                    color = textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "S/ ${"%,.2f".format(deuda.montoPendiente)}",
                    color = if (deuda.estado == "PAGADA") AccentGreen else AccentRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tipo: " + if (deuda.tipo == "DEUDA") "Deuda" else "Préstamo",
                color = textMutedColor,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado: ${deuda.estado}",
                    color = textMutedColor,
                    fontSize = 13.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onAbonar,
                        enabled = deuda.estado == "ACTIVA"
                    ) {
                        Text("Abonar")
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar deuda",
                            tint = AccentRed
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
//     ITEM SWIPEABLE PERSONALIZADO (EDITAR / ELIMINAR)
// =============================================================
@Composable
private fun SwipeableAccountItem(
    icon: ImageVector,
    iconBgColor: Color,
    titulo: String,
    subtitulo: String,
    monto: Double,
    cardColor: Color,
    textPrimary: Color,
    textMutedColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Ancho del área de acciones (edit/eliminar) que se va a revelar
    val actionWidth = 140.dp
    val maxOffsetPx = with(density) { -actionWidth.toPx() } // negativo (hacia la izquierda)

    val offsetX = remember { Animatable(0f) } // 0 = cerrado, valor negativo = abierto

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()

                        // Nuevo valor limitado entre 0 y maxOffsetPx (negativo)
                        val newOffset = (offsetX.value + dragAmount)
                            .coerceIn(maxOffsetPx, 0f)

                        scope.launch {
                            offsetX.snapTo(newOffset)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            // Si arrastraste más de la mitad, se queda abierto
                            if (offsetX.value < maxOffsetPx / 2f) {
                                offsetX.animateTo(maxOffsetPx)
                            } else {
                                offsetX.animateTo(0f)
                            }
                        }
                    }
                )
            }
    ) {
        // FONDO: botones de editar y eliminar (siempre ocupan el ancho de actionWidth)
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar cuenta",
                    tint = Color(0xFF22C55E)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar cuenta",
                    tint = Color(0xFFEF4444)
                )
            }
        }

        // CONTENIDO: la tarjeta de la cuenta, se desplaza a la izquierda según offsetX
        AccountItemCard(
            icon = icon,
            iconBgColor = iconBgColor,
            titulo = titulo,
            subtitulo = subtitulo,
            monto = monto,
            cardColor = cardColor,
            textPrimary = textPrimary,
            textMutedColor = textMutedColor,
            modifier = Modifier.offset {
                IntOffset(offsetX.value.roundToInt(), 0)
            }
        )
    }
}

// =============================================================
//      HELPER: CALCULAR SALDO ACTUAL POR CUENTA
// =============================================================
private fun calcularSaldoActual(
    cuenta: CuentaEntity,
    transacciones: List<TransaccionEntity>
): Double {
    val movimiento = transacciones
        .filter { it.cuentaId == cuenta.id }
        .sumOf { trans ->
            if (trans.tipo == "INGRESO") trans.monto else -trans.monto
        }

    return cuenta.saldoInicial + movimiento
}
