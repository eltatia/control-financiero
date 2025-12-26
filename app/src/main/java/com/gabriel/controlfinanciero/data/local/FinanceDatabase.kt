package com.gabriel.controlfinanciero.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private fun applySafeSchemaUpdates(database: SupportSQLiteDatabase) {
            addColumnIfMissing(database, "cuentas", "userId", "INTEGER NOT NULL DEFAULT 0")
            addColumnIfMissing(database, "deudas", "accountId", "INTEGER NOT NULL DEFAULT 0")
            addColumnIfMissing(database, "transacciones", "nota", "TEXT")

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    username TEXT NOT NULL,
                    password TEXT NOT NULL
                )
                """.trimIndent()
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS recordatorios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    titulo TEXT NOT NULL,
                    fechaMillis INTEGER NOT NULL,
                    tipo TEXT NOT NULL,
                    monto REAL,
                    nota TEXT,
                    repeticion TEXT NOT NULL DEFAULT 'NONE',
                    activo INTEGER NOT NULL DEFAULT 1,
                    accountId INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }

        private fun addColumnIfMissing(
            database: SupportSQLiteDatabase,
            tableName: String,
            columnName: String,
            columnDefinition: String
        ) {
            val cursor = database.query("PRAGMA table_info($tableName)")
            cursor.use {
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIndex) == columnName) {
                        return
                    }
                }
            }
            database.execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $columnDefinition")
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                applySafeSchemaUpdates(database)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                applySafeSchemaUpdates(database)
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                applySafeSchemaUpdates(database)
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                applySafeSchemaUpdates(database)
            }
        }
    }
}
