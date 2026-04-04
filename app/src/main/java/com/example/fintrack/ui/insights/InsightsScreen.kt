package com.example.fintrack.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.example.fintrack.domain.model.TransactionCategory
import com.example.fintrack.domain.model.TransactionType
import com.example.fintrack.ui.components.EmptyState
import com.example.fintrack.ui.components.SurfaceCard
import com.example.fintrack.ui.components.getCategoryColor
import com.example.fintrack.ui.theme.*
import com.example.fintrack.viewmodel.TransactionViewModel

@Composable
fun InsightsScreen(viewModel: TransactionViewModel) {

    val allTransactions by viewModel.allTransactions.collectAsState()
    val expenseByCategory by viewModel.expenseByCategory.collectAsState()
    val thisWeek by viewModel.thisWeekExpenses.collectAsState()
    val lastWeek by viewModel.lastWeekExpenses.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val last7Days by viewModel.last7DaysExpenses.collectAsState()

    val topCategory = expenseByCategory.maxByOrNull { it.value }
    val savingsRate = if (totalIncome > 0)
        ((totalIncome - totalExpense) / totalIncome * 100).coerceIn(0.0, 100.0)
    else 0.0

    val weekChange = if (lastWeek > 0)
        ((thisWeek - lastWeek) / lastWeek * 100)
    else 0.0

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
                    Text(
                        text = "Insights",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Understand your spending patterns",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ─── Quick Stats Row ──────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickStatCard(
                            emoji = "💰",
                            label = "Savings Rate",
                            value = "${String.format("%.1f", savingsRate)}%",
                            color = TealAccent,
                            modifier = Modifier.weight(1f)
                        )
                        QuickStatCard(
                            emoji = "📊",
                            label = "Total Entries",
                            value = "${allTransactions.size}",
                            color = PurpleLight,
                            modifier = Modifier.weight(1f)
                        )
                        QuickStatCard(
                            emoji = "🏷️",
                            label = "Categories",
                            value = "${expenseByCategory.size}",
                            color = AmberAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ─── Empty State ──────────────────────────────────────────────────
        if (allTransactions.isEmpty()) {
            item {
                EmptyState(
                    emoji = "📊",
                    title = "No data yet",
                    subtitle = "Add some transactions to see your spending insights",
                    modifier = Modifier.padding(vertical = 80.dp)
                )
            }
            return@LazyColumn
        }

        // ─── Week Comparison ──────────────────────────────────────────────
        item {
            SurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Week Comparison",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WeekCard(
                        label = "This Week",
                        amount = thisWeek,
                        color = PurplePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    WeekCard(
                        label = "Last Week",
                        amount = lastWeek,
                        color = TealAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Change indicator
                val isIncrease = weekChange > 0
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isIncrease)
                                ExpenseRedLight
                            else
                                IncomeGreenLight
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isIncrease) "📈" else "📉",
                            fontSize = 20.sp
                        )
                        Column {
                            Text(
                                text = if (lastWeek == 0.0) "No data for last week"
                                else "${String.format("%.1f", Math.abs(weekChange))}% " +
                                        "${if (isIncrease) "more" else "less"} than last week",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isIncrease) ExpenseRed else IncomeGreen
                            )
                            Text(
                                text = if (isIncrease)
                                    "Try to cut back on spending"
                                else
                                    "Great job keeping expenses down!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // ─── Top Spending Category ─────────────────────────────────────────
        topCategory?.let { (category, amount) ->
            item {
                SurfaceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Top Spending Category",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    getCategoryColor(category).copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = category.emoji, fontSize = 32.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Highest expense category",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Text(
                            text = "₹${String.format("%,.2f", amount)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = ExpenseRed
                        )
                    }
                }
            }
        }

        // ─── Spending by Category ─────────────────────────────────────────
        if (expenseByCategory.isNotEmpty()) {
            item {
                SurfaceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Spending by Category",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val totalExpenses = expenseByCategory.values.sum()
                        .takeIf { it > 0 } ?: 1.0

                    val sorted = expenseByCategory.entries
                        .sortedByDescending { it.value }

                    sorted.forEach { (category, amount) ->
                        CategoryProgressRow(
                            category = category,
                            amount = amount,
                            percentage = (amount / totalExpenses * 100).toFloat()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // ─── Income vs Expense Summary ────────────────────────────────────
        item {
            SurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Income vs Expense",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                val total = (totalIncome + totalExpense).takeIf { it > 0 } ?: 1.0
                val incomeRatio = (totalIncome / total).toFloat()
                val expenseRatio = (totalExpense / total).toFloat()

                // Combined bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    if (incomeRatio > 0) {
                        Box(
                            modifier = Modifier
                                .weight(incomeRatio)
                                .fillMaxHeight()
                                .background(IncomeGreen)
                        )
                    }
                    if (expenseRatio > 0) {
                        Box(
                            modifier = Modifier
                                .weight(expenseRatio)
                                .fillMaxHeight()
                                .background(ExpenseRed)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem(
                        color = IncomeGreen,
                        label = "Income",
                        amount = totalIncome
                    )
                    LegendItem(
                        color = ExpenseRed,
                        label = "Expense",
                        amount = totalExpense
                    )
                }
            }
        }

        // ─── Savings Rate Card ────────────────────────────────────────────
        item {
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
                            text = "Savings Rate",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Of your income saved",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { (savingsRate / 100).toFloat() },
                            modifier = Modifier.size(80.dp),
                            color = TealAccent,
                            trackColor = TealAccent.copy(alpha = 0.15f),
                            strokeWidth = 8.dp
                        )
                        Text(
                            text = "${savingsRate.toInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TealAccent
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ─── Quick Stat Card ──────────────────────────────────────────────────────────
@Composable
fun QuickStatCard(
    emoji: String,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// ─── Week Card ────────────────────────────────────────────────────────────────
@Composable
fun WeekCard(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₹${String.format("%,.2f", amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }
    }
}

// ─── Category Progress Row ────────────────────────────────────────────────────
@Composable
fun CategoryProgressRow(
    category: TransactionCategory,
    amount: Double,
    percentage: Float
) {
    val color = getCategoryColor(category)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = category.emoji, fontSize = 16.sp)
                }
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "₹${String.format("%,.0f", amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${String.format("%.1f", percentage)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

// ─── Legend Item ──────────────────────────────────────────────────────────────
@Composable
fun LegendItem(
    color: Color,
    label: String,
    amount: Double
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = "₹${String.format("%,.2f", amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}