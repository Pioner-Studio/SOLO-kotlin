package ru.crmplatforma.solo.ui.services

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.crmplatforma.solo.data.local.entity.ServiceEntity
import ru.crmplatforma.solo.ui.Screen

/**
 * ServicesScreen — список услуг.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    navController: NavController,
    viewModel: ServicesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Услуги") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.ServiceNew.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить услугу")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Поиск
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Поиск услуги") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                },
                singleLine = true
            )

            // Переключатель архива
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = !uiState.showArchived,
                    onClick = { viewModel.setShowArchived(false) },
                    label = { Text("Активные") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = uiState.showArchived,
                    onClick = { viewModel.setShowArchived(true) },
                    label = { Text("Архив") },
                    leadingIcon = if (uiState.showArchived) {
                        { Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }

            // Список
            if (uiState.services.isEmpty()) {
                EmptyServicesState(
                    isArchive = uiState.showArchived,
                    hasSearch = uiState.searchQuery.isNotBlank(),
                    onAddService = { navController.navigate(Screen.ServiceNew.route) }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.services, key = { it.id }) { service ->
                        ServiceCard(
                            service = service,
                            onClick = {
                                navController.navigate(Screen.ServiceEdit.createRoute(service.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(
    service: ServiceEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (service.isArchived) TextDecoration.LineThrough else null
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "${service.priceKopecks / 100} ₽",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${service.durationMinutes} мин",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                service.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            if (service.isArchived) {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = "В архиве",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyServicesState(
    isArchive: Boolean,
    hasSearch: Boolean,
    onAddService: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = when {
                    hasSearch -> Icons.Default.SearchOff
                    isArchive -> Icons.Default.Archive
                    else -> Icons.Default.ContentCut
                },
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when {
                    hasSearch -> "Ничего не найдено"
                    isArchive -> "Архив пуст"
                    else -> "Нет услуг"
                },
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    hasSearch -> "Попробуйте изменить запрос"
                    isArchive -> "Архивированные услуги появятся здесь"
                    else -> "Добавьте услуги для быстрого расчёта стоимости"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isArchive && !hasSearch) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onAddService) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить услугу")
                }
            }
        }
    }
}
