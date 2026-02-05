package com.example.autowash.ui.home.provider

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import com.example.autowash.ui.home.user.UserOrdersDonutChart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun ProviderStatsScreen() {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var isLoading by remember { mutableStateOf(true) }

    // 📦 MÉTRICAS DE ÓRDENES
    var totalOrders by remember { mutableStateOf(0) }
    var paidOrders by remember { mutableStateOf(0) }
    var pendingPayments by remember { mutableStateOf(0) }
    var inProcessOrders by remember { mutableStateOf(0) }

    // 📈 MÉTRICAS DE DESEMPEÑO
    var paymentRate by remember { mutableStateOf(0) }
    var providerLevel by remember { mutableStateOf("Calculando...") }

    // 🧼 MÉTRICAS DE SERVICIOS
    var totalServices by remember { mutableStateOf(0) }
    var activeServices by remember { mutableStateOf(0) }
    var inactiveServices by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val providerId = auth.currentUser?.uid ?: return@LaunchedEffect

        // 🔹 CARGAR ÓRDENES DEL PROVIDER
        db.collection("orders")
            .whereEqualTo("providerId", providerId)
            .get()
            .addOnSuccessListener { result ->

                val orders = result.documents.mapNotNull { it.data }

                totalOrders = orders.size

                paidOrders = orders.count { it["status"] == "PAID" }

                pendingPayments = orders.count {
                    it["status"] == "PAYMENT_METHOD_SELECTED" ||
                            it["status"] == "PAYMENT_PROOF_SUBMITTED"
                }

                inProcessOrders = orders.count {
                    it["status"] == "REQUESTED" ||
                            it["status"] == "ACCEPTED"
                }

                val acceptedOrders = paidOrders + pendingPayments
                paymentRate =
                    if (acceptedOrders == 0) 0
                    else (paidOrders * 100) / acceptedOrders

                providerLevel = when {
                    paymentRate >= 85 -> "🟢 Proveedor excelente"
                    paymentRate >= 60 -> "🟡 Buen proveedor"
                    else -> "🔴 Proveedor en riesgo"
                }

                // 🔹 CARGAR SERVICIOS DEL PROVIDER
                db.collection("services")
                    .whereEqualTo("providerId", providerId)
                    .get()
                    .addOnSuccessListener { servicesResult ->
                        val services = servicesResult.documents

                        totalServices = services.size
                        activeServices = services.count { it.getBoolean("active") == true }
                        inactiveServices = services.count { it.getBoolean("active") == false }

                        isLoading = false
                    }
                    .addOnFailureListener {
                        isLoading = false
                    }
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

        totalOrders == 0 && totalServices == 0 -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no tienes actividad registrada como proveedor",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {


                // 📊 DESEMPEÑO DEL PROVIDER
                ProviderStatsCard(
                    totalOrders = totalOrders,
                    paidOrders = paidOrders,
                    pendingPayments = pendingPayments,
                    paymentRate = paymentRate,
                    providerLevel = providerLevel
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🧼 ESTADO DE SERVICIOS
                ProviderServicesStatusCard(
                    totalServices = totalServices,
                    activeServices = activeServices,
                    inactiveServices = inactiveServices
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ⭕ ESTADO DE RESERVAS
                UserOrdersDonutChart(
                    paid = paidOrders,
                    pending = pendingPayments,
                    inProcess = inProcessOrders
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Estas métricas reflejan tu desempeño y disponibilidad como proveedor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
