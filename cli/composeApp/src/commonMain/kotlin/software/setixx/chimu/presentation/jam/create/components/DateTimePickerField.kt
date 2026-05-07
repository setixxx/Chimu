package software.setixx.chimu.presentation.jam.create.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateTime = remember(value) {
        try {
            if (value.isBlank()) {
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            } else {
                Instant.parse(value).toLocalDateTime(TimeZone.currentSystemDefault())
            }
        } catch (e: Exception) {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }

    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    )
    val timeState = rememberTimePickerState(
        initialHour = dateTime.hour,
        initialMinute = dateTime.minute,
        is24Hour = true
    )

    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = "${dateTime.year}-${dateTime.monthNumber.toString().padStart(2, '0')}-${dateTime.dayOfMonth.toString().padStart(2, '0')}",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Дата") },
                    trailingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Выбрать дату")
                    }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            Box(modifier = Modifier.weight(0.7f)) {
                OutlinedTextField(
                    value = "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Время") },
                    trailingIcon = {
                        Icon(Icons.Default.Schedule, contentDescription = "Выбрать время")
                    }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showTimePicker = true }
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { millis ->
                        val selectedDate = kotlinx.datetime.Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC)
                        val newDateTime = LocalDateTime(
                            selectedDate.year,
                            selectedDate.month,
                            selectedDate.dayOfMonth,
                            dateTime.hour,
                            dateTime.minute
                        )
                        onValueChange(newDateTime.toInstant(TimeZone.currentSystemDefault()).toString())
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newDateTime = LocalDateTime(
                        dateTime.year,
                        dateTime.month,
                        dateTime.dayOfMonth,
                        timeState.hour,
                        timeState.minute
                    )
                    onValueChange(newDateTime.toInstant(TimeZone.currentSystemDefault()).toString())
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Отмена")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timeState)
                }
            }
        )
    }
}
