package com.example.autowash.ui.register

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // LOGO
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Crear cuenta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Regístrate para usar AutoWash",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { newValue ->
                        if (
                            newValue.length <= 20 &&
                            newValue.all { it.isLetter() || it.isWhitespace() }
                        ) {
                            firstName = newValue
                        }
                    },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )


                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { newValue ->
                        if (
                            newValue.length <= 20 &&
                            newValue.all { it.isLetter() || it.isWhitespace() }
                        ) {
                            lastName = newValue
                        }
                    },
                    label = { Text("Apellido") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )


                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 10) {
                            phone = newValue
                        }
                    },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )


                Spacer(modifier = Modifier.height(28.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null)
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar contraseña") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null)
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        val cleanEmail = email.trim()
                        val cleanPassword = password.trim()
                        val cleanConfirm = confirmPassword.trim()

                        if (firstName.length < 2 || firstName.length > 20) {
                            Toast.makeText(context, "Nombre no válido", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (lastName.length < 2 || lastName.length > 20) {
                            Toast.makeText(context, "Apellido no válido", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (phone.length != 10) {
                            Toast.makeText(
                                context,
                                "El teléfono debe tener 10 dígitos",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Correo y contraseña requeridos",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }



                        if (cleanPassword.length < 6) {
                            Toast.makeText(context, "Mínimo 6 caracteres", Toast.LENGTH_SHORT)
                                .show()
                            return@Button
                        }

                        if (cleanPassword != cleanConfirm) {
                            Toast.makeText(
                                context,
                                "Las contraseñas no coinciden",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        isLoading = true

                        auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    val uid = auth.currentUser?.uid
                                        ?: return@addOnCompleteListener

                                    val userData = hashMapOf(
                                        "firstName" to firstName.trim(),
                                        "lastName" to lastName.trim(),
                                        "phone" to phone.trim(),
                                        "email" to cleanEmail,
                                        "role" to "USER",
                                        "active" to true,
                                        "createdAt" to System.currentTimeMillis()
                                    )



                                    db.collection("users")
                                        .document(uid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                "Usuario registrado correctamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onRegisterSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                "Error Firestore: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                } else {
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        task.exception?.message ?: "Error al registrar",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Registrarse")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        onRegisterSuccess()
                    }
                ) {
                    Text("← Volver al inicio de sesión")
                }

            }
        }
    }
}
