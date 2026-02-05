package com.example.autowash.ui.home.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun UserPaymentMethodScreen(
    order: Map<String, Any>?,
    onBack: () -> Unit
) {
    if (order == null) return

    val db = FirebaseFirestore.getInstance()
    val orderId = order["orderId"] as? String ?: return
    val providerId = order["providerId"] as? String ?: return

    var isSaving by remember { mutableStateOf(false) }

    var showCashConfirmDialog by remember { mutableStateOf(false) }
    var showTransferConfirmDialog by remember { mutableStateOf(false) }
    var isTransferConfirmed by remember { mutableStateOf(false) }

    var bankInfo by remember { mutableStateOf<Map<String, String>?>(null) }
    var transferCode by remember { mutableStateOf("") }
    var loadError by remember { mutableStateOf<String?>(null) }

    // ⚠️ WARNING GLOBAL
    var showWarningDialog by remember { mutableStateOf(false) }
    var warningMessage by remember { mutableStateOf("") }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    var navigateBackAfterWarning by remember { mutableStateOf(false) }

    // 🔹 Cargar cuenta bancaria del proveedor
    LaunchedEffect(providerId) {
        db.collection("users")
            .document(providerId)
            .get()
            .addOnSuccessListener { doc ->
                val bank = doc.data?.get("bankInfo") as? Map<String, String>
                if (bank != null) {
                    bankInfo = bank
                } else {
                    loadError = "El proveedor no ha registrado su cuenta bancaria"
                }
            }
            .addOnFailureListener {
                loadError = "Error al cargar datos bancarios"
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Selecciona el método de pago",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("Servicio: ${order["serviceTitle"] ?: ""}")

        // =====================
        // 💵 EFECTIVO
        // =====================
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {

                Text("💵 Efectivo", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    onClick = { showCashConfirmDialog = true }
                ) {
                    Text("Pagar en efectivo")
                }
            }
        }

        // =====================
        // 🏦 TRANSFERENCIA
        // =====================
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text("🏦 Transferencia", style = MaterialTheme.typography.titleMedium)

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    onClick = { showTransferConfirmDialog = true }
                ) {
                    Text("Pagar por transferencia")
                }

                if (showTransferConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showTransferConfirmDialog = false },
                        title = { Text("Confirmar método de pago") },
                        text = {
                            Text(
                                "¿Estás seguro de pagar por transferencia?\n\n" +
                                        "Se mostrará la cuenta bancaria del proveedor."
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showTransferConfirmDialog = false
                                    isTransferConfirmed = true
                                }
                            ) {
                                Text("Sí, continuar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showTransferConfirmDialog = false
                            }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                if (isTransferConfirmed) {

                    Divider()

                    when {
                        loadError != null -> {
                            Text(loadError!!, color = MaterialTheme.colorScheme.error)
                        }

                        bankInfo == null -> {
                            CircularProgressIndicator()
                        }

                        else -> {
                            Text("Realiza la transferencia a:", fontWeight = FontWeight.Bold)
                            Text("Banco: ${bankInfo!!["bankName"]}")
                            Text("Cuenta: ${bankInfo!!["accountNumber"]}")
                            Text("Tipo: ${bankInfo!!["accountType"]}")
                            Text("Titular: ${bankInfo!!["accountOwner"]}")

                            OutlinedTextField(
                                value = transferCode,
                                onValueChange = { transferCode = it },
                                label = { Text("Código / referencia") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = transferCode.isNotBlank() && !isSaving,
                                onClick = {

                                    warningMessage =
                                        "⚠️ Importante:\n\n" +
                                                "Si no realizas correctamente el pago por transferencia, " +
                                                "el proveedor no está obligado a prestar el servicio y " +
                                                "tu cuenta podría ser suspendida."

                                    pendingAction = {
                                        isSaving = true
                                        db.collection("orders")
                                            .document(orderId)
                                            .update(
                                                mapOf(
                                                    "paymentMethod" to "TRANSFER",
                                                    "paymentMethodLocked" to true,
                                                    "transferCode" to transferCode,
                                                    "status" to "PAYMENT_PROOF_SUBMITTED"
                                                )
                                            )
                                            .addOnSuccessListener {
                                                isSaving = false
                                            }
                                            .addOnFailureListener {
                                                isSaving = false
                                            }
                                    }

                                    showWarningDialog = true
                                }
                            ) {
                                Text("Confirmar transferencia")
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        ) {
            Text("Volver")
        }
    }

    // =====================
    // CONFIRMACIÓN EFECTIVO
    // =====================
    if (showCashConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCashConfirmDialog = false },
            title = { Text("Confirmar pago") },
            text = {
                Text("Pagarás en efectivo al finalizar el servicio.")
            },
            confirmButton = {
                Button(
                    onClick = {

                        warningMessage =
                            "⚠️ Importante:\n\n" +
                                    "Si no realizas el pago en efectivo, el proveedor no está " +
                                    "obligado a prestar el servicio y tu cuenta podría ser suspendida."

                        pendingAction = {
                            isSaving = true
                            db.collection("orders")
                                .document(orderId)
                                .update(
                                    mapOf(
                                        "paymentMethod" to "CASH",
                                        "paymentMethodLocked" to true,
                                        "status" to "PAYMENT_METHOD_SELECTED"
                                    )
                                )
                                .addOnSuccessListener {
                                    isSaving = false
                                }
                                .addOnFailureListener {
                                    isSaving = false
                                }
                        }

                        showCashConfirmDialog = false
                        showWarningDialog = true
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCashConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // =====================
    // ⚠️ WARNING GLOBAL
    // =====================
    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            title = { Text("Aviso importante") },
            text = { Text(warningMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showWarningDialog = false
                        pendingAction?.invoke()
                        pendingAction = null
                        navigateBackAfterWarning = true
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    // =====================
    // 🔁 NAVEGACIÓN FINAL
    // =====================
    LaunchedEffect(navigateBackAfterWarning) {
        if (navigateBackAfterWarning) {
            onBack()
            navigateBackAfterWarning = false
        }
    }
}
