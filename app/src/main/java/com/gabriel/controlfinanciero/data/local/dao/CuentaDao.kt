package com.gabriel.controlfinanciero.data.local.dao

import androidx.room.*
import com.gabriel.controlfinanciero.data.local.entities.CuentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CuentaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cuenta: CuentaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(cuentas: List<CuentaEntity>): List<Long>

    @Update
    suspend fun actualizar(cuenta: CuentaEntity)

    @Delete
    suspend fun eliminar(cuenta: CuentaEntity)

    @Query("DELETE FROM cuentas")
    suspend fun eliminarTodas()

    @Query("SELECT * FROM cuentas")
    fun obtenerTodas(): Flow<List<CuentaEntity>>

    @Query("SELECT * FROM cuentas WHERE userId = :userId")
    fun obtenerPorUsuario(userId: Int): Flow<List<CuentaEntity>>

    @Query("SELECT * FROM cuentas WHERE userId = :userId LIMIT 1")
    suspend fun obtenerPrimeraPorUsuario(userId: Int): CuentaEntity?

    @Query("SELECT * FROM cuentas WHERE userId = :userId")
    suspend fun obtenerPorUsuarioSync(userId: Int): List<CuentaEntity>

    @Query("DELETE FROM cuentas WHERE userId = :userId")
    suspend fun eliminarPorUsuario(userId: Int)

    @Query("SELECT * FROM cuentas WHERE id = :id")
    fun obtenerPorId(id: Int): Flow<CuentaEntity?>
}
