package com.example.fintrack.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import com.example.fintrack.ui.components.TransactionItem
import com.example.fintrack.ui.components.getCategoryColor
import com.example.fintrack.ui.theme.*
import com.example.fintrack.viewmodel.TransactionViewModel

@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Int) -> Unit
) {
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val hasActiveFilters = searchQuery.isNotEmpty() ||
            selectedType != null ||
            selectedCategory != null

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
        ) {
            // ─── Header ───────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(GradientDarkStart, GradientDarkEnd)
                            )
                        )
                        .padding(
                            top = 48.dp,
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 24.dp
                        )
                ) {
                    Column {
                        Text(
                            text = "Transactions",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = "${filteredTransactions.size} entries found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Search transactions...",
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.setSearchQuery("") }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurpleLight,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            }

            // ─── Type Filter ──────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Filter by Type",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // All filter
                        FilterChip(
                            selected = selectedType == null,
                            onClick = { viewModel.setTypeFilter(null) },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurplePrimary,
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = selectedType == TransactionType.INCOME,
                            onClick = {
                                viewModel.setTypeFilter(
                                    if (selectedType == TransactionType.INCOME)
                                        null else TransactionType.INCOME
                                )
                            },
                            label = { Text("📈 Income") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IncomeGreen,
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = selectedType == TransactionType.EXPENSE,
                            onClick = {
                                viewModel.setTypeFilter(
                                    if (selectedType == TransactionType.EXPENSE)
                                        null else TransactionType.EXPENSE
                                )
                            },
                            label = { Text("📉 Expense") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ExpenseRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // ─── Category Filter ──────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Filter by Category",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        // All categories option
                        item {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (selectedCategory == null)
                                            PurplePrimary
                                        else SurfaceVariantLight
                                    )
                                    .clickable { viewModel.setCategoryFilter(null) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "All",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selectedCategory == null)
                                        Color.White else TextSecondary
                                )
                            }
                        }

                        items(TransactionCategory.values().toList()) { category ->
                            val color = getCategoryColor(category)
                            val isSelected = selectedCategory == category

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) color
                                        else color.copy(alpha = 0.12f)
                                    )
                                    .clickable {
                                        viewModel.setCategoryFilter(
                                            if (isSelected) null else category
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = category.emoji,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected)
                                            Color.White else color,
                                        fontWeight = if (isSelected)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ─── Clear Filters Button ─────────────────────────────────────
            if (hasActiveFilters) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { viewModel.clearFilters() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clear all filters",
                                color = ExpenseRed,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // ─── Divider ──────────────────────────────────────────────────
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(BackgroundLight)
                )
            }

            // ─── Transaction List ─────────────────────────────────────────
            if (filteredTransactions.isEmpty()) {
                item {
                    EmptyState(
                        emoji = if (hasActiveFilters) "🔍" else "💸",
                        title = if (hasActiveFilters)
                            "No results found"
                        else
                            "No transactions yet",
                        subtitle = if (hasActiveFilters)
                            "Try adjusting your search or filters"
                        else
                            "Tap the + button to record your first transaction",
                        modifier = Modifier.padding(vertical = 48.dp)
                    )
                }
            } else {
                items(
                    items = filteredTransactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onEdit = { onEditTransaction(it.id) },
                        onDelete = { viewModel.deleteTransaction(it) }
                    )
                }
            }

            // FAB space
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // ─── FAB ──────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = onAddTransaction,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = PurplePrimary,
            contentColor = Color.White
        ) {
            Text(text = "+", fontSize = 28.sp, fontWeight = FontWeight.Light)
        }
    }
}