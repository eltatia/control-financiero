package com.gabriel.controlfinanciero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gabriel.controlfinanciero.data.local.entities.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM usuarios WHERE username = :username LIMIT 1")
    suspend fun obtenerPorUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: UserEntity): Long

    @Update
    suspend fun actualizar(usuario: UserEntity)

    @Query("DELETE FROM usuarios WHERE username = :username")
    suspend fun eliminarPorUsername(username: String)
}
