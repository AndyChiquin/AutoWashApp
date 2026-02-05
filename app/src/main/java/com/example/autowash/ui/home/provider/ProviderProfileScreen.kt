package com.example.autowash.ui.home.provider

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.ByteArrayOutputStream


@Composable
fun ProviderProfileScreen() {

    // 🔹 NAVEGACIÓN A CUENTA BANCARIA
    var showBankInfo by remember { mutableStateOf(false) }
    if (showBankInfo) {
        ProviderBankInfoScreen(onBack = { showBankInfo = false })
        return
    }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var newPassword by remember { mutableStateOf("") }
    var changePassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // 🔹 AVATAR STATES
    var photoBase64 by remember { mutableStateOf<String?>(null) }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // 🔹 CÁMARA
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            profileBitmap = it

            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            photoBase64 = Base64.encodeToString(
                outputStream.toByteArray(),
                Base64.DEFAULT
            )
        }
    }

    // 🔹 CARGAR DATOS DEL PROVIDER
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        email = auth.currentUser?.email ?: ""

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                firstName = doc.getString("firstName") ?: ""
                lastName = doc.getString("lastName") ?: ""
                phone = doc.getString("phone") ?: ""

                photoBase64 = doc.getString("photoBase64")
                photoBase64?.let {
                    val bytes = Base64.decode(it, Base64.DEFAULT)
                    profileBitmap =
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🔹 AVATAR
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap!!.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Button(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("Tomar Foto")
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (profileBitmap != null) {
                    Button(
                        onClick = {

                            // 🔹 LIMPIAR ESTADO LOCAL
                            profileBitmap = null
                            photoBase64 = null

                            val uid = auth.currentUser?.uid ?: return@Button

                            // 🔹 ELIMINAR CAMPO EN FIRESTORE
                            db.collection("users")
                                .document(uid)
                                .update("photoBase64", FieldValue.delete())
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Imagen eliminada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Error al eliminar imagen",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                        },
                        modifier = Modifier.height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar imagen")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }


                Text(
                    text = "Mi Perfil",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                OutlinedTextField(
                    value = firstName,
                    onValueChange = {
                        if (it.all { c -> c.isLetter() || c.isWhitespace() } && it.length <= 20) {
                            firstName = it
                        }
                    },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = {
                        if (it.all { c -> c.isLetter() || c.isWhitespace() } && it.length <= 20) {
                            lastName = it
                        }
                    },
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() } && it.length <= 10) {
                            phone = it
                        }
                    },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = changePassword,
                        onCheckedChange = {
                            changePassword = it
                            if (!it) newPassword = ""
                        }
                    )
                    Text("Cambiar contraseña")
                }

                if (changePassword) {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isLoading,
                    onClick = {

                        val uid = auth.currentUser?.uid ?: return@Button

                        if (firstName.length < 2 || lastName.length < 2) {
                            Toast.makeText(
                                context,
                                "Nombre y apellido no válidos",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (phone.length != 10) {
                            Toast.makeText(context, "Teléfono inválido", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (changePassword && newPassword.length < 6) {
                            Toast.makeText(context, "Contraseña muy corta", Toast.LENGTH_SHORT)
                                .show()
                            return@Button
                        }

                        isLoading = true

                        val updates = mutableMapOf(
                            "firstName" to firstName.trim(),
                            "lastName" to lastName.trim(),
                            "phone" to phone.trim()
                        )

                        photoBase64?.let {
                            updates["photoBase64"] = it
                        }

                        db.collection("users")
                            .document(uid)
                            .set(updates, SetOptions.merge())
                            .addOnSuccessListener {

                                if (changePassword) {
                                    auth.currentUser?.updatePassword(newPassword)
                                }

                                isLoading = false
                                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp))
                    } else {
                        Text("Guardar cambios")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    onClick = { showBankInfo = true }
                ) {
                    Text("Cuenta bancaria")
                }
            }
        }
    }
}
