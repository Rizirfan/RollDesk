package com.example.crattendance.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.crattendance.theme.Teal

enum class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Dashboard("Home", Icons.Filled.Home, Icons.Outlined.Home),
    TakeAttendance("Attendance", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    Students("Students", Icons.Filled.People, Icons.Outlined.People),
}

@Composable
fun AppBottomNavBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
) {
    NavigationBar(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
    ) {
        BottomNavItem.entries.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Teal,
                    selectedTextColor = Teal,
                    indicatorColor = Teal.copy(alpha = 0.1f),
                    unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
