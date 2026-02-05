package com.example.autowash.ui.home.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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


// 🔹 MODELO DE PROVEEDOR
data class AdminProviderItem(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val active: Boolean,
    val servicesCount: Int
)

@Composable
fun AdminProvidersScreen(
    modifier: Modifier = Modifier,
    onProviderClick: (AdminProviderItem) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    var providers by remember { mutableStateOf<List<AdminProviderItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔹 Cargar proveedores
    LaunchedEffect(Unit) {
        db.collection("users")
            .whereEqualTo("role", "PROVIDER")
            .get()
            .addOnSuccessListener { usersSnapshot ->

                val tempProviders = mutableListOf<AdminProviderItem>()

                usersSnapshot.documents.forEach { userDoc ->
                    val uid = userDoc.id
                    val firstName = userDoc.getString("firstName") ?: ""
                    val lastName = userDoc.getString("lastName") ?: ""
                    val email = userDoc.getString("email") ?: ""
                    val phone = userDoc.getString("phone") ?: ""
                    val active = userDoc.getBoolean("active") ?: false

                    // 🔹 Contar servicios del proveedor
                    db.collection("services")
                        .whereEqualTo("providerId", uid)
                        .get()
                        .addOnSuccessListener { servicesSnapshot ->
                            tempProviders.add(
                                AdminProviderItem(
                                    id = uid,
                                    fullName = "$firstName $lastName".trim(),
                                    email = email,
                                    phone = phone,
                                    active = active,
                                    servicesCount = servicesSnapshot.size()
                                )
                            )

                            providers = tempProviders.toList()
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
            text = "Proveedores",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(providers) { provider ->
                    ProviderCard(
                        provider = provider,
                        onClick = { onProviderClick(provider) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProviderCard(
    provider: AdminProviderItem,
    onClick: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var isActive by remember { mutableStateOf(provider.active) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                        text = provider.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = provider.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Teléfono: ${provider.phone}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Switch(
                    checked = isActive,
                    onCheckedChange = { newValue ->
                        isActive = newValue
                        db.collection("users")
                            .document(provider.id)
                            .update("active", newValue)
                    }
                )
            }

            Text(
                text = "Servicios: ${provider.servicesCount}",
                style = MaterialTheme.typography.bodySmall
            )

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
