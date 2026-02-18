package com.example.bplog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bplog.data.MeasurementRepository

class ViewModelFactory(private val repository: MeasurementRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AddMeasurementViewModel::class.java -> AddMeasurementViewModel(repository)
            HistoryViewModel::class.java -> HistoryViewModel(repository)
            else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")
        } as T
    }
}
