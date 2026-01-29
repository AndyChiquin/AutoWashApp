package com.example.autowash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.autowash.ui.login.LoginScreen
import com.example.autowash.ui.register.RegisterScreen
import com.example.autowash.ui.home.HomeScreen
import com.example.autowash.ui.theme.AutoWashAppTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Text


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AutoWashAppTheme {

                var currentScreen by remember { mutableStateOf("login") }
                val auth = FirebaseAuth.getInstance()

                LaunchedEffect(Unit) {
                    val user = auth.currentUser
                    if (user != null) {

                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                        db.collection("users")
                            .document(user.uid)
                            .get()
                            .addOnSuccessListener { document ->

                                val role = document.getString("role") ?: "USER"

                                currentScreen = when (role) {
                                    "ADMIN" -> "admin"
                                    "PROVIDER" -> "provider"
                                    else -> "user"
                                }
                            }
                    }
                }


                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            onLoginClick = { email, password ->
                                val cleanEmail = email.trim()
                                val cleanPassword = password.trim()

                                auth.signInWithEmailAndPassword(
                                    cleanEmail,
                                    cleanPassword
                                ).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                                        db.collection("users")
                                            .document(uid)
                                            .get()
                                            .addOnSuccessListener { document ->

                                                val role = document.getString("role") ?: "USER"

                                                println("✅ Rol del usuario: $role")

                                                currentScreen = when (role) {
                                                    "ADMIN" -> "admin"
                                                    "PROVIDER" -> "provider"
                                                    else -> "user"
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                println("❌ Error al leer usuario: ${e.message}")
                                            }
                                    } else {
                                        println("❌ Login error: ${task.exception?.message}")
                                    }
                                }
                            },
                            onGoToRegister = {
                                currentScreen = "register"
                            }
                        )
                    }

                    "register" -> {
                        RegisterScreen {
                            currentScreen = "login"
                        }
                    }
                    "user" -> {
                        com.example.autowash.ui.home.user.UserHomeScreen(
                            onLogout = {
                                currentScreen = "login"
                            }
                        )
                    }

                    "admin" -> {
                        com.example.autowash.ui.home.admin.AdminHomeScreen(
                            onLogout = {
                                currentScreen = "login"
                            }
                        )
                    }

                    "provider" -> {
                        com.example.autowash.ui.home.provider.ProviderHomeScreen(
                            onLogout = {
                                currentScreen = "login"
                            }
                        )
                    }


                }
            }
        }
    }
}
