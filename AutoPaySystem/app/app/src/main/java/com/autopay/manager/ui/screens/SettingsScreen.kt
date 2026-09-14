package com.autopay.manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autopay.manager.data.BalanceInfo
import com.autopay.manager.ui.AppViewModel

@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val balances by viewModel.balances.collectAsState()
    var method by remember { mutableStateOf("Nagad") }
    var amount by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text("Wallet balances", style = MaterialTheme.typography.headlineMedium) }

        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DropdownField("Method", listOf("Nagad", "bKash"), method) { method = it }
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it },
                        label = { Text("Current balance (BDT)") }, modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: return@Button
                            viewModel.setBalanceQuick(method, amt)
                            amount = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save balance") }
                }
            }
        }

        item { Text("Current balances", style = MaterialTheme.typography.titleLarge) }

        items(balances) { b ->
            Card(shape = RoundedCornerShape(14.dp)) {
                Row(
                    Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(b.method)
                    Text("৳ ${b.currentBalance}")
                }
            }
        }

        item {
            Text(
                "This app only reads SMS that arrive on this phone (RECEIVE_SMS / READ_SMS). " +
                    "It never sends or intercepts messages meant for other apps.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
