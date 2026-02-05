package com.example.autowash.ui.home.provider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceScreen(
    onCancel: () -> Unit,
    onServiceCreated: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }

    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Nuevo Servicio") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nombre del servicio") },
                singleLine = true,
                enabled = !isSaving
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                enabled = !isSaving
            )

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter { c -> c.isDigit() } },
                label = { Text("Duración (minutos)") },
                enabled = !isSaving
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Precio") },
                enabled = !isSaving
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Switch(
                    checked = active,
                    onCheckedChange = { active = it },
                    enabled = !isSaving
                )
                Text("Servicio activo")
            }

            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onCancel,
                    enabled = !isSaving
                ) {
                    Text("Cancelar")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isSaving,
                    onClick = {
                        if (uid == null) {
                            error = "Usuario no autenticado"
                            return@Button
                        }

                        if (title.isBlank() || duration.isBlank() || price.isBlank()) {
                            error = "Completa todos los campos obligatorios"
                            return@Button
                        }

                        isSaving = true
                        error = null

                        val data = hashMapOf(
                            "title" to title,
                            "description" to description,
                            "durationMinutes" to duration.toInt(),
                            "price" to price.toDouble(),
                            "active" to active,
                            "providerId" to uid,
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        db.collection("services")
                            .add(data)
                            .addOnSuccessListener {
                                onServiceCreated()
                            }
                            .addOnFailureListener {
                                error = "Error al guardar el servicio"
                                isSaving = false
                            }
                    }
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}
