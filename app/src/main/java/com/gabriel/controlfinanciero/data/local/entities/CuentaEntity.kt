package com.gabriel.controlfinanciero.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cuentas")
data class CuentaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val tipo: String,          // "EFECTIVO", "BANCO", etc.
    val saldoInicial: Double = 0.0
)
