package com.example.fintrack.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fintrack.ui.components.EmptyState
import com.example.fintrack.ui.components.SummaryChip
import com.example.fintrack.ui.components.SurfaceCard
import com.example.fintrack.ui.components.TransactionItem
import com.example.fintrack.ui.theme.AmberAccent
import com.example.fintrack.ui.theme.BackgroundLight
import com.example.fintrack.ui.theme.ExpenseRed
import com.example.fintrack.ui.theme.GradientDarkEnd
import com.example.fintrack.ui.theme.GradientDarkStart
import com.example.fintrack.ui.theme.IncomeGreen
import com.example.fintrack.ui.theme.NeutralDay
import com.example.fintrack.ui.theme.PurpleLight
import com.example.fintrack.ui.theme.PurplePrimary
import com.example.fintrack.ui.theme.StreakFire
import com.example.fintrack.ui.theme.TextOnDark
import com.example.fintrack.ui.theme.TextPrimary
import com.example.fintrack.ui.theme.TextSecondary
import com.example.fintrack.viewmodel.TransactionViewModel
import java.util.Calendar
import com.example.fintrack.viewmodel.TransactionViewModel.UiState
import androidx.compose.ui.text.style.TextAlign
import com.example.fintrack.ui.theme.TealAccent
import com.example.fintrack.ui.theme.TealDark

@Composable
fun HomeScreen(
    viewModel: TransactionViewModel,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Int) -> Unit,
    onViewAllTransactions: () -> Unit
) {
    val balance by viewModel.currentBalance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val last7Days by viewModel.last7DaysExpenses.collectAsState()
    val streak by viewModel.noSpendStreak.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val recentTransactions = transactions.take(5)

    when (uiState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = PurplePrimary)
                    Text(
                        text = "Loading your finances...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(text = "⚠️", fontSize = 48.sp)
                    Text(
                        text = "Something went wrong",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = (uiState as UiState.Error).message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurplePrimary
                        )
                    ) {
                        Text("Try Again", color = Color.White)
                    }
                }
            }
        }

        is UiState.Success -> {
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
// ─── Savings Progress ─────────────────────────────────────────────────────
                item {
                    val savingsRate = if (totalIncome > 0)
                        ((balance / totalIncome) * 100).coerceIn(0.0, 100.0)
                    else 0.0

                    SurfaceCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Savings Progress",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "₹${String.format("%,.2f", balance)} saved of ₹${
                                        String.format("%,.2f", totalIncome)
                                    }",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "${savingsRate.toInt()}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TealAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(TealAccent.copy(alpha = 0.15f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((savingsRate / 100).toFloat())
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(TealAccent, TealDark)
                                        )
                                    )
                            )
                        }
                    }
                }
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
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onViewAllTransactions() }
                        )
                    }
                }

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

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

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