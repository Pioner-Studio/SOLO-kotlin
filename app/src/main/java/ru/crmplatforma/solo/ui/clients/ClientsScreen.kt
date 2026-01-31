package ru.crmplatforma.solo.ui.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.crmplatforma.solo.data.local.entity.ClientEntity
import ru.crmplatforma.solo.ui.Screen
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * ClientsScreen — список клиентов с поиском и фильтрами.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    navController: NavController,
    viewModel: ClientsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Клиенты")
                        if (uiState.totalCount > 0) {
                            Text(
                                text = "${uiState.totalCount} ${pluralize(uiState.totalCount, "клиент", "клиента", "клиентов")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.ClientNew.route) }
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Добавить клиента")
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
                placeholder = { Text("Поиск по имени или телефону") },
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

            // Фильтры
            FilterChipsRow(
                selectedFilter = uiState.filter,
                onFilterChange = { viewModel.setFilter(it) }
            )

            // Список или Empty State
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.clients.isEmpty()) {
                EmptyClientsState(
                    hasSearch = uiState.searchQuery.isNotBlank(),
                    filter = uiState.filter,
                    onAddClient = { navController.navigate(Screen.ClientNew.route) },
                    onClearSearch = { viewModel.clearSearch() }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.clients, key = { it.id }) { client ->
                        ClientCard(
                            client = client,
                            onClick = {
                                navController.navigate(Screen.ClientEdit.createRoute(client.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: ClientFilter,
    onFilterChange: (ClientFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == ClientFilter.ALL,
            onClick = { onFilterChange(ClientFilter.ALL) },
            label = { Text("Все") }
        )
        FilterChip(
            selected = selectedFilter == ClientFilter.VIP,
            onClick = { onFilterChange(ClientFilter.VIP) },
            label = { Text("VIP") },
            leadingIcon = if (selectedFilter == ClientFilter.VIP) {
                { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else null
        )
        FilterChip(
            selected = selectedFilter == ClientFilter.BIRTHDAY,
            onClick = { onFilterChange(ClientFilter.BIRTHDAY) },
            label = { Text("ДР скоро") },
            leadingIcon = if (selectedFilter == ClientFilter.BIRTHDAY) {
                { Icon(Icons.Default.Cake, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else null
        )
    }
}

@Composable
private fun ClientCard(
    client: ClientEntity,
    onClick: () -> Unit
) {
    val birthdayFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))

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
            // Аватар
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = if (client.isVip)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (client.isVip) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "VIP",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = client.name.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Информация
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = client.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (client.isVip) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "VIP",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                client.phone?.let { phone ->
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                client.birthday?.let { birthday ->
                    Text(
                        text = "ДР: ${birthday.format(birthdayFormatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Статистика
            if (client.totalVisits > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${client.totalVisits}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = pluralize(client.totalVisits, "визит", "визита", "визитов"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
private fun EmptyClientsState(
    hasSearch: Boolean,
    filter: ClientFilter,
    onAddClient: () -> Unit,
    onClearSearch: () -> Unit
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
                    filter == ClientFilter.VIP -> Icons.Default.Star
                    filter == ClientFilter.BIRTHDAY -> Icons.Default.Cake
                    else -> Icons.Default.People
                },
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when {
                    hasSearch -> "Ничего не найдено"
                    filter == ClientFilter.VIP -> "Нет VIP клиентов"
                    filter == ClientFilter.BIRTHDAY -> "Нет ближайших ДР"
                    else -> "Нет клиентов"
                },
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    hasSearch -> "Попробуйте изменить запрос"
                    filter == ClientFilter.VIP -> "Отметьте клиента как VIP"
                    filter == ClientFilter.BIRTHDAY -> "Добавьте дату рождения клиентам"
                    else -> "Добавьте первого клиента"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (hasSearch) {
                OutlinedButton(onClick = onClearSearch) {
                    Text("Очистить поиск")
                }
            } else if (filter == ClientFilter.ALL) {
                Button(onClick = onAddClient) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить клиента")
                }
            }
        }
    }
}

/**
 * Склонение слов в зависимости от числа.
 */
private fun pluralize(count: Int, one: String, few: String, many: String): String {
    val n = count % 100
    return when {
        n in 11..19 -> many
        n % 10 == 1 -> one
        n % 10 in 2..4 -> few
        else -> many
    }
}
