package com.gabriel.controlfinanciero.data.export

import com.gabriel.controlfinanciero.data.local.entities.CuentaEntity
import com.gabriel.controlfinanciero.data.local.entities.TransaccionEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun buildReportCsv(
    transacciones: List<TransaccionEntity>,
    cuentas: List<CuentaEntity>,
    zoneId: ZoneId = ZoneId.systemDefault(),
    formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
): String {
    val cuentasPorId = cuentas.associateBy { it.id }
    return buildString {
        append("Fecha,Título,Categoría,Tipo,Cuenta,Monto,Nota\n")
        transacciones.sortedBy { it.fecha }.forEach { transaccion ->
            val fecha = Instant.ofEpochMilli(transaccion.fecha)
                .atZone(zoneId)
                .toLocalDate()
                .format(formatter)
            val cuentaNombre = cuentasPorId[transaccion.cuentaId]?.nombre
                ?: "Cuenta ${transaccion.cuentaId}"
            val nota = transaccion.nota ?: ""
            append(
                listOf(
                    fecha,
                    transaccion.titulo,
                    transaccion.categoria,
                    transaccion.tipo,
                    cuentaNombre,
                    "%.2f".format(transaccion.monto),
                    nota
                ).joinToString(",") { value -> csvEscape(value) }
            )
            append("\n")
        }
    }
}

private fun csvEscape(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
        "\"$escaped\""
    } else {
        escaped
    }
}
