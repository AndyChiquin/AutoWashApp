package com.example.autowash.ui.home.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

data class AdminServiceItem(
    val id: String,
    val title: String,
    val price: Double,
    val durationMinutes: Int,
    val active: Boolean,
    val providerName: String
)

@Composable
fun AdminServicesScreen(
    modifier: Modifier = Modifier
) {
    val db = FirebaseFirestore.getInstance()

    var services by remember { mutableStateOf<List<AdminServiceItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("services")
            .get()
            .addOnSuccessListener { servicesSnapshot ->

                val temp = mutableListOf<AdminServiceItem>()

                if (servicesSnapshot.isEmpty) {
                    services = emptyList()
                    isLoading = false
                    return@addOnSuccessListener
                }

                servicesSnapshot.documents.forEach { serviceDoc ->
                    val serviceId = serviceDoc.id
                    val title = serviceDoc.getString("title") ?: return@forEach
                    val price = serviceDoc.getDouble("price") ?: 0.0
                    val duration = serviceDoc.getLong("durationMinutes")?.toInt() ?: 0
                    val active = serviceDoc.getBoolean("active") ?: false
                    val providerId = serviceDoc.getString("providerId")

                    // 🔒 VALIDACIÓN CRÍTICA
                    if (providerId.isNullOrBlank()) {
                        temp.add(
                            AdminServiceItem(
                                id = serviceId,
                                title = title,
                                price = price,
                                durationMinutes = duration,
                                active = active,
                                providerName = "Proveedor desconocido"
                            )
                        )
                        services = temp.toList()
                        isLoading = false
                        return@forEach
                    }

                    db.collection("users")
                        .document(providerId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val firstName = userDoc.getString("firstName") ?: ""
                            val lastName = userDoc.getString("lastName") ?: ""

                            temp.add(
                                AdminServiceItem(
                                    id = serviceId,
                                    title = title,
                                    price = price,
                                    durationMinutes = duration,
                                    active = active,
                                    providerName = "$firstName $lastName".trim()
                                )
                            )

                            services = temp.toList()
                            isLoading = false
                        }
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Servicios (Global)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.padding(8.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(services) { service ->
                    AdminServiceCard(service)
                }
            }
        }
    }
}

@Composable
fun AdminServiceCard(
    service: AdminServiceItem
) {
    val db = FirebaseFirestore.getInstance()
    var isActive by remember { mutableStateOf(service.active) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = service.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Proveedor: ${service.providerName}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "Precio: $${service.price}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "Duración: ${service.durationMinutes} min",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Switch(
                    checked = isActive,
                    onCheckedChange = { newValue ->
                        isActive = newValue
                        db.collection("services")
                            .document(service.id)
                            .update("active", newValue)
                    }
                )
            }

            Text(
                text = if (isActive) "Estado: Activo" else "Estado: Inactivo",
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}
