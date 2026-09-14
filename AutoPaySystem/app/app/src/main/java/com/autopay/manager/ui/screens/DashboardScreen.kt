package com.autopay.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autopay.manager.data.Transaction
import com.autopay.manager.ui.AppViewModel
import com.autopay.manager.ui.theme.BrandMint
import com.autopay.manager.ui.theme.BrandPurple
import com.autopay.manager.ui.theme.BrandPurpleDark
import com.autopay.manager.ui.theme.SuccessGreen
import com.autopay.manager.ui.theme.TextSecondary
import com.autopay.manager.ui.theme.WarningAmber
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val balances by viewModel.balances.collectAsState()

    val totalBalance = balances.sumOf { it.currentBalance }
    val verifiedCount = transactions.count { it.status == "unused" || it.status == "used" }
    val pendingReview = transactions.count { it.status == "awaiting-review" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text("Overview", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            BalanceCard(totalBalance)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatChip(
                    label = "Verified SMS",
                    value = verifiedCount.toString(),
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Awaiting review",
                    value = pendingReview.toString(),
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text("Recent activity", style = MaterialTheme.typography.titleLarge)
        }

        if (transactions.isEmpty()) {
            item {
                EmptyState("No SMS-verified payments yet. They'll show up here automatically.")
            }
        }

        items(transactions.take(15)) { tx ->
            TransactionRow(tx)
        }
    }
}

@Composable
private fun BalanceCard(total: Double) {
    val formatted = remember(total) {
        NumberFormat.getNumberInstance(Locale.US).format(total)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(BrandPurple, BrandPurpleDark)))
            .padding(24.dp)
    ) {
        Column {
            Text("Total balance", color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(6.dp))
            Text(
                "৳ $formatted",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = color)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
fun TransactionRow(tx: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = if (tx.status == "error") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
            val tint = when (tx.status) {
                "used" -> TextSecondary
                "awaiting-review" -> WarningAmber
                "error" -> Color.Red
                else -> SuccessGreen
            }
            Icon(icon, contentDescription = null, tint = tint)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${tx.method} • ${tx.type}", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (tx.trxId != "--") "TrxID: ${tx.trxId}" else tx.rawMessage.take(40),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("৳ ${tx.amount}", style = MaterialTheme.typography.titleMedium)
                StatusBadge(tx.status)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "used" -> TextSecondary
        "awaiting-review" -> WarningAmber
        "error" -> Color.Red
        else -> BrandMint
    }
    Text(status, color = color, style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}
