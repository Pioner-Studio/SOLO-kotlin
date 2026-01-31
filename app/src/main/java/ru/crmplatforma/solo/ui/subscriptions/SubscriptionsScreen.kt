package ru.crmplatforma.solo.ui.subscriptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ru.crmplatforma.solo.data.local.entity.SubscriptionEntity
import ru.crmplatforma.solo.data.local.entity.SubscriptionPeriod
import ru.crmplatforma.solo.ui.Screen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * SubscriptionsScreen — список регулярных платежей.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    navController: NavController,
    viewModel: SubscriptionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Подписки") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleShowArchived() }) {
                        Icon(
                            if (uiState.showArchived) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (uiState.showArchived) "Скрыть неактивные" else "Показать неактивные"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.SubscriptionNew.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить подписку")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.subscriptions.isEmpty()) {
            EmptySubscriptionsState(
                onAddClick = { navController.navigate(Screen.SubscriptionNew.route) },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Сводка
                item {
                    SummaryCard(
                        monthlyTotalKopecks = uiState.monthlyTotalKopecks,
                        upcomingCount = uiState.upcomingCount
                    )
                }

                // Список подписок
                items(uiState.subscriptions, key = { it.id }) { subscription ->
                    SubscriptionCard(
                        subscription = subscription,
                        onClick = { navController.navigate(Screen.SubscriptionEdit.createRoute(subscription.id)) },
                        onMarkPaid = {
                            coroutineScope.launch {
                                viewModel.markPaid(subscription.id)
                            }
                        }
                    )
                }

                // Пространство для FAB
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    monthlyTotalKopecks: Long,
    upcomingCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatMoney(monthlyTotalKopecks),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "в месяц",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = upcomingCount.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (upcomingCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "на этой неделе",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SubscriptionCard(
    subscription: SubscriptionEntity,
    onClick: () -> Unit,
    onMarkPaid: () -> Unit
) {
    val today = LocalDate.now()
    val daysUntilPayment = ChronoUnit.DAYS.between(today, subscription.nextPaymentDate).toInt()

    val urgencyColor = when {
        daysUntilPayment <= 0 -> MaterialTheme.colorScheme.error
        daysUntilPayment <= 3 -> Color(0xFFF59E0B) // amber
        daysUntilPayment <= 7 -> Color(0xFF3B82F6) // blue
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (subscription.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка с цветом срочности
            Surface(
                shape = MaterialTheme.shapes.small,
                color = urgencyColor.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = urgencyColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Информация
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (!subscription.isActive) TextDecoration.LineThrough else null
                )

                Text(
                    text = formatMoney(subscription.amountKopecks) +
                           if (subscription.period == SubscriptionPeriod.YEARLY) " / год" else " / мес",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = urgencyColor
                    )
                    Text(
                        text = when {
                            daysUntilPayment < 0 -> "Просрочено"
                            daysUntilPayment == 0 -> "Сегодня"
                            daysUntilPayment == 1 -> "Завтра"
                            else -> subscription.nextPaymentDate.format(dateFormatter)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = urgencyColor
                    )
                }
            }

            // Кнопка "Оплачено"
            if (subscription.isActive) {
                FilledTonalIconButton(
                    onClick = onMarkPaid,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.15f),
                        contentColor = Color(0xFF10B981)
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Оплачено")
                }
            }
        }
    }
}

@Composable
private fun EmptySubscriptionsState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Нет подписок",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Добавьте регулярные платежи: аренда, интернет, сервисы. Приложение напомнит о дате оплаты.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить подписку")
            }
        }
    }
}

/**
 * Форматирование суммы (копейки → рубли).
 */
private fun formatMoney(kopecks: Long): String {
    val rubles = kopecks / 100
    val kop = kopecks % 100
    return if (kop > 0) {
        "$rubles,$kop ₽"
    } else {
        "$rubles ₽"
    }
}
