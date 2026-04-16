@file:OptIn(ExperimentalComposeUiApi::class)

package com.example.distridulce.ui.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.distridulce.model.Client
import com.example.distridulce.model.initials
import com.example.distridulce.model.mockClients
import com.example.distridulce.ui.theme.BackgroundLight
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.TextPrimary
import com.example.distridulce.ui.theme.TextSecondary

// ── Avatar palette ─────────────────────────────────────────────────────────────

private val avatarPalette = listOf(
    Color(0xFF2563EB),
    Color(0xFF7C3AED),
    Color(0xFF059669),
    Color(0xFFD97706),
    Color(0xFFDC2626),
    Color(0xFF0891B2),
    Color(0xFF9333EA),
    Color(0xFF16A34A),
)

private fun avatarColor(index: Int): Color = avatarPalette[index % avatarPalette.size]

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * Client management screen.
 *
 * Shows a searchable grid of [ClientCard]s. Tapping a card opens a [ClientDetailDialog];
 * tapping *Nuevo Pedido* (card button or dialog button) calls [onNewOrder] with the
 * selected client, which the NavGraph wires directly to [OrderBuilderScreen].
 */
@Composable
fun ClientsScreen(onNewOrder: (Client) -> Unit = {}) {
    var query        by remember { mutableStateOf("") }
    var detailClient by remember { mutableStateOf<Client?>(null) }

    val filtered = remember(query) {
        if (query.isBlank()) mockClients
        else mockClients.filter { it.name.contains(query, ignoreCase = true) }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = BackgroundLight) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            ClientsHeader(total = mockClients.size)

            // ── Search bar ────────────────────────────────────────────────────
            ClientSearchBar(query = query, onQueryChange = { query = it })

            // ── Content ───────────────────────────────────────────────────────
            if (filtered.isEmpty()) {
                EmptyClientsState(query = query, modifier = Modifier.weight(1f))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement   = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = filtered, key = { it.id }) { client ->
                        val index = mockClients.indexOf(client)
                        ClientCard(
                            client      = client,
                            avatarColor = avatarColor(index),
                            onClick     = { detailClient = client },
                            onNewOrder  = { onNewOrder(client) }
                        )
                    }
                }
            }
        }
    }

    // ── Detail dialog ─────────────────────────────────────────────────────────
    detailClient?.let { client ->
        val index = mockClients.indexOf(client)
        ClientDetailDialog(
            client      = client,
            avatarColor = avatarColor(index),
            onNewOrder  = {
                detailClient = null
                onNewOrder(client)
            },
            onDismiss   = { detailClient = null }
        )
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun ClientsHeader(total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = "Clientes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text  = "$total clientes en cartera",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────

/**
 * Real-time case-insensitive search field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = modifier.fillMaxWidth(),
        placeholder   = { Text("Buscar clientes…", color = TextSecondary) },
        leadingIcon   = {
            Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = TextSecondary)
        },
        trailingIcon  = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Limpiar búsqueda",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine    = true,
        shape         = RoundedCornerShape(12.dp),
        colors        = TextFieldDefaults.outlinedTextFieldColors(
            containerColor       = Color.White,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedBorderColor   = BrandBlue
        )
    )
}

// ── Client card ───────────────────────────────────────────────────────────────

/**
 * Displays a client summary. The whole card is tappable ([onClick]) to open the
 * detail dialog; the *Nuevo Pedido* button triggers [onNewOrder] directly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientCard(
    client: Client,
    avatarColor: Color,
    onClick: () -> Unit,
    onNewOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Avatar + name + badge ─────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClientAvatar(initials = client.initials(), color = avatarColor, size = 44)

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text  = client.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Last order row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text  = client.lastOrderText,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                // Orders badge
                OrdersBadge(count = client.totalOrders)
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            // ── Contact details ───────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ClientDetailRow(icon = Icons.Filled.LocationOn, text = client.address)
                ClientDetailRow(icon = Icons.Filled.Phone,      text = client.phone)
                ClientDetailRow(icon = Icons.Filled.Email,      text = client.email)
            }

            // ── Nuevo Pedido button ───────────────────────────────────────────
            Button(
                onClick  = onNewOrder,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCartCheckout,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = "Nuevo Pedido",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

/**
 * Shown when the search query has no matching clients.
 */
@Composable
fun EmptyClientsState(query: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.People,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = TextSecondary.copy(alpha = 0.3f)
            )
            Text(
                text  = "No se encontraron clientes",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            if (query.isNotBlank()) {
                Text(
                    text  = "Prueba con otro nombre",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ── Client detail dialog ──────────────────────────────────────────────────────

/**
 * Full-detail modal for a single client.
 * Shows all contact info, order stats, and a direct *Nuevo Pedido* shortcut.
 */
@Composable
fun ClientDetailDialog(
    client: Client,
    avatarColor: Color,
    onNewOrder: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier  = Modifier.widthIn(max = 480.dp),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Header: avatar + name + close ─────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ClientAvatar(initials = client.initials(), color = avatarColor, size = 52)

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text  = client.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text  = "ID ${client.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // ── Contact section ───────────────────────────────────────────
                Text(
                    text  = "CONTACTO",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClientDetailRow(icon = Icons.Filled.LocationOn, text = client.address, large = true)
                    ClientDetailRow(icon = Icons.Filled.Phone,      text = client.phone,   large = true)
                    ClientDetailRow(icon = Icons.Filled.Email,      text = client.email,   large = true)
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // ── Stats row ─────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatChip(label = "Pedidos totales", value = "${client.totalOrders}", modifier = Modifier.weight(1f))
                    StatChip(label = "Último pedido",   value = client.lastOrderText,   modifier = Modifier.weight(1f))
                }

                // ── Actions ───────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cerrar", color = TextSecondary)
                    }
                    Button(
                        onClick  = onNewOrder,
                        modifier = Modifier.weight(2f),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCartCheckout,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Nuevo Pedido")
                    }
                }
            }
        }
    }
}

// ── Shared sub-components ─────────────────────────────────────────────────────

@Composable
fun ClientAvatar(initials: String, color: Color, size: Int = 48) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = initials,
            style = if (size >= 48) MaterialTheme.typography.titleSmall
                    else MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun ClientDetailRow(
    icon: ImageVector,
    text: String,
    large: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(if (large) 14.dp else 12.dp)
        )
        Text(
            text  = text,
            style = if (large) MaterialTheme.typography.bodySmall
                    else MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OrdersBadge(count: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BrandBlue.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text  = "$count pedidos",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = BrandBlue
        )
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundLight)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
