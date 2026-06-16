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
import chimu.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
                    value = "${dateTime.day.toString().padStart(2, '0')}.${dateTime.month.number.toString().padStart(2, '0')}.${dateTime.year}",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.date)) },
                    trailingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = stringResource(Res.string.choose_date))
                    },
                    shape = MaterialTheme.shapes.largeIncreased
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
                    label = { Text(stringResource(Res.string.time)) },
                    trailingIcon = {
                        Icon(Icons.Default.Schedule, contentDescription = stringResource(Res.string.choose_time))
                    },
                    shape = MaterialTheme.shapes.largeIncreased
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
                        val selectedDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC)
                        val newDateTime = LocalDateTime(
                            selectedDate.year,
                            selectedDate.month,
                            selectedDate.day,
                            dateTime.hour,
                            dateTime.minute
                        )
                        onValueChange(newDateTime.toInstant(TimeZone.currentSystemDefault()).toString())
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.cancel))
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
                        dateTime.day,
                        timeState.hour,
                        timeState.minute
                    )
                    onValueChange(newDateTime.toInstant(TimeZone.currentSystemDefault()).toString())
                    showTimePicker = false
                }) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(Res.string.cancel))
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
