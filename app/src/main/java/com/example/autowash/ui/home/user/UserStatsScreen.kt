package com.example.autowash.ui.home.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun UserStatsScreen() {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var isLoading by remember { mutableStateOf(true) }
    var totalOrders by remember { mutableStateOf(0) }
    var paidOrders by remember { mutableStateOf(0) }
    var pendingPayments by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect

        db.collection("orders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->

                val orders = result.documents.mapNotNull { it.data }

                totalOrders = orders.size

                paidOrders = orders.count {
                    it["status"] == "PAID"
                }

                pendingPayments = orders.count {
                    it["status"] == "PAYMENT_METHOD_SELECTED" ||
                            it["status"] == "PAYMENT_PROOF_SUBMITTED"
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
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        totalOrders == 0 -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aún no tienes estadísticas porque no has realizado reservas",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                // ✅ 1️⃣ CUADRO DE MÉTRICAS (ARRIBA)
                UserStatsCard(
                    totalOrders = totalOrders,
                    paidOrders = paidOrders,
                    pendingPayments = pendingPayments
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ✅ 2️⃣ DONUT (DEBAJO DEL CUADRO)
                UserOrdersDonutChart(
                    paid = paidOrders,
                    pending = pendingPayments,
                    inProcess = totalOrders - paidOrders - pendingPayments
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ℹ️ TEXTO FINAL
                Text(
                    "Estas métricas se calculan en base a tu historial de uso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

    }
}
