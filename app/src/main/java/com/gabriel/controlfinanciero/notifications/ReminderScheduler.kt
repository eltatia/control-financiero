package com.gabriel.controlfinanciero.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.gabriel.controlfinanciero.data.local.entities.RecordatorioEntity

object ReminderScheduler {
    fun schedule(context: Context, recordatorio: RecordatorioEntity) {
        if (recordatorio.id <= 0) return
        val triggerAt = recordatorio.fechaMillis
        if (triggerAt <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            buildPendingIntent(context, recordatorio)
        )
    }

    fun cancel(context: Context, recordatorioId: Int) {
        if (recordatorioId <= 0) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(context, recordatorioId))
    }

    private fun buildPendingIntent(
        context: Context,
        recordatorio: RecordatorioEntity
    ): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_ID, recordatorio.id)
            putExtra(ReminderAlarmReceiver.EXTRA_TITLE, recordatorio.titulo)
            putExtra(ReminderAlarmReceiver.EXTRA_TIPO, recordatorio.tipo)
            putExtra(ReminderAlarmReceiver.EXTRA_AMOUNT, recordatorio.monto ?: Double.NaN)
            putExtra(ReminderAlarmReceiver.EXTRA_NOTA, recordatorio.nota)
        }
        return PendingIntent.getBroadcast(
            context,
            recordatorio.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildPendingIntent(context: Context, recordatorioId: Int): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            recordatorioId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
