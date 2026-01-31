package ru.crmplatforma.solo.ui.subscriptions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.crmplatforma.solo.data.local.entity.SubscriptionPeriod

/**
 * SubscriptionEditorScreen — создание и редактирование подписки.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionEditorScreen(
    navController: NavController,
    subscriptionId: String? = null,
    viewModel: SubscriptionEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBillingDayPicker by remember { mutableStateOf(false) }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditMode) "Редактировать" else "Новая подписка") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = !isLoading && viewModel.isValid()
                    ) {
                        Text("Сохранить")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && viewModel.isEditMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Название
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.setName(it) },
                    label = { Text("Название *") },
                    placeholder = { Text("Аренда, Интернет, Netflix...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.name.isBlank(),
                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) }
                )

                // Сумма
                OutlinedTextField(
                    value = uiState.amountText,
                    onValueChange = { viewModel.setAmount(it) },
                    label = { Text("Сумма *") },
                    placeholder = { Text("15000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.amountText.isBlank(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    suffix = { Text("₽") }
                )

                // Периодичность
                Text(
                    text = "Периодичность",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.period == SubscriptionPeriod.MONTHLY,
                        onClick = { viewModel.setPeriod(SubscriptionPeriod.MONTHLY) },
                        label = { Text("Ежемесячно") },
                        leadingIcon = if (uiState.period == SubscriptionPeriod.MONTHLY) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null
                    )
                    FilterChip(
                        selected = uiState.period == SubscriptionPeriod.YEARLY,
                        onClick = { viewModel.setPeriod(SubscriptionPeriod.YEARLY) },
                        label = { Text("Ежегодно") },
                        leadingIcon = if (uiState.period == SubscriptionPeriod.YEARLY) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null
                    )
                }

                // День оплаты
                OutlinedCard(
                    onClick = { showBillingDayPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "День оплаты",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${uiState.billingDay} число",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }

                // Описание
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.setDescription(it) },
                    label = { Text("Примечание") },
                    placeholder = { Text("Дополнительная информация") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                HorizontalDivider()

                // Напоминания
                Text(
                    text = "Напоминания",
                    style = MaterialTheme.typography.titleMedium
                )

                ReminderSwitch(
                    label = "За 3 дня",
                    checked = uiState.remind3Days,
                    onCheckedChange = { viewModel.setRemind3Days(it) }
                )

                ReminderSwitch(
                    label = "За 1 день",
                    checked = uiState.remind1Day,
                    onCheckedChange = { viewModel.setRemind1Day(it) }
                )

                ReminderSwitch(
                    label = "В день оплаты",
                    checked = uiState.remindOnDay,
                    onCheckedChange = { viewModel.setRemindOnDay(it) }
                )

                // Кнопка удаления (только для редактирования)
                if (viewModel.isEditMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Удалить подписку")
                    }
                }
            }
        }
    }

    // Диалог выбора дня
    if (showBillingDayPicker) {
        BillingDayPickerDialog(
            currentDay = uiState.billingDay,
            onDaySelected = {
                viewModel.setBillingDay(it)
                showBillingDayPicker = false
            },
            onDismiss = { showBillingDayPicker = false }
        )
    }

    // Диалог подтверждения удаления
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Удалить подписку?") },
            text = { Text("Подписка будет удалена безвозвратно.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun ReminderSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun BillingDayPickerDialog(
    currentDay: Int,
    onDaySelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("День оплаты") },
        text = {
            Column {
                Text(
                    text = "Выберите число месяца для оплаты:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Сетка дней 1-31
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (rowStart in 1..31 step 7) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (day in rowStart until (rowStart + 7).coerceAtMost(32)) {
                                if (day <= 31) {
                                    FilterChip(
                                        selected = currentDay == day,
                                        onClick = { onDaySelected(day) },
                                        label = { Text(day.toString()) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Готово")
            }
        }
    )
}
