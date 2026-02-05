package com.example.autowash.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

@Composable
fun UserTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        "Servicios",
        "Mis Reservas",
        "Perfil",
        "Métodos de Pago"
    )

    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 16.dp
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(title)
                }
            )
        }
    }
}
