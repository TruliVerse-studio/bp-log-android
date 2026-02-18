package com.example.bplog.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.bplog.data.Measurement
import com.example.bplog.ui.HistoryFilter
import com.example.bplog.ui.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.launch

import androidx.compose.ui.platform.LocalContext
import com.example.bplog.data.ExportUtil

private val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val measurements by viewModel.measurements.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    var pendingEdit by remember { mutableStateOf<Measurement?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Measurement?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }

    var chartRef by remember { mutableStateOf<com.github.mikephil.charting.charts.LineChart?>(null) }

    Scaffold(
        snackbarHost = {SnackbarHost(hostState = snackbarHostState)}    
    ){ padding ->

    Column(
        modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
 
            Text(
                text = "History",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            OutlinedButton(onClick = { showExportDialog = true }) {
                Text("Export")
            }
        
        }

        FilterChips(
            currentFilter = filter,
            onFilterSelected = viewModel::setFilter,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        if (measurements.isNotEmpty()) {
            BpLineChart(
                measurements = measurements,
                filter = filter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                onChartReady = { chartRef = it }
            )
        }

        if (measurements.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No measurements yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(measurements, key = { it.id }) { measurement ->
                    MeasurementItem(
                        measurement = measurement,
                        onEdit = { measurementToEdit ->
                            pendingEdit = measurementToEdit
                            showEditDialog = true 
                        },

                        onDelete = { measurementToDelete ->
                            pendingDelete = measurementToDelete
                            showDeleteDialog = true                        
                        }
                    )
                }
            }
            if (showEditDialog && pendingEdit != null) {
                val current = pendingEdit!!

                var sysText by remember(current.id) { mutableStateOf(current.sys.toString()) }
                var diaText by remember(current.id) { mutableStateOf(current.dia.toString()) }
                var pulseText by remember(current.id) { mutableStateOf(current.pulse.toString()) }

                val zone = ZoneId.systemDefault()

                var editedTimestamp by remember(current.id) { mutableStateOf(current.timestamp) }

                val dateTimeText = remember(editedTimestamp) {
                    Instant.ofEpochMilli(editedTimestamp)
                        .atZone(zone)
                        .toLocalDateTime()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
                }

                var showDatePicker by remember { mutableStateOf(false) }
                var showTimePicker by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = {
                        showEditDialog = false
                        pendingEdit = null
                        },
                    title = { Text("Edit measurement") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                            Text(
                                text = dateTimeText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { showDatePicker = true }) {
                                    Text("Change date")
                                }
                                OutlinedButton(onClick = { showTimePicker = true }) {
                                    Text("Change time")
                                }
                            }

                            OutlinedTextField(
                                value = sysText,
                                onValueChange = { sysText = it.filter(Char::isDigit) },
                                label = { Text("SYS") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = diaText,
                                onValueChange = { diaText = it.filter(Char::isDigit) },
                                label = { Text("DIA") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = pulseText,
                                onValueChange = { pulseText = it.filter(Char::isDigit) },
                                label = { Text("Pulse") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val sys = sysText.toIntOrNull()
                            val dia = diaText.toIntOrNull()
                            val pulse = pulseText.toIntOrNull()

                            if (sys != null && dia != null && pulse != null) {
                                viewModel.updateMeasurement(
                                    current.copy(
                                        sys = sys, 
                                        dia = dia, 
                                        pulse = pulse,
                                        timestamp = editedTimestamp
                                        )
                                )
                                showEditDialog = false
                                pendingEdit = null
                            }
                        }) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showEditDialog = false
                            pendingEdit = null
                        }) { Text("Cancel") }
                    }
                )

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = editedTimestamp
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val zone = ZoneId.systemDefault()
                                    val selectedDate = Instant.ofEpochMilli(millis)
                                        .atZone(zone)
                                        .toLocalDate()

                                    val currentTime = Instant.ofEpochMilli(editedTimestamp)
                                        .atZone(zone)
                                        .toLocalTime()
                                
                                    editedTimestamp = selectedDate
                                        .atTime(currentTime)
                                        .atZone(zone)
                                        .toInstant()
                                        .toEpochMilli()
                                }
                                showDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                if (showTimePicker) {
                    val zone = ZoneId.systemDefault()

                    val initialTime = Instant.ofEpochMilli(editedTimestamp)
                        .atZone(zone)
                        .toLocalTime()

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
                                val currentDate = Instant.ofEpochMilli(editedTimestamp)
                                    .atZone(zone)
                                    .toLocalDate()

                                editedTimestamp = currentDate
                                    .atTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                                    .atZone(zone)
                                    .toInstant()
                                    .toEpochMilli()

                                showTimePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

            }


            if (showDeleteDialog && pendingDelete != null){
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        pendingDelete = null
                    },
                    title = { Text("Delete entry?") },
                    text = { Text("This measurement will be permanently removed.") },
                    confirmButton = {
                        TextButton(onClick = {
                                val deleted = pendingDelete

                                if (deleted != null) {
                                    viewModel.deleteMeasurement(deleted)

        
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Entry deleted",
                                            actionLabel = "UNDO",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.insertMeasurement(deleted)
                                        }
                                    }
                                }

                                showDeleteDialog = false
                                pendingDelete = null
                            }) { Text("Delete") }
 
                    }
                )
            }
        }
    }
    }
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export") },
            text = { Text("Choose a format") },
            confirmButton = {
                TextButton(onClick = { 
                    val uri = ExportUtil.exportCsv(context, measurements)                  
                    showExportDialog = false

                        scope.launch {
                            snackbarHostState.showSnackbar(
                            message = if (uri != null) 
                                "CSV exported to Downloads/BPLog" 
                            else 
                                "CSV export failed"
                            )
                        }
                }) { Text("CSV") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        val chart = chartRef
                        val bitmap = chart?.chartBitmap

                        val suffix = when (filter) {
                            HistoryFilter.DAYS_7 -> "7 days"
                            HistoryFilter.DAYS_30 -> "30 days"
                            HistoryFilter.ALL -> "All"
                        }

                        val exportedFileUri = if (bitmap != null) {
                            ExportUtil.exportPdf(context, bitmap, suffix, measurements)
                        } else null

                        showExportDialog = false

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = if (exportedFileUri != null) 
                                    "PDF exported to Downloads/BPLog" 
                                else 
                                    "PDF export failed"
                            )
                        }
                    }) { Text("PDF") }

                    TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
                }
            }
        )
    }

}

@Composable
private fun FilterChips(
    currentFilter: HistoryFilter,
    onFilterSelected: (HistoryFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentFilter == HistoryFilter.DAYS_7,
            onClick = { onFilterSelected(HistoryFilter.DAYS_7) },
            label = { Text("7 days") }
        )
        FilterChip(
            selected = currentFilter == HistoryFilter.DAYS_30,
            onClick = { onFilterSelected(HistoryFilter.DAYS_30) },
            label = { Text("30 days") }
        )
        FilterChip(
            selected = currentFilter == HistoryFilter.ALL,
            onClick = { onFilterSelected(HistoryFilter.ALL) },
            label = { Text("All") }
        )
    }
}

@Composable
private fun MeasurementItem(
    measurement: Measurement,
    onEdit: (Measurement) -> Unit,
    onDelete: (Measurement) -> Unit
    ) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(Date(measurement.timestamp)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${measurement.sys} / ${measurement.dia} mmHg  •  ${measurement.pulse} bpm",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row {
                IconButton(onClick = { onEdit(measurement) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDelete(measurement) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
