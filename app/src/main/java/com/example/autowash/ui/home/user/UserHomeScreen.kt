package com.example.autowash.ui.home.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var selectedTab by remember { mutableIntStateOf(0) }

    var fullName by remember { mutableStateOf("Usuario") }
    var role by remember { mutableStateOf("Cliente") }

    // 🔹 ORDEN SELECCIONADA PARA PAGO
    var selectedOrderForPayment by remember {
        mutableStateOf<Map<String, Any>?>(null)
    }

    // 🔹 Cargar datos del usuario
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val firstName = document.getString("firstName") ?: ""
                val lastName = document.getString("lastName") ?: ""
                val roleDb = document.getString("role") ?: "USER"

                fullName = "$firstName $lastName".trim()

                role = when (roleDb) {
                    "ADMIN" -> "Administrador"
                    "PROVIDER" -> "Proveedor"
                    else -> "Cliente"
                }
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

        Column(modifier = Modifier.padding(padding)) {

            // 🔹 TABS
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Servicios") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Mis Reservas") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Perfil") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Métodos de Pago") }
                )
            }

            // 🔹 CONTENIDO
            Column(modifier = Modifier.padding(16.dp)) {
                when (selectedTab) {

                    // 🧼 SERVICIOS
                    0 -> ServicesSection()

                    // 📦 MIS RESERVAS
                    1 -> UserOrdersSection(
                        onSelectPaymentMethod = { order ->
                            selectedOrderForPayment = order
                            selectedTab = 3 // 👉 redirige a Métodos de Pago
                        }
                    )

                    // 👤 PERFIL
                    2 -> ProfileScreen()

                    // 💳 MÉTODOS DE PAGO (CONTEXTUAL)
                    3 -> {
                        if (selectedOrderForPayment != null) {
                            UserPaymentMethodScreen(
                                order = selectedOrderForPayment,
                                onBack = {
                                    selectedOrderForPayment = null
                                    selectedTab = 1
                                }
                            )
                        } else {
                            Text(
                                text = "Selecciona una reserva aceptada para definir el método de pago",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
