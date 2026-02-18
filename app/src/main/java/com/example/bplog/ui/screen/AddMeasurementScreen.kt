package com.example.bplog.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bplog.ui.AddMeasurementEvent
import com.example.bplog.ui.AddMeasurementViewModel
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeasurementScreen(
    viewModel: AddMeasurementViewModel,
    onShowSnackbar: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val zone = ZoneId.systemDefault()
    val dateTimeText = remember(uiState.timestamp) {
        Instant.ofEpochMilli(uiState.timestamp)
            .atZone(zone)
            .toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AddMeasurementEvent.Saved -> onShowSnackbar("Saved")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
        //verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
 
        Text(
            text = "Blood Pressure Log",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 7.dp)
        )
        
        Text(
            text = "Add Measurement",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 10.dp)
        )

        Text(
            text = dateTimeText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 10.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showDatePicker = true }) { 
                Icon(Icons.Filled.DateRange, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Date") 
            }

            OutlinedButton(onClick = { showTimePicker = true }) { 
                Icon(Icons.Filled.Schedule, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Time") 
            }

            OutlinedButton(onClick = { viewModel.resetTimestampToNow() }) { 
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(20.dp))
                // Text("Now") 
            }


        }
      
        OutlinedTextField(
            value = uiState.sys,
            onValueChange = viewModel::updateSys,
            label = { Text("SYS (mmHg)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errorMessage != null
        )
        OutlinedTextField(
            value = uiState.dia,
            onValueChange = viewModel::updateDia,
            label = { Text("DIA (mmHg)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errorMessage != null
        )
        OutlinedTextField(
            value = uiState.pulse,
            onValueChange = viewModel::updatePulse,
            label = { Text("Pulse (bpm)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errorMessage != null
        )
        uiState.errorMessage?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = viewModel::save,
            modifier = Modifier.fillMaxWidth()
        )

         {

            Text("Save")
        }
        Text(
            text = "© 2026 TruliVerse",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            textAlign = TextAlign.Center
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.timestamp
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
                            val currentTime = Instant.ofEpochMilli(uiState.timestamp).atZone(zone).toLocalTime()
                            val newTs = selectedDate.atTime(currentTime).atZone(zone).toInstant().toEpochMilli()
                            viewModel.updateTimestamp(newTs)
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            val initialTime = Instant.ofEpochMilli(uiState.timestamp).atZone(zone).toLocalTime()

            val timePickerState = rememberTimePickerState(
                initialHour = initialTime.hour,
                initialMinute = initialTime.minute,
                is24Hour = true
            )

            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text("Select time") },
                text = { TimePicker(state = timePickerState) },
                confirmButton = {
                    TextButton(onClick = {
                        val currentDate = Instant.ofEpochMilli(uiState.timestamp).atZone(zone).toLocalDate()
                        val newTs = currentDate
                            .atTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                            .atZone(zone)
                            .toInstant()
                            .toEpochMilli()
                        viewModel.updateTimestamp(newTs)
                        showTimePicker = false
                    }) { Text("OK") }
                 },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                }
            )
        }   
    }
}
