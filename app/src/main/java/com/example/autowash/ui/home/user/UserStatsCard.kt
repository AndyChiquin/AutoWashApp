package com.example.autowash.ui.home.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun UserStatsCard(
    totalOrders: Int,
    paidOrders: Int,
    pendingPayments: Int
) {
    val reputation = if (totalOrders == 0) 0.0 else (paidOrders.toDouble() / totalOrders) * 5

    val (statusText, statusColor) = when {
        reputation >= 4.5 -> "🟢 Cliente confiable" to Color(0xFF2E7D32)
        reputation >= 3.0 -> "🟡 Cliente frecuente" to Color(0xFFF9A825)
        else -> "🔴 Cliente con pagos pendientes" to Color(0xFFC62828)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {

        Text(
            text = "📊 Tu actividad",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem("Reservas", totalOrders.toString())
            StatItem("Pagadas", paidOrders.toString())
            StatItem("Pendientes", pendingPayments.toString())
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "⭐ Reputación: ${(reputation * 10).roundToInt() / 10.0} / 5",
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = statusText,
            color = statusColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}
