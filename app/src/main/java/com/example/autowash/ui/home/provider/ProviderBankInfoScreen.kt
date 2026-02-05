package com.example.autowash.ui.home.provider

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProviderBankInfoScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val providerId = auth.currentUser?.uid ?: return

    var bankName by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var accountOwner by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Cuenta bancaria",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Esta información será mostrada al cliente para pagos por transferencia.",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            value = bankName,
            onValueChange = { bankName = it },
            label = { Text("Banco") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = accountType,
            onValueChange = { accountType = it },
            label = { Text("Tipo de cuenta (Ahorros / Corriente)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = accountNumber,
            onValueChange = { accountNumber = it },
            label = { Text("Número de cuenta") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = accountOwner,
            onValueChange = { accountOwner = it },
            label = { Text("Titular de la cuenta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving,
            onClick = {
                if (
                    bankName.isBlank() ||
                    accountType.isBlank() ||
                    accountNumber.isBlank() ||
                    accountOwner.isBlank()
                ) {
                    Toast.makeText(
                        context,
                        "Completa todos los campos",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                isSaving = true

                val bankInfo = mapOf(
                    "bankName" to bankName,
                    "accountType" to accountType,
                    "accountNumber" to accountNumber,
                    "accountOwner" to accountOwner
                )

                db.collection("users")
                    .document(providerId)
                    .update("bankInfo", bankInfo)
                    .addOnSuccessListener {
                        isSaving = false
                        Toast.makeText(
                            context,
                            "Cuenta bancaria guardada",
                            Toast.LENGTH_SHORT
                        ).show()
                        onBack()
                    }
                    .addOnFailureListener {
                        isSaving = false
                        Toast.makeText(
                            context,
                            "Error al guardar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        ) {
            Text("Guardar")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack,
            enabled = !isSaving
        ) {
            Text("Volver")
        }
    }
}
