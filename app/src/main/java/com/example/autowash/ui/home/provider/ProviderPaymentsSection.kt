package com.example.autowash.ui.home.provider

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun ProviderPaymentsSection() {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val providerId = auth.currentUser?.uid

    var orders by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        if (providerId == null) return@DisposableEffect onDispose {}

        val listener = db.collection("orders")
            .whereEqualTo("providerId", providerId)
            .whereIn(
                "status",
                listOf(
                    "PAYMENT_METHOD_SELECTED",
                    "PAYMENT_PROOF_SUBMITTED"
                )
            )
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    orders = snapshot.documents.mapNotNull { doc ->
                        doc.data?.toMutableMap()?.apply {
                            put("orderId", doc.id)
                        }
                    }
                    isLoading = false
                }
            }

        onDispose {
            listener.remove()
        }
    }

    when {
        isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        orders.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay pagos pendientes")
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    ProviderPaymentCard(
                        order = order,
                        onPaymentConfirmed = {
                            orders = orders.filterNot {
                                it["orderId"] == order["orderId"]
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ProviderPaymentCard(
    order: Map<String, Any>,
    onPaymentConfirmed: () -> Unit
) {

    val context = LocalContext.current
    val orderId = order["orderId"] as? String

    val paymentMethod = order["paymentMethod"] as? String
    val transferCode = order["transferCode"] as? String

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = order["serviceTitle"] as? String ?: "Servicio",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(6.dp))

            Text("Fecha: ${order["serviceDate"] ?: "-"}")
            Text("Hora: ${order["serviceTime"] ?: "-"}")

            Spacer(Modifier.height(10.dp))

            // =====================
            // 💳 MÉTODO DE PAGO
            // =====================
            when (paymentMethod) {

                "CASH" -> {
                    Text(
                        text = "💵 Pago pendiente (efectivo)",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                "TRANSFER" -> {
                    Text(
                        text = "🏦 Transferencia realizada",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Código de referencia:",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = transferCode ?: "—",
                        fontWeight = FontWeight.Bold
                    )
                }

                else -> {
                    Text("Método de pago no definido")
                }
            }

            Spacer(Modifier.height(12.dp))

            // =====================
            // ✅ CONFIRMAR PAGO
            // =====================
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (orderId == null) return@Button

                    FirebaseFirestore.getInstance()
                        .collection("orders")
                        .document(orderId)
                        .update("status", "PAID")
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Pago confirmado",
                                Toast.LENGTH_SHORT
                            ).show()
                            onPaymentConfirmed()
                        }
                }
            ) {
                Text("Confirmar pago recibido")
            }
        }
    }
}
