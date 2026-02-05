package com.example.autowash.ui.home.user

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun UserOrdersSection(
    onSelectPaymentMethod: (Map<String, Any>) -> Unit,
    onStatsCalculated: (
        totalOrders: Int,
        paidOrders: Int,
        pendingPayments: Int
    ) -> Unit = { _, _, _ -> }
) {


    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var orders by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect

        db.collection("orders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val loadedOrders = result.documents.map { doc ->
                    val data = doc.data ?: emptyMap()
                    data + mapOf("orderId" to doc.id)
                }

                orders = loadedOrders
                isLoading = false

                // 📊 CALCULAR MÉTRICAS
                val totalOrders = loadedOrders.size

                val paidOrders = loadedOrders.count {
                    it["status"] == "PAID"
                }

                val pendingPayments = loadedOrders.count {
                    it["status"] == "PAYMENT_METHOD_SELECTED" ||
                            it["status"] == "PAYMENT_PROOF_SUBMITTED"
                }

                // 🔁 ENVIAR MÉTRICAS AL HOME
                onStatsCalculated(
                    totalOrders,
                    paidOrders,
                    pendingPayments
                )
            }

            .addOnFailureListener {
                isLoading = false
            }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        orders.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No tienes reservas todavía")
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(orders) { order ->
                    UserOrderCard(
                        order = order,
                        onSelectPaymentMethod = onSelectPaymentMethod
                    )
                }

            }
        }
    }
}

@Composable
fun UserOrderCard(
    order: Map<String, Any>,
    onSelectPaymentMethod: (Map<String, Any>) -> Unit
) {


    val status = order["status"] as? String ?: "REQUESTED"

    val (statusText, statusColor) = when (status) {

        "REQUESTED" ->
            "⏳ Esperando aceptación del proveedor" to Color.Gray

        "ACCEPTED" ->
            "✅ Servicio aceptado · Selecciona método de pago" to Color(0xFF2E7D32)

        "PAYMENT_METHOD_SELECTED" ->
            "💵 Pago pendiente" to Color(0xFFF9A825)

        "PAYMENT_PROOF_SUBMITTED" ->
            "🕒 Pago en validación" to Color(0xFF0288D1)

        "PAID" ->
            "🎉 Servicio pagado" to Color(0xFF2E7D32)

        "REJECTED" ->
            "❌ Servicio rechazado" to Color(0xFFC62828)

        else ->
            "Estado desconocido" to Color.Gray
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = order["serviceTitle"] as? String ?: "Servicio",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Fecha: ${order["serviceDate"] ?: "-"}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Hora: ${order["serviceTime"] ?: "-"}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor
            )

            Text(
                text = "OrderId: ${order["orderId"]}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


            // 🔹 BOTÓN SOLO CUANDO ESTÁ ACEPTADO
            if (status == "ACCEPTED") {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onSelectPaymentMethod(order)
                    }
                ) {
                    Text("Seleccionar método de pago")
                }
            }
        }
    }
}
