package com.gabriel.controlfinanciero.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gabriel.controlfinanciero.data.local.dao.CuentaDao
import com.gabriel.controlfinanciero.data.local.dao.TransaccionDao
import com.gabriel.controlfinanciero.data.local.entities.CuentaEntity
import com.gabriel.controlfinanciero.data.local.entities.TransaccionEntity

@Database(
    entities = [
        TransaccionEntity::class,
        CuentaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun transaccionDao(): TransaccionDao
    abstract fun cuentaDao(): CuentaDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
