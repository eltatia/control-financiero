package com.gabriel.controlfinanciero.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.text.NumberFormat
import java.util.Locale

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ID, 0)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Recordatorio"
        val tipo = intent.getStringExtra(EXTRA_TIPO) ?: "EVENTO"
        val nota = intent.getStringExtra(EXTRA_NOTA)
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, Double.NaN)

        val subtitle = buildString {
            append(tipo)
            if (!amount.isNaN()) {
                val formatted = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
                    .format(amount)
                append(" · ")
                append(formatted)
            }
            if (!nota.isNullOrBlank()) {
                append(" · ")
                append(nota)
            }
        }

        ReminderNotificationManager.showNotification(
            context = context,
            id = id,
            title = title,
            message = subtitle
        )
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TIPO = "extra_tipo"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_NOTA = "extra_nota"
    }
}
