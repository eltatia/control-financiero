package com.gabriel.controlfinanciero.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {

    @Query(
        """
        SELECT * FROM recordatorios
        WHERE fechaMillis BETWEEN :desde AND :hasta
        AND activo = 1
        ORDER BY fechaMillis DESC
        """
    )
    fun obtenerRecordatoriosRango(
        desde: Long,
        hasta: Long
    ): Flow<List<RecordatorioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(recordatorio: RecordatorioEntity): Long

    @Update
    suspend fun actualizar(recordatorio: RecordatorioEntity)

    @Delete
    suspend fun eliminar(recordatorio: RecordatorioEntity)
}