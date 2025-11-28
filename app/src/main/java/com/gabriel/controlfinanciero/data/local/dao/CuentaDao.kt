package com.gabriel.controlfinanciero.data.local.dao

import androidx.room.*
import com.gabriel.controlfinanciero.data.local.entities.CuentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CuentaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cuenta: CuentaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(cuentas: List<CuentaEntity>)

    @Update
    suspend fun actualizar(cuenta: CuentaEntity)

    @Delete
    suspend fun eliminar(cuenta: CuentaEntity)

    @Query("SELECT * FROM cuentas")
    fun obtenerTodas(): Flow<List<CuentaEntity>>

    @Query("SELECT * FROM cuentas WHERE id = :id")
    fun obtenerPorId(id: Int): Flow<CuentaEntity?>
}
