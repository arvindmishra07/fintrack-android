package com.example.fintrack.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fintrack.domain.model.TransactionType
import com.example.fintrack.ui.components.*
import com.example.fintrack.ui.theme.*
import com.example.fintrack.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: TransactionViewModel,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Int) -> Unit
) {
    val balance by viewModel.currentBalance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val last7Days by viewModel.last7DaysExpenses.collectAsState()
    val streak by viewModel.noSpendStreak.collectAsState()

    val recentTransactions = transactions.take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ─── Header ───────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(GradientDarkStart, GradientDarkEnd)
                        )
                    )
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 32.dp)
            ) {
                Column {
                    // Greeting
                    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val greeting = when {
                        hour < 12 -> "Good Morning"
                        hour < 17 -> "Good Afternoon"
                        else -> "Good Evening"
                    }

                    Text(
                        text = "$greeting 👋",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextOnDark.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "FinTrack",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextOnDark
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Balance Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PurplePrimary, PurpleLight)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(
                                text = "Total Balance",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnDark.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "₹${String.format("%,.2f", balance)}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextOnDark
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SummaryChip(
                                    label = "Income",
                                    amount = totalIncome,
                                    emoji = "📈",
                                    backgroundColor = IncomeGreen.copy(alpha = 0.3f),
                                    modifier = Modifier.weight(1f)
                                )
                                SummaryChip(
                                    label = "Expense",
                                    amount = totalExpense,
                                    emoji = "📉",
                                    backgroundColor = ExpenseRed.copy(alpha = 0.3f),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ─── Streak Banner ────────────────────────────────────────────────
        item {
            if (streak > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    StreakFire.copy(alpha = 0.15f),
                                    AmberAccent.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "🔥", fontSize = 32.sp)
                        Column {
                            Text(
                                text = "$streak day no-spend streak!",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = StreakFire
                            )
                            Text(
                                text = "Keep it going, you're doing great!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // ─── Weekly Spending Chart ────────────────────────────────────────
        item {
            SurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Last 7 Days",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (last7Days.isEmpty() || last7Days.all { it.second == 0.0 }) {
                    EmptyState(
                        emoji = "📊",
                        title = "No spending yet",
                        subtitle = "Your weekly chart will appear here"
                    )
                } else {
                    WeeklyBarChart(data = last7Days)
                }
            }
        }

        // ─── Recent Transactions Header ───────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "See all →",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PurplePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ─── Recent Transactions List ─────────────────────────────────────
        if (recentTransactions.isEmpty()) {
            item {
                EmptyState(
                    emoji = "💸",
                    title = "No transactions yet",
                    subtitle = "Tap the + button to add your first transaction",
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            items(recentTransactions) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onEdit = { onEditTransaction(it.id) },
                    onDelete = { viewModel.deleteTransaction(it) }
                )
            }
        }

        // ─── Add Transaction FAB space ────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // ─── FAB ──────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = onAddTransaction,
            modifier = Modifier.padding(24.dp),
            containerColor = PurplePrimary,
            contentColor = Color.White
        ) {
            Text(text = "+", fontSize = 28.sp, fontWeight = FontWeight.Light)
        }
    }
}

// ─── Weekly Bar Chart ──────────────────────────────────────────────────────────
@Composable
fun WeeklyBarChart(data: List<Pair<String, Double>>) {
    val maxValue = data.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, amount) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // Amount label on top of bar
                if (amount > 0) {
                    Text(
                        text = "₹${amount.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 8.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bar
                val barHeightFraction = (amount / maxValue).toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(
                            if (barHeightFraction > 0f)
                                barHeightFraction.coerceAtLeast(0.05f)
                            else 0.05f
                        )
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (amount > 0)
                                    listOf(PurplePrimary, PurpleLight)
                                else
                                    listOf(
                                        NeutralDay,
                                        NeutralDay
                                    )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Day label
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 9.sp
                )
            }
        }
    }
}