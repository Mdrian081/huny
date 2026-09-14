package com.autopay.manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autopay.manager.data.WithdrawRequest
import com.autopay.manager.ui.AppViewModel
import com.autopay.manager.ui.theme.TextSecondary

@Composable
fun WithdrawScreen(viewModel: AppViewModel) {
    val withdrawals by viewModel.withdrawals.collectAsState()

    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("Nagad") }
    var number by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text("Withdraw", style = MaterialTheme.typography.headlineMedium) }

        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it },
                        label = { Text("Amount (BDT)") }, modifier = Modifier.fillMaxWidth()
                    )
                    DropdownField("Send to", listOf("Nagad", "bKash", "Bank"), method) { method = it }
                    OutlinedTextField(
                        value = number, onValueChange = { number = it },
                        label = { Text("Account / phone number") }, modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: return@Button
                            viewModel.requestWithdraw(
                                WithdrawRequest(
                                    amount = amt,
                                    destinationMethod = method,
                                    destinationNumber = number,
                                    status = "pending"
                                )
                            )
                            amount = ""; number = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Request withdrawal") }
                }
            }
        }

        item { Text("History", style = MaterialTheme.typography.titleLarge) }

        if (withdrawals.isEmpty()) {
            item { EmptyState("No withdrawal requests yet.") }
        }

        items(withdrawals) { w ->
            Card(shape = RoundedCornerShape(14.dp)) {
                Row(
                    Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("৳ ${w.amount} → ${w.destinationMethod}")
                        Text(w.destinationNumber, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                    StatusBadge(w.status)
                }
            }
        }
    }
}
