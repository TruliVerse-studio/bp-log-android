package com.example.bplog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bplog.data.Measurement
import com.example.bplog.data.MeasurementRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddMeasurementUiState(
    val sys: String = "",
    val dia: String = "",
    val pulse: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

sealed class AddMeasurementEvent {
    data object Saved : AddMeasurementEvent()
}

class AddMeasurementViewModel(private val repository: MeasurementRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMeasurementUiState())
    val uiState: StateFlow<AddMeasurementUiState> = _uiState

    private val _events = MutableSharedFlow<AddMeasurementEvent>()
    val events: SharedFlow<AddMeasurementEvent> = _events.asSharedFlow()

    fun updateSys(value: String) {
        _uiState.update { it.copy(sys = value.filter { c -> c.isDigit() }, errorMessage = null) }
    }

    fun updateDia(value: String) {
        _uiState.update { it.copy(dia = value.filter { c -> c.isDigit() }, errorMessage = null) }
    }

    fun updatePulse(value: String) {
        _uiState.update { it.copy(pulse = value.filter { c -> c.isDigit() }, errorMessage = null) }
    }

    fun updateTimestamp(newTimestamp: Long) {
        _uiState.update { it.copy(timestamp = newTimestamp) }
    }

    fun resetTimestampToNow() {
        _uiState.update { it.copy(timestamp = System.currentTimeMillis()) }
    }

    fun save() {
        val state = _uiState.value
        val error = when {
            state.sys.isBlank() -> "Enter SYS (mmHg)"
            state.dia.isBlank() -> "Enter DIA (mmHg)"
            state.pulse.isBlank() -> "Enter PULSE (bpm)"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }
        viewModelScope.launch {
            repository.insert(
                Measurement(
                    timestamp = state.timestamp,
                    sys = state.sys.toInt(),
                    dia = state.dia.toInt(),
                    pulse = state.pulse.toInt()
                )
            )
            _uiState.value = AddMeasurementUiState(timestamp = System.currentTimeMillis())
            _events.emit(AddMeasurementEvent.Saved)
        }
    }
}
