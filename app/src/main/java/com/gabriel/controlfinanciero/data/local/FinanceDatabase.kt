package com.gabriel.controlfinanciero.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gabriel.controlfinanciero.data.local.dao.CuentaDao
import com.gabriel.controlfinanciero.data.local.dao.DeudaDao
import com.gabriel.controlfinanciero.data.local.dao.RecordatorioDao
import com.gabriel.controlfinanciero.data.local.dao.TransaccionDao
import com.gabriel.controlfinanciero.data.local.dao.UserDao
import com.gabriel.controlfinanciero.data.local.entities.CuentaEntity
import com.gabriel.controlfinanciero.data.local.entities.DeudaEntity
import com.gabriel.controlfinanciero.data.local.entities.RecordatorioEntity
import com.gabriel.controlfinanciero.data.local.entities.TransaccionEntity
import com.gabriel.controlfinanciero.data.local.entities.UserEntity

@Database(
    entities = [
        TransaccionEntity::class,
        CuentaEntity::class,
        DeudaEntity::class,
        RecordatorioEntity::class,
        UserEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun transaccionDao(): TransaccionDao
    abstract fun cuentaDao(): CuentaDao
    abstract fun deudaDao(): DeudaDao
    abstract fun recordatorioDao(): RecordatorioDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getInstance(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_db"
                )
                    .fallbackToDestructiveMigration() // por ahora destruimos en cambios de versión
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
