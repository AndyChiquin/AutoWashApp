package com.example.autowash.ui.home.provider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocalCarWash
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.autowash.ui.home.user.models.ServiceUIModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderHomeScreen(
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid

    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateService by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("Proveedor") }
    var role by remember { mutableStateOf("Proveedor") }

    var services by remember { mutableStateOf<List<ServiceUIModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadServices() {
        if (uid == null) return
        isLoading = true

        db.collection("services")
            .whereEqualTo("providerId", uid)
            .get()
            .addOnSuccessListener { result ->
                services = result.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val description = doc.getString("description") ?: ""
                    val durationMinutes = doc.getLong("durationMinutes") ?: 0
                    val priceValue = doc.getDouble("price") ?: 0.0
                    val active = doc.getBoolean("active") ?: true
                    val providerId = doc.getString("providerId") ?: ""


                    ServiceUIModel(
                        id = doc.id,
                        title = title,
                        description = description,
                        duration = "${durationMinutes} min",
                        price = "$${String.format("%.2f", priceValue)}",
                        icon = Icons.Default.LocalCarWash,
                        active = active,
                        providerId = providerId

                    )

                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    LaunchedEffect(Unit) {
        if (uid == null) return@LaunchedEffect

        db.collection("users").document(uid).get()
            .addOnSuccessListener {
                val first = it.getString("firstName") ?: ""
                val last = it.getString("lastName") ?: ""
                fullName = "$first $last".trim()
            }

        loadServices()
    }

    if (showCreateService) {
        CreateServiceScreen(
            onCancel = { showCreateService = false },
            onServiceCreated = {
                showCreateService = false
                loadServices()
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Proveedor", fontWeight = FontWeight.Bold) },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(fullName, fontWeight = FontWeight.Bold)
                            Text(role, style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedButton(
                            onClick = {
                                auth.signOut()
                                onLogout()
                            }
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Salir")
                        }
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selectedTab == 0, { selectedTab = 0 }) { Text("Mis Servicios") }
                Tab(selectedTab == 1, { selectedTab = 1 }) { Text("Reservas") }
                Tab(selectedTab == 2, { selectedTab = 2 }) { Text("Pagos") }
                Tab(selectedTab == 3, { selectedTab = 3 }) { Text("Perfil") }
            }


            when (selectedTab) {

                0 -> {
                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            item {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Mis Servicios",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Button(
                                        onClick = { showCreateService = true },
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("+ Nuevo Servicio")
                                    }
                                }
                            }

                            if (services.isEmpty()) {
                                item {
                                    Text("Aún no tienes servicios creados")
                                }
                            } else {
                                items(services) { service ->
                                    ProviderServiceCard(
                                        serviceId = service.id,
                                        service = service,
                                        isActive = service.active
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Reservas
                    ProviderOrdersSection()
                }

                2 -> {
                    // Pagos
                    ProviderPaymentsSection()
                }

                3 -> {
                    // Perfil
                    ProviderProfileScreen()
                }
            }

        }
    }
}
