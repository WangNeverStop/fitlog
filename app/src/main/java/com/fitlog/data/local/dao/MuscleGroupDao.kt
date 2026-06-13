package com.fitlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fitlog.data.local.entity.MuscleGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface MuscleGroupDao {
    @Query("SELECT * FROM muscle_group ORDER BY id ASC")
    fun observeAll(): Flow<List<MuscleGroup>>

    @Query("SELECT * FROM muscle_group ORDER BY id ASC")
    suspend fun getAll(): List<MuscleGroup>

    @Query("SELECT * FROM muscle_group WHERE id = :id")
    suspend fun getById(id: Long): MuscleGroup?

    @Query("SELECT COUNT(*) FROM muscle_group")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(groups: List<MuscleGroup>): List<Long>
}
