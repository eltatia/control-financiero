package com.gabriel.controlfinanciero.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gabriel.controlfinanciero.data.FinanceRepository
import com.gabriel.controlfinanciero.data.local.entities.CuentaEntity
import com.gabriel.controlfinanciero.data.local.entities.TransaccionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class FinanceViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: FinanceRepository =
        FinanceRepository.getInstance(application)

    // ------------------- CUENTAS -------------------

    private val _cuentas = MutableStateFlow<List<CuentaEntity>>(emptyList())
    val cuentas: StateFlow<List<CuentaEntity>> = _cuentas.asStateFlow()

    // ------------------- TODAS LAS TRANSACCIONES (para saldo actual por cuenta) -------------------

    private val _todasTransacciones = MutableStateFlow<List<TransaccionEntity>>(emptyList())
    val todasTransacciones: StateFlow<List<TransaccionEntity>> = _todasTransacciones.asStateFlow()

    init {
        // Escuchamos las cuentas de Room
        viewModelScope.launch {
            repository.obtenerCuentas().collect { lista ->
                _cuentas.value = lista
            }
        }

        // Escuchamos todas las transacciones de Room
        viewModelScope.launch {
            repository.obtenerTodasTransacciones().collect { lista ->
                _todasTransacciones.value = lista
            }
        }
    }

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
            repository.obtenerTransaccionesRango(desde, hasta).collect { lista ->
                val ingresos = lista.filter { it.tipo == "INGRESO" }.sumOf { it.monto }
                val egresos = lista.filter { it.tipo == "EGRESO" }.sumOf { it.monto }

                _totalIngresosAnual.value = ingresos
                _totalEgresosAnual.value = egresos
                _balanceAnual.value = ingresos - egresos
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
}
