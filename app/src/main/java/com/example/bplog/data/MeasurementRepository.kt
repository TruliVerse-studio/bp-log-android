package com.example.bplog.data

import kotlinx.coroutines.flow.Flow

class MeasurementRepository(private val dao: MeasurementDao) {

    suspend fun insert(measurement: Measurement): Long = dao.insert(measurement)

    suspend fun delete(measurement: Measurement) = dao.delete(measurement)

    suspend fun update(measurement: Measurement) = dao.update(measurement)

    fun getAllOrderedByDate(): Flow<List<Measurement>> = dao.getAllOrderedByDate()
}
