package com.gabriel.controlfinanciero.data

import android.content.Context
import androidx.room.withTransaction
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

    // ------------------- DATOS DEMO -------------------

    suspend fun cargarDatosDemo() {
        val ahora = System.currentTimeMillis()
        val unDiaMillis = 24 * 60 * 60 * 1000L

        db.withTransaction {
            db.transaccionDao().eliminarTodas()
            db.deudaDao().eliminarTodas()
            db.cuentaDao().eliminarTodas()

            val cuentasIds = db.cuentaDao().insertarLista(
                listOf(
                    CuentaEntity(nombre = "Efectivo", tipo = "EFECTIVO", saldoInicial = 420.0),
                    CuentaEntity(nombre = "Banco Principal", tipo = "BANCO", saldoInicial = 3250.0),
                    CuentaEntity(nombre = "Ahorros viaje", tipo = "BILLETERA", saldoInicial = 1500.0)
                )
            )

            val cuentaEfectivo = cuentasIds.getOrNull(0)?.toInt() ?: 0
            val cuentaBanco = cuentasIds.getOrNull(1)?.toInt() ?: 0
            val cuentaAhorros = cuentasIds.getOrNull(2)?.toInt() ?: 0

            db.transaccionDao().insertarLista(
                listOf(
                    TransaccionEntity(
                        titulo = "Salario", monto = 2500.0, tipo = "INGRESO",
                        fecha = ahora - (3 * unDiaMillis), categoria = "Ingresos", cuentaId = cuentaBanco
                    ),
                    TransaccionEntity(
                        titulo = "Comida semanal", monto = 180.0, tipo = "EGRESO",
                        fecha = ahora - (2 * unDiaMillis), categoria = "Alimentos", cuentaId = cuentaBanco
                    ),
                    TransaccionEntity(
                        titulo = "Gasolina", monto = 60.0, tipo = "EGRESO",
                        fecha = ahora - unDiaMillis, categoria = "Transporte", cuentaId = cuentaBanco
                    ),
                    TransaccionEntity(
                        titulo = "Ahorro mensual", monto = 300.0, tipo = "EGRESO",
                        fecha = ahora - unDiaMillis, categoria = "Ahorros", cuentaId = cuentaBanco
                    ),
                    TransaccionEntity(
                        titulo = "Freelance web", monto = 480.0, tipo = "INGRESO",
                        fecha = ahora - (5 * unDiaMillis), categoria = "Servicios", cuentaId = cuentaEfectivo
                    ),
                    TransaccionEntity(
                        titulo = "Café y snacks", monto = 35.0, tipo = "EGRESO",
                        fecha = ahora, categoria = "Entretenimiento", cuentaId = cuentaEfectivo
                    ),
                    TransaccionEntity(
                        titulo = "Intereses", monto = 25.0, tipo = "INGRESO",
                        fecha = ahora - (10 * unDiaMillis), categoria = "Ahorros", cuentaId = cuentaAhorros
                    )
                )
            )

            db.deudaDao().insertarLista(
                listOf(
                    DeudaEntity(
                        nombre = "Tarjeta crédito", montoTotal = 1200.0, montoPendiente = 850.0,
                        fechaRegistro = ahora - (20 * unDiaMillis), fechaVencimiento = ahora + (10 * unDiaMillis),
                        tipo = "DEUDA", estado = "ACTIVA"
                    ),
                    DeudaEntity(
                        nombre = "Préstamo familiar", montoTotal = 500.0, montoPendiente = 200.0,
                        fechaRegistro = ahora - (40 * unDiaMillis), fechaVencimiento = null,
                        tipo = "PRESTAMO", estado = "ACTIVA"
                    )
                )
            )
        }
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
