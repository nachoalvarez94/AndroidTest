package com.example.distridulce.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.distridulce.navigation.Screen
import com.example.distridulce.navigation.navigateTopLevel
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.IconBgBlue
import com.example.distridulce.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sidebar(navController: NavController) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    PermanentDrawerSheet(
        modifier = Modifier.width(240.dp),
        drawerContainerColor = Color.White
    ) {
        // ── Logo ─────────────────────────────────────────────────────────────
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "DistriDulce",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BrandBlue,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Text(
            text = "Gestión Comercial",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Navigation items ─────────────────────────────────────────────────
        Screen.all.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                selected = selected,
                onClick = { navController.navigateTopLevel(screen.route) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = BrandBlue,
                    selectedIconColor      = Color.White,
                    selectedTextColor      = Color.White,
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor    = TextSecondary,
                    unselectedTextColor    = TextSecondary,
                ),
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        // ── Push user section to bottom ───────────────────────────────────────
        Spacer(modifier = Modifier.weight(1f))

        Divider(color = MaterialTheme.colorScheme.outline)

        SidebarUser(
            name  = "Admin",
            email = "admin@distridulce.com"
        )
    }
}

@Composable
private fun SidebarUser(name: String, email: String) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(IconBgBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = email,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
