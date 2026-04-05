package com.example.fintrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fintrack.data.local.database.FinTrackDatabase
import com.example.fintrack.data.repository.TransactionRepository
import com.example.fintrack.navigation.FinTrackNavGraph
import com.example.fintrack.navigation.Screen
import com.example.fintrack.navigation.bottomNavItems
import com.example.fintrack.ui.theme.*
import com.example.fintrack.viewmodel.TransactionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = FinTrackDatabase.getDatabase(this)
        val repository = TransactionRepository(database.transactionDao())

        setContent {
            FinTrackTheme {
                val navController = rememberNavController()
                val viewModel: TransactionViewModel = viewModel(
                    factory = TransactionViewModel.Factory(repository)
                )

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Screens where bottom nav is hidden
                val hideBottomNav = currentRoute == Screen.AddTransaction.route ||
                        currentRoute?.startsWith("edit_transaction") == true

                Scaffold(
                    bottomBar = {
                        if (!hideBottomNav) {
                            NavigationBar(
                                containerColor = SurfaceLight,
                                tonalElevation = 8.dp
                            ) {
                                bottomNavItems.forEach { item ->
                                    val selected = currentRoute == item.screen.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.screen.route) {
                                                popUpTo(Screen.Home.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Text(
                                                text = when (item.label) {
                                                    "Home" -> "🏠"
                                                    "Transactions" -> "💳"
                                                    "Insights" -> "📊"
                                                    "Streak" -> "🔥"
                                                    else -> "•"
                                                },
                                                style = MaterialTheme.typography.headlineSmall
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = item.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (selected) PurplePrimary
                                                else TextSecondary
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = PurplePrimary,
                                            indicatorColor = SurfaceVariantLight
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    FinTrackNavGraph(
                        navController = navController,
                        viewModel = viewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}