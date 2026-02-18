package com.example.bplog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bplog.data.Measurement
import com.example.bplog.data.MeasurementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HistoryFilter(val days: Int?) {
    DAYS_7(7),
    DAYS_30(30),
    ALL(null)
}

class HistoryViewModel(private val repository: MeasurementRepository) : ViewModel() {

    private val _filter = MutableStateFlow(HistoryFilter.DAYS_7)

    val filter: StateFlow<HistoryFilter> = _filter

    val measurements: StateFlow<List<Measurement>> = repository
        .getAllOrderedByDate()
        .combine(_filter) { list, filter ->
            if (filter.days == null) list
            else {
                val cutoff = System.currentTimeMillis() - filter.days * 24L * 60 * 60 * 1000
                list.filter { it.timestamp >= cutoff }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setFilter(filter: HistoryFilter) {
        _filter.update { filter }
    }

    fun deleteMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            repository.delete(measurement)
        }
    }

    fun updateMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            repository.update(measurement)
        }
    }

    fun insertMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            repository.insert(measurement)
        }
    }

}
