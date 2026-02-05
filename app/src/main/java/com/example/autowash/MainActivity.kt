package com.example.autowash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.autowash.ui.login.LoginScreen
import com.example.autowash.ui.register.RegisterScreen
import com.example.autowash.ui.theme.AutoWashAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AutoWashAppTheme {

                var currentScreen by remember { mutableStateOf("login") }
                val auth = FirebaseAuth.getInstance()
                val db = FirebaseFirestore.getInstance()

                // 🔹 MANTENER SESIÓN (APP ABIERTA O RECIÉN ABIERTA)
                LaunchedEffect(Unit) {
                    val user = auth.currentUser
                    if (user != null) {
                        db.collection("users")
                            .document(user.uid)
                            .get()
                            .addOnSuccessListener { document ->

                                val isActive = document.getBoolean("active") ?: false

                                if (!isActive) {
                                    auth.signOut()
                                    currentScreen = "login"
                                } else {
                                    val role = document.getString("role") ?: "USER"
                                    currentScreen = when (role) {
                                        "ADMIN" -> "admin"
                                        "PROVIDER" -> "provider"
                                        else -> "user"
                                    }
                                }
                            }
                    }
                }

                when (currentScreen) {

                    // 🔐 LOGIN
                    "login" -> {
                        LoginScreen(
                            onLoginClick = { email, password, onError ->

                                auth.signInWithEmailAndPassword(
                                    email.trim(),
                                    password.trim()
                                ).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        val uid = auth.currentUser?.uid
                                            ?: return@addOnCompleteListener

                                        db.collection("users")
                                            .document(uid)
                                            .get()
                                            .addOnSuccessListener { document ->

                                                val isActive =
                                                    document.getBoolean("active") ?: false

                                                if (!isActive) {
                                                    auth.signOut()
                                                    onError(
                                                        "🚫 Tu cuenta está inactiva. Contacta con el administrador."
                                                    )
                                                } else {
                                                    val role =
                                                        document.getString("role") ?: "USER"

                                                    currentScreen = when (role) {
                                                        "ADMIN" -> "admin"
                                                        "PROVIDER" -> "provider"
                                                        else -> "user"
                                                    }
                                                }
                                            }
                                            .addOnFailureListener {
                                                auth.signOut()
                                                onError("Error al cargar el usuario")
                                            }

                                    } else {
                                        val message = when (task.exception) {
                                            is FirebaseAuthInvalidUserException ->
                                                "Este correo no está registrado"

                                            is FirebaseAuthInvalidCredentialsException ->
                                                "Correo o contraseña incorrectos"

                                            else ->
                                                "Error al iniciar sesión"
                                        }
                                        onError(message)
                                    }
                                }
                            },

                            // 🔑 OLVIDÉ MI CONTRASEÑA
                            onForgotPassword = { email, onResult ->
                                FirebaseAuth.getInstance()
                                    .sendPasswordResetEmail(email.trim())
                                    .addOnSuccessListener {
                                        onResult("📩 Revisa tu correo para restablecer la contraseña")
                                    }
                                    .addOnFailureListener {
                                        onResult("❌ El correo no está registrado")
                                    }
                            },

                            onGoToRegister = {
                                currentScreen = "register"
                            }
                        )
                    }

                    // 📝 REGISTRO
                    "register" -> {
                        RegisterScreen {
                            currentScreen = "login"
                        }
                    }

                    // 👤 USER
                    "user" -> {
                        com.example.autowash.ui.home.user.UserHomeScreen(
                            onLogout = {
                                auth.signOut()
                                currentScreen = "login"
                            }
                        )
                    }

                    // 👑 ADMIN
                    "admin" -> {
                        com.example.autowash.ui.home.admin.AdminHomeScreen(
                            onLogout = {
                                auth.signOut()
                                currentScreen = "login"
                            }
                        )
                    }

                    // 🚗 PROVIDER
                    "provider" -> {
                        com.example.autowash.ui.home.provider.ProviderHomeScreen(
                            onLogout = {
                                auth.signOut()
                                currentScreen = "login"
                            }
                        )
                    }
                }
            }
        }
    }
}
