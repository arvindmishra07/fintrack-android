package com.example.fintrack.ui.streak

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fintrack.domain.model.TransactionType
import com.example.fintrack.ui.components.EmptyState
import com.example.fintrack.ui.components.SurfaceCard
import com.example.fintrack.ui.theme.*
import com.example.fintrack.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StreakScreen(viewModel: TransactionViewModel) {

    val allTransactions by viewModel.allTransactions.collectAsState()
    val currentStreak by viewModel.noSpendStreak.collectAsState()
    val bestStreak by viewModel.bestStreak.collectAsState()

    // ─── Build last 30 days calendar data ────────────────────────────────
    val calendarDays = remember(allTransactions) {
        val days = mutableListOf<CalendarDay>()
        for (i in 29 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            val dayEnd = dayStart + 24 * 60 * 60 * 1000L - 1

            val hadExpense = allTransactions.any {
                it.type == TransactionType.EXPENSE &&
                        it.date in dayStart..dayEnd
            }

            val isToday = i == 0
            val isFuture = false

            days.add(
                CalendarDay(
                    date = cal.time,
                    dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                    hasExpense = hadExpense,
                    isToday = isToday,
                    isFuture = isFuture
                )
            )
        }
        days
    }

    val noSpendDays = calendarDays.count { !it.hasExpense }
    val spendDays = calendarDays.count { it.hasExpense }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // ─── Header ───────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A1A2E),
                                Color(0xFF2D1B00)
                            )
                        )
                    )
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No-Spend Streak",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Track your spending-free days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── Main Streak Display ──────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        StreakFire.copy(alpha = 0.3f),
                                        AmberAccent.copy(alpha = 0.3f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = StreakFire.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Fire emoji with size based on streak
                            Text(
                                text = when {
                                    currentStreak == 0 -> "💤"
                                    currentStreak < 3 -> "🔥"
                                    currentStreak < 7 -> "🔥🔥"
                                    currentStreak < 14 -> "🔥🔥🔥"
                                    else -> "🔥🔥🔥🔥"
                                },
                                fontSize = 48.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "$currentStreak",
                                fontSize = 80.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (currentStreak > 0) StreakFire
                                else TextHint,
                                lineHeight = 80.sp
                            )

                            Text(
                                text = if (currentStreak == 1) "day streak"
                                else "day streak",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Motivational message
                            Text(
                                text = getMotivationalMessage(currentStreak),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // ─── Stats Row ────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StreakStatCard(
                    emoji = "🏆",
                    label = "Best Streak",
                    value = "$bestStreak days",
                    color = AmberAccent,
                    modifier = Modifier.weight(1f)
                )
                StreakStatCard(
                    emoji = "✅",
                    label = "No-Spend Days",
                    value = "$noSpendDays days",
                    color = NoSpendGreen,
                    modifier = Modifier.weight(1f)
                )
                StreakStatCard(
                    emoji = "💸",
                    label = "Spend Days",
                    value = "$spendDays days",
                    color = SpendRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ─── Calendar Heatmap ─────────────────────────────────────────────
        item {
            SurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Last 30 Days",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Green = No spend  •  Red = Spent money",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Day labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextHint,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    userScrollEnabled = false
                ) {
                    // Add offset for first day of month
                    val firstDayOffset = getFirstDayOffset(calendarDays)
                    items(firstDayOffset) {
                        Box(modifier = Modifier.size(36.dp))
                    }

                    items(calendarDays) { day ->
                        CalendarDayCell(day = day)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = NoSpendGreen, label = "No spend")
                    LegendDot(color = SpendRed, label = "Spent")
                    LegendDot(color = NeutralDay, label = "No data")
                }
            }
        }

        // ─── How it works ─────────────────────────────────────────────────
        item {
            SurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "How It Works",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                HowItWorksRow(
                    emoji = "📅",
                    title = "Track daily",
                    subtitle = "Every day without an expense counts as a no-spend day"
                )
                Spacer(modifier = Modifier.height(12.dp))
                HowItWorksRow(
                    emoji = "🔥",
                    title = "Build your streak",
                    subtitle = "Consecutive no-spend days build your streak counter"
                )
                Spacer(modifier = Modifier.height(12.dp))
                HowItWorksRow(
                    emoji = "💥",
                    title = "Streak resets",
                    subtitle = "Adding any expense for today resets your current streak"
                )
                Spacer(modifier = Modifier.height(12.dp))
                HowItWorksRow(
                    emoji = "🏆",
                    title = "Beat your best",
                    subtitle = "Try to beat your personal best streak every time"
                )
            }
        }

        // ─── Milestone Card ───────────────────────────────────────────────
        item {
            SurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Milestones",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    Triple(3, "🌱", "3 Day Starter"),
                    Triple(7, "⭐", "7 Day Warrior"),
                    Triple(14, "🏅", "14 Day Champion"),
                    Triple(30, "👑", "30 Day Legend")
                ).forEach { (target, emoji, label) ->
                    MilestoneRow(
                        emoji = emoji,
                        label = label,
                        target = target,
                        current = currentStreak,
                        best = bestStreak
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ─── Calendar Day Cell ────────────────────────────────────────────────────────
@Composable
fun CalendarDayCell(day: CalendarDay) {
    val bgColor = when {
        day.isToday && !day.hasExpense -> NoSpendGreen
        day.isToday && day.hasExpense -> SpendRed
        !day.hasExpense -> NoSpendGreen.copy(alpha = 0.4f)
        else -> SpendRed.copy(alpha = 0.4f)
    }

    val borderColor = when {
        day.isToday -> Color.White
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                width = if (day.isToday) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${day.dayOfMonth}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (day.isToday) FontWeight.ExtraBold
            else FontWeight.Normal,
            color = Color.White,
            fontSize = 10.sp
        )
    }
}

// ─── Streak Stat Card ─────────────────────────────────────────────────────────
@Composable
fun StreakStatCard(
    emoji: String,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Legend Dot ───────────────────────────────────────────────────────────────
@Composable
fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

// ─── How It Works Row ─────────────────────────────────────────────────────────
@Composable
fun HowItWorksRow(emoji: String, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceVariantLight),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

// ─── Milestone Row ────────────────────────────────────────────────────────────
@Composable
fun MilestoneRow(
    emoji: String,
    label: String,
    target: Int,
    current: Int,
    best: Int
) {
    val isAchieved = best >= target
    val isActive = current >= target

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isAchieved) AmberAccent.copy(alpha = 0.2f)
                    else NeutralDay
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isAchieved) emoji else "🔒",
                fontSize = 20.sp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isAchieved) TextPrimary else TextSecondary
            )
            Text(
                text = "$target day streak required",
                style = MaterialTheme.typography.bodySmall,
                color = TextHint
            )
        }

        if (isAchieved) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AmberAccent.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Achieved ✓",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = "${target - current} more",
                style = MaterialTheme.typography.labelSmall,
                color = TextHint
            )
        }
    }
}

// ─── Data class ───────────────────────────────────────────────────────────────
data class CalendarDay(
    val date: Date,
    val dayOfMonth: Int,
    val hasExpense: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean
)

// ─── Helpers ──────────────────────────────────────────────────────────────────
fun getMotivationalMessage(streak: Int): String {
    return when {
        streak == 0 -> "Start today — every journey begins with one step 💪"
        streak == 1 -> "Great start! One day down, keep it going!"
        streak < 3 -> "You're building momentum. Don't stop now!"
        streak < 7 -> "Amazing! You're on a roll 🎯"
        streak < 14 -> "One week strong! You're crushing it 💥"
        streak < 30 -> "Two weeks? You're a saving machine! 🚀"
        else -> "Legendary! You are the master of your finances 👑"
    }
}

fun getFirstDayOffset(days: List<CalendarDay>): Int {
    if (days.isEmpty()) return 0
    val cal = Calendar.getInstance()
    cal.time = days.first().date
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> 0
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        else -> 0
    }
}