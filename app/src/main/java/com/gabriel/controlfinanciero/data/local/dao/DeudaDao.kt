package com.gabriel.controlfinanciero.data.local.dao

import androidx.room.*
import com.gabriel.controlfinanciero.data.local.entities.DeudaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeudaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(deuda: DeudaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(deudas: List<DeudaEntity>): List<Long>

    @Update
    suspend fun actualizar(deuda: DeudaEntity)

    @Delete
    suspend fun eliminar(deuda: DeudaEntity)

    @Query("SELECT * FROM deudas ORDER BY fechaRegistro DESC")
    fun obtenerTodas(): Flow<List<DeudaEntity>>

    @Query("SELECT * FROM deudas WHERE accountId = :accountId ORDER BY fechaRegistro DESC")
    fun obtenerPorCuenta(accountId: Int): Flow<List<DeudaEntity>>

    @Query("SELECT * FROM deudas WHERE estado = :estado ORDER BY fechaRegistro DESC")
    fun obtenerPorEstado(estado: String): Flow<List<DeudaEntity>>

    @Query("DELETE FROM deudas")
    suspend fun eliminarTodas()
}
