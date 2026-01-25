package com.example.autowash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.autowash.ui.login.LoginScreen
import com.example.autowash.ui.theme.AutoWashAppTheme
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoWashAppTheme {
                LoginScreen { email, password ->

                    val cleanEmail = email.trim()
                    val cleanPassword = password.trim()

                    val auth = FirebaseAuth.getInstance()

                    auth.signInWithEmailAndPassword(cleanEmail, cleanPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                println("✅ Login OK con Firebase")
                            } else {
                                println("❌ Error Firebase: ${task.exception?.message}")
                            }
                        }
                }
            }
        }
    }
}
