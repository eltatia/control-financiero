package com.gabriel.controlfinanciero.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordatorios")
data class RecordatorioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val fechaMillis: Long,
    val tipo: String, // "PAGO", "COBRO", "NOTA"
    val monto: Double? = null,
    val nota: String? = null,
    val repeticion: String = "NONE", // "NONE", "WEEKLY", "MONTHLY"
    val activo: Boolean = true
)
