package com.gabriel.controlfinanciero.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deudas")
data class DeudaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,                 // Ej: "Préstamo moto", "Deuda con Paty"
    val montoTotal: Double,
    val montoPendiente: Double,
    val fechaRegistro: Long,            // millis
    val fechaVencimiento: Long?,        // nullable si no hay
    val tipo: String,                   // "DEUDA" o "PRESTAMO"
    val estado: String                  // "ACTIVA" o "PAGADA"
)
