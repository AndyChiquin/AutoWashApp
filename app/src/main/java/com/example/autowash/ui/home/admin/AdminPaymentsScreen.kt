package com.example.autowash.ui.home.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AdminPaymentsScreen(
    modifier: Modifier = Modifier
) {

    val db = FirebaseFirestore.getInstance()

    var payments by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔹 Cargar TODOS los pagos
    LaunchedEffect(Unit) {
        db.collection("orders")
            .get()
            .addOnSuccessListener { snapshot ->
                payments = snapshot.documents.mapNotNull { doc ->
                    doc.data?.toMutableMap()?.apply {
                        put("orderId", doc.id)
                    }
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    when {
        isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        payments.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay pagos registrados")
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(payments) { order ->
                    AdminPaymentCard(order)
                }
            }
        }
    }
}

@Composable
fun AdminPaymentCard(order: Map<String, Any>) {

    val status = order["status"] as? String ?: ""
    val paymentMethod = order["paymentMethod"] as? String ?: "-"
    val transferCode = order["transferCode"] as? String

    val (statusText, statusColor) = when (status) {

        "PAYMENT_METHOD_SELECTED" ->
            "💵 Pendiente de pago" to Color(0xFFF9A825)

        "PAYMENT_PROOF_SUBMITTED" ->
            "🏦 Transferencia enviada" to Color(0xFF0288D1)

        "PAID" ->
            "✅ Pagado" to Color(0xFF2E7D32)

        else ->
            "Estado desconocido" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = order["serviceTitle"] as? String ?: "Servicio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Cliente ID: ${order["userId"] ?: "-"}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Proveedor ID: ${order["providerId"] ?: "-"}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Método de pago: $paymentMethod",
                style = MaterialTheme.typography.bodySmall
            )

            if (paymentMethod == "TRANSFER") {
                Text(
                    text = "Código transferencia: ${transferCode ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = statusText,
                color = statusColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
