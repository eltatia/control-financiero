package com.gabriel.controlfinanciero.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transacciones")
data class TransaccionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val monto: Double,
    val tipo: String,      // "INGRESO" o "EGRESO"
    val fecha: Long,       // timestamp en milisegundos
    val categoria: String,
    val cuentaId: Int,
    val nota: String? = null
)