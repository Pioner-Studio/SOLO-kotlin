package ru.crmplatforma.solo.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ещё") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Profile header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Профиль",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Настройки аккаунта",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Menu items
            Text(
                text = "Модули",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            MenuItem(
                icon = Icons.AutoMirrored.Filled.List,
                title = "Услуги",
                subtitle = "Прайс-лист",
                onClick = { /* TODO */ }
            )
            MenuItem(
                icon = Icons.Default.CheckCircle,
                title = "Задачи",
                subtitle = "Дела и напоминания",
                onClick = { /* TODO */ }
            )
            MenuItem(
                icon = Icons.Default.CreditCard,
                title = "Подписки",
                subtitle = "Регулярные платежи",
                onClick = { /* TODO */ }
            )
            MenuItem(
                icon = Icons.Default.Inventory2,
                title = "Склад",
                subtitle = "Материалы",
                onClick = { /* TODO */ }
            )
            MenuItem(
                icon = Icons.Default.Warning,
                title = "ICE",
                subtitle = "Экстренная карточка",
                badgeText = "!",
                onClick = { /* TODO */ }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Настройки",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            MenuItem(
                icon = Icons.Default.Sync,
                title = "Синхронизация",
                subtitle = "Статус синхронизации",
                onClick = { /* TODO */ }
            )
            MenuItem(
                icon = Icons.Default.Notifications,
                title = "Уведомления",
                subtitle = "Настройки напоминаний",
                onClick = { /* TODO */ }
            )
            MenuItem(
                icon = Icons.Default.Info,
                title = "О приложении",
                subtitle = "Версия 1.0",
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badgeText != null) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(badgeText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
