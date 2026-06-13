package com.fitlog.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlog.data.local.entity.BodyWeight
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWeightDao {
    /** Weight history for one user only — never mixes other profiles' entries. */
    @Query("SELECT * FROM body_weight WHERE userId = :userId ORDER BY date ASC")
    fun observeForUser(userId: Long): Flow<List<BodyWeight>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: BodyWeight)

    @Delete
    suspend fun delete(entry: BodyWeight)
}
