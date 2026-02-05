package com.example.autowash.ui.home.admin

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

// 🔹 MODELO
data class AdminUserItem(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val active: Boolean
)

@Composable
fun AdminUsersScreen(
    modifier: Modifier = Modifier
) {

    val db = FirebaseFirestore.getInstance()

    var users by remember { mutableStateOf<List<AdminUserItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                users = snapshot.documents.mapNotNull { doc ->
                    val firstName = doc.getString("firstName") ?: return@mapNotNull null
                    val lastName = doc.getString("lastName") ?: ""
                    val email = doc.getString("email") ?: ""
                    val role = doc.getString("role") ?: "USER"
                    val active = doc.getBoolean("active") ?: false

                    AdminUserItem(
                        id = doc.id,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        role = role,
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
            text = "Usuarios",
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
                items(users) { user ->
                    AdminUserCard(user)
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: AdminUserItem
) {
    val db = FirebaseFirestore.getInstance()

    var isActive by remember { mutableStateOf(user.active) }
    var currentRole by remember { mutableStateOf(user.role) }
    var showRoleMenu by remember { mutableStateOf(false) }

    val roleLabel = when (currentRole) {
        "ADMIN" -> "Administrador"
        "PROVIDER" -> "Proveedor"
        else -> "Cliente"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // 🔹 Nombre + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = "${user.firstName} ${user.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (currentRole != "ADMIN") {
                    Switch(
                        checked = isActive,
                        onCheckedChange = { newValue ->
                            isActive = newValue
                            db.collection("users")
                                .document(user.id)
                                .update("active", newValue)
                        }
                    )
                }
            }

            // 🔹 Rol
            if (currentRole == "ADMIN") {
                Text(
                    text = "Rol: Administrador",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Column {
                    OutlinedButton(
                        onClick = { showRoleMenu = true }
                    ) {
                        Text("Rol: $roleLabel")
                    }

                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cliente") },
                            onClick = {
                                currentRole = "USER"
                                showRoleMenu = false
                                db.collection("users")
                                    .document(user.id)
                                    .update("role", "USER")
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Proveedor") },
                            onClick = {
                                currentRole = "PROVIDER"
                                showRoleMenu = false
                                db.collection("users")
                                    .document(user.id)
                                    .update("role", "PROVIDER")
                            }
                        )
                    }
                }
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
