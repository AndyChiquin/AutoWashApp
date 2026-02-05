package com.example.autowash.ui.home.user

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.autowash.ui.home.user.models.ServiceUIModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ServicesSection() {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val userId = auth.currentUser?.uid

    var services by remember { mutableStateOf<List<ServiceUIModel>>(emptyList()) }
    var reservedServiceIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedService by remember { mutableStateOf<ServiceUIModel?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // 🔹 Cargar servicios + reservas del usuario
    LaunchedEffect(Unit) {
        if (userId == null) return@LaunchedEffect

        // 1️⃣ Obtener servicios activos
        db.collection("services")
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { serviceResult ->

                val loadedServices = serviceResult.documents.mapNotNull { doc ->
                    ServiceUIModel(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        duration = "${doc.getLong("durationMinutes") ?: 0} min",
                        price = "$${doc.getDouble("price") ?: 0.0}",
                        icon = Icons.Default.DirectionsCar,
                        active = doc.getBoolean("active") ?: true,
                        providerId = doc.getString("providerId") ?: ""
                    )
                }

                // 2️⃣ Obtener órdenes del usuario
                db.collection("orders")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { orderResult ->
                        reservedServiceIds = orderResult.documents
                            .mapNotNull { it.getString("serviceId") }
                            .toSet()

                        services = loadedServices
                        isLoading = false
                    }
                    .addOnFailureListener {
                        services = loadedServices
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

        services.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay servicios disponibles")
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(services) { service ->

                    val isReserved = reservedServiceIds.contains(service.id)

                    ServiceCard(
                        service = service,
                        isReserved = isReserved,
                        onReserveClick = { selected ->
                            if (isReserved) {
                                Toast
                                    .makeText(
                                        context,
                                        "Este servicio ya fue reservado",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            } else {
                                selectedService = selected
                                showConfirmDialog = true
                            }
                        }
                    )
                }
            }
        }
    }

    // 🔹 Confirmar reserva
    if (showConfirmDialog && selectedService != null) {
        ConfirmOrderDialog(
            service = selectedService!!,
            onDismiss = {
                showConfirmDialog = false
                selectedService = null
            },
            onConfirm = { date, time ->

                val service = selectedService ?: return@ConfirmOrderDialog

                val priceValue = service.price.replace("$", "").toDoubleOrNull() ?: 0.0
                val durationValue = service.duration.replace(" min", "").toIntOrNull() ?: 0

                val order = hashMapOf(
                    "userId" to userId,
                    "providerId" to service.providerId,
                    "serviceId" to service.id,
                    "serviceTitle" to service.title,
                    "price" to priceValue,
                    "durationMinutes" to durationValue,
                    "serviceDate" to date,
                    "serviceTime" to time,
                    "status" to "REQUESTED",
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection("orders")
                    .add(order)
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Reserva enviada correctamente",
                            Toast.LENGTH_LONG
                        ).show()

                        // 🔒 bloquear servicio inmediatamente
                        reservedServiceIds = reservedServiceIds + service.id
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Error al enviar la reserva",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                showConfirmDialog = false
                selectedService = null
            }
        )
    }
}
