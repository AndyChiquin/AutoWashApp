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


data class AdminProviderServiceItem(
    val id: String,
    val title: String,
    val price: Double,
    val durationMinutes: Int,
    val active: Boolean
)

@Composable
fun AdminProviderServicesScreen(
    providerId: String,
    providerName: String,
    modifier: Modifier = Modifier
) {
    val db = FirebaseFirestore.getInstance()

    var services by remember { mutableStateOf<List<AdminProviderServiceItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(providerId) {
        db.collection("services")
            .whereEqualTo("providerId", providerId)
            .get()
            .addOnSuccessListener { snapshot ->
                services = snapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val price = doc.getDouble("price") ?: 0.0
                    val duration = doc.getLong("durationMinutes")?.toInt() ?: 0
                    val active = doc.getBoolean("active") ?: false

                    AdminProviderServiceItem(
                        id = doc.id,
                        title = title,
                        price = price,
                        durationMinutes = duration,
                        active = active
                    )
                }
                isLoading = false
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Servicios de $providerName",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.padding(8.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (services.isEmpty()) {
            Text(
                text = "Este proveedor no tiene servicios registrados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(services) { service ->
                    ProviderServiceCard(service)
                }
            }
        }
    }
}

@Composable
fun ProviderServiceCard(
    service: AdminProviderServiceItem
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        text = "Precio: $${service.price}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "Duración: ${service.durationMinutes} min",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // 🔹 ACTIVAR / DESACTIVAR SERVICIO
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
