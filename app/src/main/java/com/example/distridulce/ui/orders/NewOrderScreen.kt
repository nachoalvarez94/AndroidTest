package com.example.distridulce.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.distridulce.model.Client
import com.example.distridulce.model.initials
import com.example.distridulce.model.mockClients
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.TextPrimary
import com.example.distridulce.ui.theme.TextSecondary

// ── Avatar palette ─────────────────────────────────────────────────────────────

private val avatarPalette = listOf(
    Color(0xFF2563EB), // blue
    Color(0xFF7C3AED), // purple
    Color(0xFF059669), // green
    Color(0xFFD97706), // amber
    Color(0xFFDC2626), // red
    Color(0xFF0891B2), // cyan
    Color(0xFF9333EA), // violet
    Color(0xFF16A34A), // emerald
)

private fun avatarColor(index: Int): Color = avatarPalette[index % avatarPalette.size]

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * Shows a grid of selectable clients. Clicking a client triggers [onClientSelected]
 * so the caller (NavGraph) can navigate to CatalogScreen with the chosen client.
 */
@Composable
fun NewOrderScreen(onClientSelected: (Client) -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            NewOrderHeader()

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = mockClients,
                    key = { _, client -> client.id }
                ) { index, client ->
                    ClientCard(
                        client = client,
                        avatarColor = avatarColor(index),
                        onClick = { onClientSelected(client) }
                    )
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun NewOrderHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Nuevo Pedido",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Selecciona un cliente para continuar",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

// ── Client card ───────────────────────────────────────────────────────────────

/**
 * Clickable card that represents a single client.
 * The card includes a colour-coded avatar with initials, plus
 * the client's name, address and phone number.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientCard(
    client: Client,
    avatarColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 4.dp   // subtle lift on press
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClientAvatar(initials = client.initials(), color = avatarColor)

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = client.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1
                )
                ClientDetailRow(
                    icon = Icons.Filled.LocationOn,
                    text = client.address
                )
                ClientDetailRow(
                    icon = Icons.Filled.Phone,
                    text = client.phone
                )
            }
        }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun ClientAvatar(initials: String, color: Color) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ClientDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1
        )
    }
}
