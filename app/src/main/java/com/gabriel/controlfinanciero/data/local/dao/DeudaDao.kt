package com.gabriel.controlfinanciero.data.local.dao

import androidx.room.*
import com.gabriel.controlfinanciero.data.local.entities.DeudaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeudaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(deuda: DeudaEntity)

    @Update
    suspend fun actualizar(deuda: DeudaEntity)

    @Delete
    suspend fun eliminar(deuda: DeudaEntity)

    @Query("SELECT * FROM deudas ORDER BY fechaRegistro DESC")
    fun obtenerTodas(): Flow<List<DeudaEntity>>

    @Query("SELECT * FROM deudas WHERE estado = :estado ORDER BY fechaRegistro DESC")
    fun obtenerPorEstado(estado: String): Flow<List<DeudaEntity>>
}
