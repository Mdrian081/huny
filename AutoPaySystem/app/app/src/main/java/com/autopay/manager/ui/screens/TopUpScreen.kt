package com.autopay.manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autopay.manager.data.TopUpOrder
import com.autopay.manager.ui.AppViewModel
import com.autopay.manager.ui.theme.TextSecondary

private data class Package(val label: String, val price: Double)

private val packages = listOf(
    Package("Free Fire 25 Diamonds", 25.0),
    Package("Free Fire 100 Diamonds", 95.0),
    Package("Free Fire 310 Diamonds", 290.0),
    Package("Free Fire 520 Diamonds", 480.0)
)

@Composable
fun TopUpScreen(viewModel: AppViewModel) {
    val orders by viewModel.topups.collectAsState()

    var selected by remember { mutableStateOf(packages.first()) }
    var playerId by remember { mutableStateOf("") }
    var trxId by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text("Game Top-up", style = MaterialTheme.typography.headlineMedium) }

        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DropdownField(
                        "Package",
                        packages.map { it.label },
                        selected.label
                    ) { label -> selected = packages.first { it.label == label } }

                    Text("Price: ৳ ${selected.price}", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = playerId, onValueChange = { playerId = it },
                        label = { Text("Free Fire Player ID") }, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = trxId, onValueChange = { trxId = it },
                        label = { Text("Payment Transaction ID") }, modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (playerId.isBlank() || trxId.isBlank()) return@Button
                            viewModel.placeTopUpOrder(
                                TopUpOrder(
                                    product = selected.label,
                                    playerId = playerId,
                                    price = selected.price,
                                    trxId = trxId,
                                    status = "pending"
                                )
                            )
                            playerId = ""; trxId = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Place order") }
                }
            }
        }

        item { Text("Order history", style = MaterialTheme.typography.titleLarge) }

        if (orders.isEmpty()) {
            item { EmptyState("No top-up orders yet.") }
        }

        items(orders) { order ->
            Card(shape = RoundedCornerShape(14.dp)) {
                Row(
                    Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(order.product)
                        Text("Player ID: ${order.playerId}", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                    StatusBadge(order.status)
                }
            }
        }
    }
}
