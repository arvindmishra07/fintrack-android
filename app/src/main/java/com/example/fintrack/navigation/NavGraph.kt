package com.example.fintrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
            // HomeScreen will go here
        }

        composable(Screen.Transactions.route) {
            // TransactionScreen will go here
        }

        composable(Screen.AddTransaction.route) {
            // AddTransactionScreen will go here
        }

        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getInt("transactionId") ?: return@composable
            // EditTransactionScreen will go here
        }

        composable(Screen.Insights.route) {
            // InsightsScreen will go here
        }

        composable(Screen.Streak.route) {
            // StreakScreen will go here
        }
    }
}