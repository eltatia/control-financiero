package com.gabriel.controlfinanciero.data

import android.content.Context
import com.gabriel.controlfinanciero.data.local.FinanceDatabase
import com.gabriel.controlfinanciero.data.local.entities.CuentaEntity
import com.gabriel.controlfinanciero.data.local.entities.DeudaEntity
import com.gabriel.controlfinanciero.data.local.entities.TransaccionEntity
import kotlinx.coroutines.flow.Flow

class FinanceRepository private constructor(
    private val db: FinanceDatabase
) {

    // ------------------- CUENTAS -------------------

    fun obtenerCuentas(): Flow<List<CuentaEntity>> =
        db.cuentaDao().obtenerTodas()

    suspend fun crearCuenta(
        nombre: String,
        tipo: String,
        saldoInicial: Double
    ) {
        val cuenta = CuentaEntity(
            nombre = nombre,
            tipo = tipo,
            saldoInicial = saldoInicial
        )
        db.cuentaDao().insertar(cuenta)
    }

    suspend fun actualizarCuenta(cuenta: CuentaEntity) {
        db.cuentaDao().actualizar(cuenta)
    }

    suspend fun eliminarCuenta(cuenta: CuentaEntity) {
        db.cuentaDao().eliminar(cuenta)
    }

    // ------------------- TRANSACCIONES -------------------

    fun obtenerTransaccionesRango(
        desde: Long,
        hasta: Long
    ): Flow<List<TransaccionEntity>> =
        db.transaccionDao().obtenerPorRangoFecha(desde, hasta)

    suspend fun registrarTransaccion(transaccion: TransaccionEntity) {
        db.transaccionDao().insertar(transaccion)
    }

    fun obtenerTotalPorTipoYRango(
        tipo: String,      // "INGRESO" o "EGRESO"
        desde: Long,
        hasta: Long
    ): Flow<Double?> =
        db.transaccionDao().obtenerTotalPorTipoYRangoFecha(tipo, desde, hasta)

    // 🔹 Todas las transacciones (para saldo actual por cuenta)
    fun obtenerTodasTransacciones(): Flow<List<TransaccionEntity>> =
        db.transaccionDao().obtenerTodas()

    // ------------------- DEUDAS / PRÉSTAMOS -------------------

    fun obtenerDeudas(): Flow<List<DeudaEntity>> =
        db.deudaDao().obtenerTodas()

    suspend fun crearDeuda(
        nombre: String,
        montoTotal: Double,
        tipo: String,                // "DEUDA" o "PRESTAMO"
        fechaVencimiento: Long? = null
    ) {
        val ahora = System.currentTimeMillis()
        val deuda = DeudaEntity(
            nombre = nombre,
            montoTotal = montoTotal,
            montoPendiente = montoTotal,   // empieza debiendo todo
            fechaRegistro = ahora,
            fechaVencimiento = fechaVencimiento,
            tipo = tipo,
            estado = "ACTIVA"
        )
        db.deudaDao().insertar(deuda)
    }

    suspend fun actualizarDeuda(deuda: DeudaEntity) {
        db.deudaDao().actualizar(deuda)
    }

    suspend fun eliminarDeuda(deuda: DeudaEntity) {
        db.deudaDao().eliminar(deuda)
    }
    // Registrar abono (pago parcial o total) de una deuda
    suspend fun abonarDeuda(deuda: DeudaEntity, montoAbono: Double) {
        if (montoAbono <= 0.0) return

        val nuevoPendiente = (deuda.montoPendiente - montoAbono).coerceAtLeast(0.0)
        val nuevoEstado = if (nuevoPendiente <= 0.009) "PAGADA" else deuda.estado

        val actualizada = deuda.copy(
            montoPendiente = nuevoPendiente,
            estado = nuevoEstado
        )

        db.deudaDao().actualizar(actualizada)
    }

    // ------------------- SINGLETON -------------------

    companion object {
        @Volatile
        private var INSTANCE: FinanceRepository? = null

        fun getInstance(context: Context): FinanceRepository {
            return INSTANCE ?: synchronized(this) {
                val db = FinanceDatabase.getInstance(context)
                FinanceRepository(db).also { INSTANCE = it }
            }
        }
    }
}
