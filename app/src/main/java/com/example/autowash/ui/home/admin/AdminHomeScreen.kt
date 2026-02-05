package com.example.autowash.ui.home.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


// 🔹 Secciones internas del Admin (SIN Navigation Component)
enum class AdminSection {
    DASHBOARD,
    USERS,
    PROVIDERS,
    PROVIDER_SERVICES,
    SERVICES,
    PAYMENTS

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var fullName by remember { mutableStateOf("Administrador") }
    var role by remember { mutableStateOf("Administrador") }

    var selectedProviderId by remember { mutableStateOf<String?>(null) }
    var selectedProviderName by remember { mutableStateOf("") }


    var currentSection by remember { mutableStateOf(AdminSection.DASHBOARD) }

    // 🔹 Cargar datos del ADMIN
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val firstName = document.getString("firstName") ?: ""
                val lastName = document.getString("lastName") ?: ""
                fullName = "$firstName $lastName".trim()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lavado Express",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (currentSection != AdminSection.DASHBOARD) {
                        IconButton(onClick = {
                            currentSection = AdminSection.DASHBOARD
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = fullName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = role,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                auth.signOut()
                                onLogout()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Salir",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salir")
                        }
                    }
                }
            )
        }
    ) { padding ->

        when (currentSection) {

            // 🔹 DASHBOARD
            AdminSection.DASHBOARD -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = "Panel de Administración",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Selecciona una opción para gestionar la aplicación",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AdminDashboardButton(
                        title = "Usuarios",
                        description = "Gestionar usuarios y roles",
                        onClick = {
                            currentSection = AdminSection.USERS
                        }
                    )

                    AdminDashboardButton(
                        title = "Proveedores",
                        description = "Administrar proveedores",
                        onClick = {
                            currentSection = AdminSection.PROVIDERS
                        }
                    )


                    AdminDashboardButton(
                        title = "Servicios",
                        description = "Gestionar todos los servicios",
                        onClick = {
                            currentSection = AdminSection.SERVICES
                        }
                    )

                    AdminDashboardButton(
                        title = "Pagos",
                        description = "Ver y auditar pagos",
                        onClick = {
                            currentSection = AdminSection.PAYMENTS
                        }
                    )
                }
            }

            // 🔹 USUARIOS
            AdminSection.USERS -> {
                AdminUsersScreen(
                    modifier = Modifier.padding(padding)
                )
            }

            AdminSection.PROVIDERS -> {
                AdminProvidersScreen(
                    modifier = Modifier.padding(padding),
                    onProviderClick = { provider ->
                        selectedProviderId = provider.id
                        selectedProviderName = provider.fullName
                        currentSection = AdminSection.PROVIDER_SERVICES
                    }
                )
            }

            AdminSection.PROVIDER_SERVICES -> {
                AdminProviderServicesScreen(
                    providerId = selectedProviderId ?: return@Scaffold,
                    providerName = selectedProviderName,
                    modifier = Modifier.padding(padding)
                )
            }

            AdminSection.SERVICES -> {
                AdminServicesScreen(
                    modifier = Modifier.padding(padding)
                )
            }

            AdminSection.PAYMENTS -> {
                AdminPaymentsScreen(
                    modifier = Modifier.padding(padding)
                )
            }


        }
    }
}

@Composable
fun AdminDashboardButton(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
