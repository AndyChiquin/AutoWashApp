package com.example.autowash.ui.home.provider

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProviderStatsCard(
    totalOrders: Int,
    paidOrders: Int,
    pendingPayments: Int,
    paymentRate: Int,
    providerLevel: String
) {
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
            text = "📊 Tu desempeño",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Reservas")
                Text(
                    text = totalOrders.toString(),
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text("Pagadas")
                Text(
                    text = paidOrders.toString(),
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text("Pendientes")
                Text(
                    text = pendingPayments.toString(),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("📈 Tasa de pago: $paymentRate%")
        Text(
            text = providerLevel,
            fontWeight = FontWeight.SemiBold
        )
    }
}
