package com.gabriel.controlfinanciero.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabriel.controlfinanciero.data.FinanceRepository
import com.gabriel.controlfinanciero.data.local.entities.CuentaEntity
import com.gabriel.controlfinanciero.data.local.entities.DeudaEntity
import com.gabriel.controlfinanciero.data.local.entities.RecordatorioEntity
import com.gabriel.controlfinanciero.data.local.entities.TransaccionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DayMarker(
    val ingreso: Boolean,
    val egreso: Boolean,
    val recordatorio: Boolean
)

sealed class CalendarEvent {
    abstract val date: LocalDate
    abstract val title: String
    abstract val subtitle: String
    abstract val amount: Double?

    data class Tx(
        override val date: LocalDate,
        override val title: String,
        override val subtitle: String,
        override val amount: Double,
        val tipo: String
    ) : CalendarEvent()

    data class Rem(
        val id: Int,
        override val date: LocalDate,
        override val title: String,
        override val subtitle: String,
        override val amount: Double?,
        val tipo: String,
        val nota: String?
    ) : CalendarEvent()

    data class VenceDeuda(
        override val date: LocalDate,
        override val title: String,
        override val subtitle: String,
        override val amount: Double?,
        val estado: String
    ) : CalendarEvent()
}

class FinanceViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: FinanceRepository =
        FinanceRepository.getInstance(application)

    // ------------------- CALENDARIO -------------------

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _mode = MutableStateFlow("MES")
    val mode: StateFlow<String> = _mode.asStateFlow()

    private val _filter = MutableStateFlow("TODOS")
    val filter: StateFlow<String> = _filter.asStateFlow()

    private val _visibleRange = MutableStateFlow(0L to 0L)
    val visibleRange: StateFlow<Pair<Long, Long>> = _visibleRange.asStateFlow()

    private val _dayMarkers = MutableStateFlow<Map<LocalDate, DayMarker>>(emptyMap())
    val dayMarkers: StateFlow<Map<LocalDate, DayMarker>> = _dayMarkers.asStateFlow()

    private val _eventosDelDia = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val eventosDelDia: StateFlow<List<CalendarEvent>> = _eventosDelDia.asStateFlow()

    private val _totalIngresosDia = MutableStateFlow(0.0)
    val totalIngresosDia: StateFlow<Double> = _totalIngresosDia.asStateFlow()

    private val _totalEgresosDia = MutableStateFlow(0.0)
    val totalEgresosDia: StateFlow<Double> = _totalEgresosDia.asStateFlow()

    private val _balanceDia = MutableStateFlow(0.0)
    val balanceDia: StateFlow<Double> = _balanceDia.asStateFlow()

    // ------------------- CUENTAS -------------------

    private val _cuentas = MutableStateFlow<List<CuentaEntity>>(emptyList())
    val cuentas: StateFlow<List<CuentaEntity>> = _cuentas.asStateFlow()

    // ------------------- DEUDAS / PRÉSTAMOS -------------------

    private val _deudas = MutableStateFlow<List<DeudaEntity>>(emptyList())
    val deudas: StateFlow<List<DeudaEntity>> = _deudas.asStateFlow()

    // ------------------- TODAS LAS TRANSACCIONES (para saldo actual por cuenta) -------------------

    private val _todasTransacciones = MutableStateFlow<List<TransaccionEntity>>(emptyList())
    val todasTransacciones: StateFlow<List<TransaccionEntity>> = _todasTransacciones.asStateFlow()

    // ------------------- SALDOS DE CUENTAS -------------------

    private val _saldoActualCuentas = MutableStateFlow(0.0)
    val saldoActualCuentas: StateFlow<Double> = _saldoActualCuentas.asStateFlow()

    private val _saldoNetoTrasDeudas = MutableStateFlow(0.0)
    val saldoNetoTrasDeudas: StateFlow<Double> = _saldoNetoTrasDeudas.asStateFlow()

    // ------------------- RECORDATORIOS (HOME) -------------------

    private val _recordatoriosProximos = MutableStateFlow<List<RecordatorioEntity>>(emptyList())
    val recordatoriosProximos: StateFlow<List<RecordatorioEntity>> = _recordatoriosProximos.asStateFlow()

    // ------------------- AJUSTES (NOMBRE / CUENTA) -------------------

    private val _nombreUsuario = MutableStateFlow("Alex")
    val nombreUsuario: StateFlow<String> = _nombreUsuario.asStateFlow()

    private val _cuentaActualId = MutableStateFlow(0)
    val cuentaActualId: StateFlow<Int> = _cuentaActualId.asStateFlow()

    private val _cuentaActual = MutableStateFlow<CuentaEntity?>(null)
    val cuentaActual: StateFlow<CuentaEntity?> = _cuentaActual.asStateFlow()

    private val rangeDataFlow = visibleRange.flatMapLatest { (desde, hasta) ->
        combine(
            repository.obtenerTransaccionesRango(desde, hasta),
            repository.obtenerRecordatoriosRango(desde, hasta),
            deudas
        ) { transacciones, recordatorios, deudasLista ->
            CalendarRangeData(transacciones, recordatorios, deudasLista)
        }
    }

    // ------------------- INIT -------------------

    init {
        setMes(LocalDate.now())

        // Cuentas
        viewModelScope.launch {
            repository.obtenerCuentas().collect { lista ->
                _cuentas.value = lista
            }
        }

        // Todas las transacciones
        viewModelScope.launch {
            repository.obtenerTodasTransacciones().collect { lista ->
                _todasTransacciones.value = lista
            }
        }

        // Deudas / préstamos
        viewModelScope.launch {
            repository.obtenerDeudas().collect { lista ->
                _deudas.value = lista
            }
        }

        viewModelScope.launch {
            repository.userName.collect { nombre ->
                _nombreUsuario.value = nombre
            }
        }

        viewModelScope.launch {
            repository.accountId.collect { cuentaId ->
                _cuentaActualId.value = cuentaId
            }
        }

        viewModelScope.launch {
            combine(cuentas, cuentaActualId) { cuentasLista, cuentaId ->
                cuentasLista.firstOrNull { it.id == cuentaId }
                    ?: cuentasLista.firstOrNull()
            }.collect { cuenta ->
                _cuentaActual.value = cuenta
            }
        }

        // Saldo total de cuentas y saldo neto descontando deudas activas
        viewModelScope.launch {
            combine(cuentas, todasTransacciones, deudas) { cuentasLista, transacciones, deudasLista ->
                val saldoCuentas = cuentasLista.sumOf { cuenta ->
                    val movimiento = transacciones
                        .filter { it.cuentaId == cuenta.id }
                        .sumOf { trans -> if (trans.tipo == "INGRESO") trans.monto else -trans.monto }
                    cuenta.saldoInicial + movimiento
                }

                val deudasPendientes = deudasLista
                    .filter { it.estado == "ACTIVA" }
                    .sumOf { it.montoPendiente }

                Pair(saldoCuentas, saldoCuentas - deudasPendientes)
            }.collect { (saldoCuentas, saldoNeto) ->
                _saldoActualCuentas.value = saldoCuentas
                _saldoNetoTrasDeudas.value = saldoNeto
            }
        }

        viewModelScope.launch {
            val hoy = LocalDate.now()
            val zoneId = ZoneId.systemDefault()
            val desde = hoy.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val hasta = hoy.plusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
            repository.obtenerRecordatoriosRango(desde, hasta).collect { lista ->
                _recordatoriosProximos.value = lista
            }
        }

        viewModelScope.launch {
            rangeDataFlow.collect { data ->
                _dayMarkers.value = buildDayMarkers(data)
            }
        }

        viewModelScope.launch {
            combine(rangeDataFlow, selectedDate, filter) { data, date, filtro ->
                buildEventosDelDia(data, date, filtro)
            }.collect { resultado ->
                _eventosDelDia.value = resultado.eventos
                _totalIngresosDia.value = resultado.ingresos
                _totalEgresosDia.value = resultado.egresos
                _balanceDia.value = resultado.ingresos - resultado.egresos
            }
        }
    }

    // ------------------- OPERACIONES CUENTAS -------------------

    fun crearCuenta(
        nombre: String,
        tipo: String,
        saldoInicial: Double
    ) {
        viewModelScope.launch {
            repository.crearCuenta(nombre, tipo, saldoInicial)
        }
    }

    fun actualizarCuenta(cuenta: CuentaEntity) {
        viewModelScope.launch {
            repository.actualizarCuenta(cuenta)
        }
    }

    fun eliminarCuenta(cuenta: CuentaEntity) {
        viewModelScope.launch {
            repository.eliminarCuenta(cuenta)
        }
    }

    // ------------------- OPERACIONES DEUDAS -------------------

    fun crearDeuda(
        nombre: String,
        montoTotal: Double,
        tipo: String,                 // "DEUDA" o "PRESTAMO"
        fechaVencimiento: Long? = null
    ) {
        viewModelScope.launch {
            repository.crearDeuda(
                nombre = nombre,
                montoTotal = montoTotal,
                tipo = tipo,
                fechaVencimiento = fechaVencimiento
            )
        }
    }

    fun actualizarDeuda(deuda: DeudaEntity) {
        viewModelScope.launch {
            repository.actualizarDeuda(deuda)
        }
    }

    fun eliminarDeuda(deuda: DeudaEntity) {
        viewModelScope.launch {
            repository.eliminarDeuda(deuda)
        }
    }
    fun abonarDeuda(deuda: DeudaEntity, montoAbono: Double) {
        viewModelScope.launch {
            repository.abonarDeuda(deuda, montoAbono)
        }
    }


    // ------------------- TRANSACCIONES DEL MES -------------------

    private val _transaccionesMes = MutableStateFlow<List<TransaccionEntity>>(emptyList())
    val transaccionesMes: StateFlow<List<TransaccionEntity>> = _transaccionesMes.asStateFlow()

    private val _totalIngresosMes = MutableStateFlow(0.0)
    val totalIngresosMes: StateFlow<Double> = _totalIngresosMes.asStateFlow()

    private val _totalEgresosMes = MutableStateFlow(0.0)
    val totalEgresosMes: StateFlow<Double> = _totalEgresosMes.asStateFlow()

    private val _balanceMes = MutableStateFlow(0.0)
    val balanceMes: StateFlow<Double> = _balanceMes.asStateFlow()

    /**
     * Carga las transacciones del mes de la fecha dada (por defecto el mes actual)
     * y recalcula ingresos, egresos y balance.
     */
    fun cargarDatosMes(fecha: LocalDate = LocalDate.now()) {
        val firstDay = fecha.withDayOfMonth(1)
        val lastDay = fecha.withDayOfMonth(fecha.lengthOfMonth())

        val zoneId = ZoneId.systemDefault()
        val desde = firstDay.atStartOfDay(zoneId).toInstant().toEpochMilli()
        // Hasta el último milisegundo del último día
        val hasta = lastDay.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        viewModelScope.launch {
            repository.obtenerTransaccionesRango(desde, hasta).collect { lista ->
                _transaccionesMes.value = lista

                val ingresos = lista.filter { it.tipo == "INGRESO" }.sumOf { it.monto }
                val egresos = lista.filter { it.tipo == "EGRESO" }.sumOf { it.monto }

                _totalIngresosMes.value = ingresos
                _totalEgresosMes.value = egresos
                _balanceMes.value = ingresos - egresos
            }
        }
    }

    fun setMes(fecha: LocalDate) {
        _mode.value = "MES"
        val firstDay = fecha.withDayOfMonth(1)
        val lastDay = fecha.withDayOfMonth(fecha.lengthOfMonth())
        val zoneId = ZoneId.systemDefault()
        val desde = firstDay.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val hasta = lastDay.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        _visibleRange.value = desde to hasta
        _selectedDate.value = fecha
    }

    fun setSemana(fecha: LocalDate) {
        _mode.value = "SEMANA"
        val startOfWeek = fecha.minusDays((fecha.dayOfWeek.value % 7).toLong())
        val endOfWeek = startOfWeek.plusDays(6)
        val zoneId = ZoneId.systemDefault()
        val desde = startOfWeek.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val hasta = endOfWeek.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        _visibleRange.value = desde to hasta
        _selectedDate.value = fecha
    }

    fun seleccionarDia(dia: LocalDate) {
        _selectedDate.value = dia
    }

    fun setFiltro(filtro: String) {
        _filter.value = filtro
    }

    fun crearRecordatorio(
        titulo: String,
        tipo: String,
        monto: Double?,
        nota: String?,
        fechaSeleccionada: LocalDate
    ) {
        if (titulo.isBlank()) return
        val zoneId = ZoneId.systemDefault()
        val fechaMillis = fechaSeleccionada.atStartOfDay(zoneId).toInstant().toEpochMilli()

        viewModelScope.launch {
            val recordatorio = RecordatorioEntity(
                titulo = titulo,
                fechaMillis = fechaMillis,
                tipo = tipo,
                monto = monto,
                nota = nota
            )
            repository.crearRecordatorio(recordatorio)
        }
    }

    fun actualizarRecordatorio(
        id: Int,
        titulo: String,
        tipo: String,
        monto: Double?,
        nota: String?,
        fechaSeleccionada: LocalDate,
        repeticion: String = "NONE",
        activo: Boolean = true
    ) {
        if (titulo.isBlank()) return
        val zoneId = ZoneId.systemDefault()
        val fechaMillis = fechaSeleccionada.atStartOfDay(zoneId).toInstant().toEpochMilli()

        viewModelScope.launch {
            val recordatorio = RecordatorioEntity(
                id = id,
                titulo = titulo,
                fechaMillis = fechaMillis,
                tipo = tipo,
                monto = monto,
                nota = nota,
                repeticion = repeticion,
                activo = activo
            )
            repository.actualizarRecordatorio(recordatorio)
        }
    }

    fun eliminarRecordatorio(id: Int) {
        viewModelScope.launch {
            val recordatorio = RecordatorioEntity(
                id = id,
                titulo = "",
                fechaMillis = 0L,
                tipo = "NOTA",
                monto = null,
                nota = null,
                repeticion = "NONE",
                activo = false
            )
            repository.eliminarRecordatorio(recordatorio)
        }
    }

    private fun buildDayMarkers(data: CalendarRangeData): Map<LocalDate, DayMarker> {
        val markers = mutableMapOf<LocalDate, DayMarker>()
        val zoneId = ZoneId.systemDefault()

        fun updateMarker(
            date: LocalDate,
            ingreso: Boolean = false,
            egreso: Boolean = false,
            recordatorio: Boolean = false
        ) {
            val current = markers[date] ?: DayMarker(false, false, false)
            markers[date] = current.copy(
                ingreso = current.ingreso || ingreso,
                egreso = current.egreso || egreso,
                recordatorio = current.recordatorio || recordatorio
            )
        }

        data.transacciones.forEach { transaccion ->
            val date = Instant.ofEpochMilli(transaccion.fecha).atZone(zoneId).toLocalDate()
            updateMarker(
                date = date,
                ingreso = transaccion.tipo == "INGRESO",
                egreso = transaccion.tipo == "EGRESO"
            )
        }

        data.recordatorios.forEach { recordatorio ->
            val date = Instant.ofEpochMilli(recordatorio.fechaMillis).atZone(zoneId).toLocalDate()
            updateMarker(date = date, recordatorio = true)
        }

        val (desde, hasta) = _visibleRange.value
        data.deudas.forEach { deuda ->
            val fechaMillis = deuda.fechaVencimiento ?: return@forEach
            if (deuda.estado != "ACTIVA") return@forEach
            if (fechaMillis < desde || fechaMillis > hasta) return@forEach
            val date = Instant.ofEpochMilli(fechaMillis).atZone(zoneId).toLocalDate()
            updateMarker(date = date, recordatorio = true)
        }

        return markers.toSortedMap()
    }

    private fun buildEventosDelDia(
        data: CalendarRangeData,
        date: LocalDate,
        filtro: String
    ): DayEventsResult {
        val zoneId = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ofPattern("d 'de' MMM", Locale("es", "ES"))
        val eventos = mutableListOf<CalendarEvent>()

        data.transacciones.forEach { transaccion ->
            val transDate = Instant.ofEpochMilli(transaccion.fecha).atZone(zoneId).toLocalDate()
            if (transDate == date) {
                val subtitle = if (transaccion.categoria.isNotBlank()) {
                    transaccion.categoria
                } else {
                    formatter.format(transDate)
                }
                eventos.add(
                    CalendarEvent.Tx(
                        date = transDate,
                        title = transaccion.titulo,
                        subtitle = subtitle,
                        amount = transaccion.monto,
                        tipo = transaccion.tipo
                    )
                )
            }
        }

        data.recordatorios.forEach { recordatorio ->
            val remDate = Instant.ofEpochMilli(recordatorio.fechaMillis).atZone(zoneId).toLocalDate()
            if (remDate == date) {
                val subtitle = recordatorio.nota ?: recordatorio.tipo
                eventos.add(
                    CalendarEvent.Rem(
                        id = recordatorio.id,
                        date = remDate,
                        title = recordatorio.titulo,
                        subtitle = subtitle,
                        amount = recordatorio.monto,
                        tipo = recordatorio.tipo,
                        nota = recordatorio.nota
                    )
                )
            }
        }

        data.deudas.forEach { deuda ->
            val fechaMillis = deuda.fechaVencimiento ?: return@forEach
            if (deuda.estado != "ACTIVA") return@forEach
            val deudaDate = Instant.ofEpochMilli(fechaMillis).atZone(zoneId).toLocalDate()
            if (deudaDate == date) {
                eventos.add(
                    CalendarEvent.VenceDeuda(
                        date = deudaDate,
                        title = deuda.nombre,
                        subtitle = "Vence el ${formatter.format(deudaDate)}",
                        amount = deuda.montoPendiente,
                        estado = deuda.estado
                    )
                )
            }
        }

        val ingresos = eventos.filterIsInstance<CalendarEvent.Tx>()
            .filter { it.tipo == "INGRESO" }
            .sumOf { it.amount }
        val egresos = eventos.filterIsInstance<CalendarEvent.Tx>()
            .filter { it.tipo == "EGRESO" }
            .sumOf { it.amount }

        val filtrados = when (filtro) {
            "INGRESOS" -> eventos.filterIsInstance<CalendarEvent.Tx>().filter { it.tipo == "INGRESO" }
            "EGRESOS" -> eventos.filterIsInstance<CalendarEvent.Tx>().filter { it.tipo == "EGRESO" }
            "RECORDATORIOS" -> eventos.filter {
                it is CalendarEvent.Rem || it is CalendarEvent.VenceDeuda
            }
            else -> eventos
        }

        val ordenados = filtrados.sortedWith(
            compareBy<CalendarEvent> { event ->
                when (event) {
                    is CalendarEvent.Tx -> if (event.tipo == "INGRESO") 0 else 1
                    is CalendarEvent.Rem -> 2
                    is CalendarEvent.VenceDeuda -> 3
                }
            }.thenBy { it.title }
        )

        return DayEventsResult(ordenados, ingresos, egresos)
    }

    // ------------------- TRANSACCIONES ANUALES -------------------

    private val _totalIngresosAnual = MutableStateFlow(0.0)
    val totalIngresosAnual: StateFlow<Double> = _totalIngresosAnual.asStateFlow()

    private val _totalEgresosAnual = MutableStateFlow(0.0)
    val totalEgresosAnual: StateFlow<Double> = _totalEgresosAnual.asStateFlow()

    private val _balanceAnual = MutableStateFlow(0.0)
    val balanceAnual: StateFlow<Double> = _balanceAnual.asStateFlow()

    /**
     * Carga las transacciones del año dado (por defecto el año actual)
     * y recalcula ingresos, egresos y balance anual.
     */
    fun cargarDatosAnuales(anio: Int = LocalDate.now().year) {
        val firstDay = LocalDate.of(anio, 1, 1)
        val lastDay = LocalDate.of(anio, 12, 31)

        val zoneId = ZoneId.systemDefault()
        val desde = firstDay.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val hasta = lastDay.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        viewModelScope.launch {
            combine(
                repository.obtenerTransaccionesRango(desde, hasta),
                cuentas
            ) { lista, cuentasLista ->
                val ingresos = lista.filter { it.tipo == "INGRESO" }.sumOf { it.monto }
                val ingresosIniciales = cuentasLista
                    .filter { it.saldoInicial > 0 }
                    .sumOf { it.saldoInicial }
                val egresos = lista.filter { it.tipo == "EGRESO" }.sumOf { it.monto }

                Pair(ingresos + ingresosIniciales, egresos)
            }.collect { (ingresosTotales, egresos) ->
                _totalIngresosAnual.value = ingresosTotales
                _totalEgresosAnual.value = egresos
                _balanceAnual.value = ingresosTotales - egresos
            }
        }
    }

    // ------------------- REGISTRAR TRANSACCIÓN -------------------

    /**
     * Agrega una nueva transacción (ingreso o egreso) a la base de datos.
     */
    fun registrarTransaccion(
        titulo: String,
        monto: Double,
        tipo: String,        // "INGRESO" o "EGRESO"
        categoria: String,
        cuentaId: Int,
        fechaMillis: Long = System.currentTimeMillis(),
        nota: String? = null
    ) {
        viewModelScope.launch {
            val transaccion = TransaccionEntity(
                titulo = titulo,
                monto = monto,
                tipo = tipo,
                fecha = fechaMillis,
                categoria = categoria,
                cuentaId = cuentaId,
                nota = nota
            )
            repository.registrarTransaccion(transaccion)
        }
    }

    fun cargarDatosDemo() {
        viewModelScope.launch {
            repository.cargarDatosDemo()
            cargarDatosMes()
            cargarDatosAnuales()
        }
    }

    fun setNombreUsuario(nombre: String) {
        viewModelScope.launch {
            repository.setUserName(nombre)
        }
    }

    fun setCuentaActual(cuentaId: Int) {
        viewModelScope.launch {
            repository.setAccountId(cuentaId)
        }
    }

    private data class CalendarRangeData(
        val transacciones: List<TransaccionEntity>,
        val recordatorios: List<RecordatorioEntity>,
        val deudas: List<DeudaEntity>
    )

    private data class DayEventsResult(
        val eventos: List<CalendarEvent>,
        val ingresos: Double,
        val egresos: Double
    )

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
                FinanceViewModel(app)
            }
        }
    }
}
