package com.example.autowash.ui.home.user.models

import androidx.compose.ui.graphics.vector.ImageVector

data class ServiceUIModel(
    val id: String,
    val title: String,
    val description: String,
    val duration: String,
    val price: String,
    val icon: ImageVector,
    val active: Boolean,
    val providerId: String
)