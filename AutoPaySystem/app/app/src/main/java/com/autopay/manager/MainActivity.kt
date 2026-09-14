package com.autopay.manager

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.autopay.manager.data.FirebaseConfigManager
import com.autopay.manager.data.FirebaseRepository
import com.autopay.manager.ui.AppViewModel
import com.autopay.manager.ui.screens.ConnectFirebaseScreen
import com.autopay.manager.ui.screens.DashboardScreen
import com.autopay.manager.ui.screens.SettingsScreen
import com.autopay.manager.ui.screens.SetupGuideScreen
import com.autopay.manager.ui.screens.TopUpScreen
import com.autopay.manager.ui.screens.TransactionsScreen
import com.autopay.manager.ui.screens.WithdrawScreen
import com.autopay.manager.ui.theme.AutoPayTheme

private sealed class Dest(val route: String, val label: String) {
    data object Dashboard : Dest("dashboard", "Home")
    data object Transactions : Dest("transactions", "Transactions")
    data object Withdraw : Dest("withdraw", "Withdraw")
    data object TopUp : Dest("topup", "Top-up")
    data object Setup : Dest("setup", "API Setup")
    data object Settings : Dest("settings", "Settings")
}

private val bottomDestinations =
    listOf(Dest.Dashboard, Dest.Transactions, Dest.Withdraw, Dest.TopUp, Dest.Setup, Dest.Settings)

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* best-effort; app still works without them, just without auto-sync */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())

        setContent {
            AutoPayTheme {
                Surface {
                    RootScreen()
                }
            }
        }
    }
}

@Composable
private fun RootScreen() {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(FirebaseConfigManager.isConnected(context)) }

    if (!isConnected) {
        ConnectFirebaseScreen(onConnected = { isConnected = true })
    } else {
        val firestore = remember { FirebaseConfigManager.getFirestore(context) }
        if (firestore == null) {
            // Config existed but failed to initialize - let the user reconnect.
            ConnectFirebaseScreen(onConnected = { isConnected = true })
        } else {
            val repository = remember { FirebaseRepository(firestore) }
            val viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory(repository))
            AppScaffold(viewModel)
        }
    }
}

@Composable
private fun AppScaffold(viewModel: AppViewModel) {
    val navController: NavHostController = rememberNavController()

    Scaffold(
        bottomBar = { AppBottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Dest.Dashboard.route) { DashboardScreen(viewModel) }
            composable(Dest.Transactions.route) { TransactionsScreen(viewModel) }
            composable(Dest.Withdraw.route) { WithdrawScreen(viewModel) }
            composable(Dest.TopUp.route) { TopUpScreen(viewModel) }
            composable(Dest.Setup.route) { SetupGuideScreen(viewModel) }
            composable(Dest.Settings.route) { SettingsScreen(viewModel) }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        bottomDestinations.forEach { dest ->
            val icon = when (dest) {
                Dest.Dashboard -> Icons.Default.Home
                Dest.Transactions -> Icons.Default.List
                Dest.Withdraw -> Icons.Default.AccountBalanceWallet
                Dest.TopUp -> Icons.Default.SportsEsports
                Dest.Setup -> Icons.Default.Link
                Dest.Settings -> Icons.Default.Settings
            }
            NavigationBarItem(
                selected = currentRoute == dest.route,
                onClick = {
                    navController.navigate(dest.route) {
                        launchSingleTop = true
                        popUpTo(Dest.Dashboard.route)
                    }
                },
                icon = { Icon(icon, contentDescription = dest.label) },
                label = { Text(dest.label) }
            )
        }
    }
}
