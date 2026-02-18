package com.example.bplog.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {

    @Insert
    suspend fun insert(measurement: Measurement): Long

    @Delete
    suspend fun delete(measurement: Measurement)

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    fun getAllOrderedByDate(): Flow<List<Measurement>>

    @Update
    suspend fun update(measurement: Measurement)

}
