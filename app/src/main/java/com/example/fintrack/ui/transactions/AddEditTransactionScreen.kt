package com.example.fintrack.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fintrack.domain.model.Transaction
import com.example.fintrack.domain.model.TransactionCategory
import com.example.fintrack.domain.model.TransactionType
import com.example.fintrack.ui.components.getCategoryColor
import com.example.fintrack.ui.theme.*
import com.example.fintrack.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    viewModel: TransactionViewModel,
    existingTransaction: Transaction? = null,
    onNavigateBack: () -> Unit
) {
    val isEditing = existingTransaction != null

    // ─── Form State ───────────────────────────────────────────────────────
    var amount by remember {
        mutableStateOf(existingTransaction?.amount?.toString() ?: "")
    }
    var selectedType by remember {
        mutableStateOf(existingTransaction?.type ?: TransactionType.EXPENSE)
    }
    var selectedCategory by remember {
        mutableStateOf(existingTransaction?.category ?: TransactionCategory.FOOD)
    }
    var note by remember {
        mutableStateOf(existingTransaction?.note ?: "")
    }
    var selectedDate by remember {
        mutableStateOf(existingTransaction?.date ?: System.currentTimeMillis())
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    // ─── Category lists ───────────────────────────────────────────────────
    val expenseCategories = listOf(
        TransactionCategory.FOOD,
        TransactionCategory.TRANSPORT,
        TransactionCategory.SHOPPING,
        TransactionCategory.ENTERTAINMENT,
        TransactionCategory.HEALTH,
        TransactionCategory.BILLS,
        TransactionCategory.EDUCATION,
        TransactionCategory.OTHER
    )

    val incomeCategories = listOf(
        TransactionCategory.SALARY,
        TransactionCategory.FREELANCE,
        TransactionCategory.INVESTMENT,
        TransactionCategory.GIFT
    )

    val currentCategories = if (selectedType == TransactionType.EXPENSE)
        expenseCategories else incomeCategories

    // Reset category when type changes
    LaunchedEffect(selectedType) {
        selectedCategory = if (selectedType == TransactionType.EXPENSE)
            TransactionCategory.FOOD else TransactionCategory.SALARY
    }

    // ─── Date Picker ──────────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = it
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = PurplePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // ─── Top Bar ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientDarkStart, GradientDarkEnd)
                    )
                )
                .padding(top = 48.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isEditing) "Edit Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = if (isEditing) "Update your transaction details"
                    else "Record your income or expense",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ─── Type Toggle ──────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Transaction Type",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TransactionTypeButton(
                            label = "Expense",
                            emoji = "📉",
                            selected = selectedType == TransactionType.EXPENSE,
                            selectedColor = ExpenseRed,
                            onClick = { selectedType = TransactionType.EXPENSE },
                            modifier = Modifier.weight(1f)
                        )
                        TransactionTypeButton(
                            label = "Income",
                            emoji = "📈",
                            selected = selectedType == TransactionType.INCOME,
                            selectedColor = IncomeGreen,
                            onClick = { selectedType = TransactionType.INCOME },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ─── Amount Input ─────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            amountError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("0.00", color = TextHint)
                        },
                        prefix = {
                            Text(
                                "₹ ",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 18.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        isError = amountError,
                        supportingText = if (amountError) {
                            { Text("Please enter a valid amount") }
                        } else null,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = SurfaceVariantLight,
                            unfocusedContainerColor = SurfaceVariantLight
                        )
                    )
                }
            }

            // ─── Category Picker ──────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(currentCategories) { category ->
                            CategoryChip(
                                category = category,
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                }
            }

            // ─── Date Picker ──────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = SimpleDateFormat(
                                "dd MMMM yyyy", Locale.getDefault()
                            ).format(Date(selectedDate)),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Pick date",
                        tint = PurplePrimary
                    )
                }
            }

            // ─── Note Input ───────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Note (Optional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Add a note...", color = TextHint)
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = SurfaceVariantLight,
                            unfocusedContainerColor = SurfaceVariantLight
                        )
                    )
                }
            }

            // ─── Save Button ──────────────────────────────────────────────
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue == null || amountValue <= 0) {
                        amountError = true
                        return@Button
                    }

                    val transaction = Transaction(
                        id = existingTransaction?.id ?: 0,
                        amount = amountValue,
                        type = selectedType,
                        category = selectedCategory,
                        date = selectedDate,
                        note = note.trim()
                    )

                    if (isEditing) {
                        viewModel.updateTransaction(transaction)
                    } else {
                        viewModel.insertTransaction(transaction)
                    }

                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurplePrimary
                )
            ) {
                Text(
                    text = if (isEditing) "Update Transaction" else "Save Transaction",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ─── Type Button ──────────────────────────────────────────────────────────────
@Composable
fun TransactionTypeButton(
    label: String,
    emoji: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) selectedColor.copy(alpha = 0.15f)
                else SurfaceVariantLight
            )
            .border(
                width = 2.dp,
                color = if (selected) selectedColor else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) selectedColor else TextSecondary
            )
        }
    }
}

// ─── Category Chip ────────────────────────────────────────────────────────────
@Composable
fun CategoryChip(
    category: TransactionCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = getCategoryColor(category)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (selected) color.copy(alpha = 0.25f)
                    else SurfaceVariantLight
                )
                .border(
                    width = 2.dp,
                    color = if (selected) color else Color.Transparent,
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.emoji, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) color else TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}