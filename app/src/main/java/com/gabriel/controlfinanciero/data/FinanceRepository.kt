package com.gabriel.controlfinanciero.data

import android.content.Context
import com.gabriel.controlfinanciero.data.local.FinanceDatabase
import com.gabriel.controlfinanciero.data.local.entities.CuentaEntity
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
