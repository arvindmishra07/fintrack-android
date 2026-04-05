package com.example.fintrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fintrack.ui.home.HomeScreen
import com.example.fintrack.ui.insights.InsightsScreen
import com.example.fintrack.ui.streak.StreakScreen
import com.example.fintrack.ui.transactions.AddEditTransactionScreen
import com.example.fintrack.ui.transactions.TransactionScreen
import com.example.fintrack.viewmodel.TransactionViewModel

// ─── Routes ───────────────────────────────────────────────────────────────────
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Transactions : Screen("transactions")
    object AddTransaction : Screen("add_transaction")
    object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: Int) = "edit_transaction/$transactionId"
    }
    object Insights : Screen("insights")
    object Streak : Screen("streak")
}

// ─── Bottom Nav Items ─────────────────────────────────────────────────────────
sealed class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: String
) {
    object Home : BottomNavItem(Screen.Home, "Home", "home")
    object Transactions : BottomNavItem(Screen.Transactions, "Transactions", "transactions")
    object Insights : BottomNavItem(Screen.Insights, "Insights", "insights")
    object Streak : BottomNavItem(Screen.Streak, "Streak", "streak")
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Transactions,
    BottomNavItem.Insights,
    BottomNavItem.Streak
)

// ─── Nav Graph ────────────────────────────────────────────────────────────────
@Composable
fun FinTrackNavGraph(
    navController: NavHostController,
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onEditTransaction = { id ->
                    navController.navigate(Screen.EditTransaction.createRoute(id))
                },
                onViewAllTransactions = {
                    navController.navigate(Screen.Transactions.route)
                }
            )
        }

        composable(Screen.AddTransaction.route) {
            AddEditTransactionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Transactions.route) {
            TransactionScreen(
                viewModel = viewModel,
                onAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onEditTransaction = { id ->
                    navController.navigate(Screen.EditTransaction.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getInt("transactionId") ?: return@composable
            val transactions by viewModel.allTransactions.collectAsState()
            val transaction = transactions.find { it.id == transactionId }
            transaction?.let {
                AddEditTransactionScreen(
                    viewModel = viewModel,
                    existingTransaction = it,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Insights.route) {
            InsightsScreen(viewModel = viewModel)
        }

        composable(Screen.Streak.route) {
            StreakScreen(viewModel = viewModel)
        }
    }
}