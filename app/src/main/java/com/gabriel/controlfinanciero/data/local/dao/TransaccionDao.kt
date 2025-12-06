package com.gabriel.controlfinanciero.data.local.dao

import androidx.room.*
import com.gabriel.controlfinanciero.data.local.entities.TransaccionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransaccionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(transaccion: TransaccionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(transacciones: List<TransaccionEntity>): List<Long>

    @Update
    suspend fun actualizar(transaccion: TransaccionEntity)

    @Delete
    suspend fun eliminar(transaccion: TransaccionEntity)

    @Query("DELETE FROM transacciones")
    suspend fun eliminarTodas()

    // 🔹 Todas las transacciones (la usaremos para saldo actual por cuenta)
    @Query("SELECT * FROM transacciones ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<TransaccionEntity>>

    @Query("""
        SELECT * FROM transacciones
        WHERE fecha BETWEEN :desde AND :hasta
        ORDER BY fecha DESC
    """)
    fun obtenerPorRangoFecha(
        desde: Long,
        hasta: Long
    ): Flow<List<TransaccionEntity>>

    @Query("""
        SELECT SUM(monto) FROM transacciones
        WHERE tipo = :tipo AND fecha BETWEEN :desde AND :hasta
    """)
    fun obtenerTotalPorTipoYRangoFecha(
        tipo: String,
        desde: Long,
        hasta: Long
    ): Flow<Double?>
}
