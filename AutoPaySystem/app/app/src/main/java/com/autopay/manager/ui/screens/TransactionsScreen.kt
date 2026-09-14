package com.autopay.manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autopay.manager.data.Transaction
import com.autopay.manager.ui.AppViewModel

@Composable
fun TransactionsScreen(viewModel: AppViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add manual transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Text("Transactions", style = MaterialTheme.typography.headlineMedium) }

            if (transactions.isEmpty()) {
                item { EmptyState("No transactions yet.") }
            }

            items(transactions) { tx ->
                TransactionRow(tx)
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { tx ->
                viewModel.addManualTransaction(tx)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddTransactionDialog(onDismiss: () -> Unit, onSave: (Transaction) -> Unit) {
    var method by remember { mutableStateOf("Nagad") }
    var type by remember { mutableStateOf("Personal") }
    var amount by remember { mutableStateOf("") }
    var trxId by remember { mutableStateOf("") }
    var senderNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add manual transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DropdownField("Method", listOf("Nagad", "bKash", "Manual"), method) { method = it }
                DropdownField("Type", listOf("Personal", "Merchant", "Agent"), type) { type = it }
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (BDT)") })
                OutlinedTextField(value = trxId, onValueChange = { trxId = it }, label = { Text("Transaction ID") })
                OutlinedTextField(value = senderNumber, onValueChange = { senderNumber = it }, label = { Text("Phone number") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    Transaction(
                        method = method,
                        type = type,
                        trxId = trxId,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        senderNumber = senderNumber,
                        rawMessage = "Manually entered",
                        status = "unused"
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DropdownField(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().then(Modifier),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = {
                    onSelected(opt)
                    expanded = false
                })
            }
        }
    }
}
