package com.example.autowash.ui.home.user

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.autowash.ui.home.user.models.ServiceUIModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmOrderDialog(
    service: ServiceUIModel,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }

    // ───── DatePicker ─────
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= calendar.timeInMillis
            }
        }
    )

    val showDatePicker = remember { mutableStateOf(false) }

    if (showDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker.value = false
                    datePickerState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        selectedDate = sdf.format(Date(it))
                    }
                }) {
                    Text("Aceptar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ───── TimePicker ─────
    fun openTimePicker() {
        val now = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    // ───── UI ─────
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Confirmar reserva") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text(service.title, style = MaterialTheme.typography.titleMedium)
                Text(service.description, style = MaterialTheme.typography.bodySmall)

                Divider()

                OutlinedButton(
                    onClick = { showDatePicker.value = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (selectedDate.isEmpty())
                            "Seleccionar fecha"
                        else
                            "Fecha: $selectedDate"
                    )
                }

                OutlinedButton(
                    onClick = { openTimePicker() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (selectedTime.isEmpty())
                            "Seleccionar hora"
                        else
                            "Hora: $selectedTime"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedDate.isNotEmpty() && selectedTime.isNotEmpty(),
                onClick = {
                    onConfirm(selectedDate, selectedTime)
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
