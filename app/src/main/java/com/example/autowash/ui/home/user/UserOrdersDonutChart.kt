package com.example.autowash.ui.home.user

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun UserOrdersDonutChart(
    paid: Int,
    pending: Int,
    inProcess: Int
) {
    val total = paid + pending + inProcess
    if (total == 0) return

    val paidAngle = paid.toFloat() / total * 360f
    val pendingAngle = pending.toFloat() / total * 360f
    val inProcessAngle = 360f - paidAngle - pendingAngle

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = "📈 Estado de tus reservas",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Canvas(modifier = Modifier.size(220.dp)) {

            val strokeWidth = 28.dp.toPx()
            var startAngle = -90f

            drawArc(
                color = Color(0xFF2E7D32), // Pagadas
                startAngle = startAngle,
                sweepAngle = paidAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            startAngle += paidAngle

            drawArc(
                color = Color(0xFFF9A825), // Pendientes
                startAngle = startAngle,
                sweepAngle = pendingAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            startAngle += pendingAngle

            drawArc(
                color = Color(0xFF0288D1), // En proceso
                startAngle = startAngle,
                sweepAngle = inProcessAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🟢 Pagadas: $paid", style = MaterialTheme.typography.bodySmall)
            Text("🟡 Pendientes: $pending", style = MaterialTheme.typography.bodySmall)
            Text("🔵 Proceso: $inProcess", style = MaterialTheme.typography.bodySmall)
        }
    }
}
