package ru.crmplatforma.solo.ui.services

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

/**
 * ServiceEditorScreen — создание и редактирование услуги.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceEditorScreen(
    navController: NavController,
    serviceId: String? = null,
    viewModel: ServiceEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    LaunchedEffect(serviceId) {
        if (serviceId != null) {
            viewModel.loadService(serviceId)
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (serviceId == null) "Новая услуга" else "Редактировать")
                },
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
        if (isLoading) {
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
                    label = { Text("Название услуги *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
                    singleLine = true,
                    isError = uiState.name.isBlank()
                )

                // Цена
                OutlinedTextField(
                    value = uiState.priceRubles,
                    onValueChange = { viewModel.setPrice(it) },
                    label = { Text("Цена, ₽ *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = uiState.priceRubles.isBlank()
                )

                // Длительность
                OutlinedTextField(
                    value = uiState.durationMinutes,
                    onValueChange = { viewModel.setDuration(it) },
                    label = { Text("Длительность, мин *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = uiState.durationMinutes.isBlank()
                )

                // Быстрый выбор длительности
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(30, 45, 60, 90, 120).forEach { minutes ->
                        FilterChip(
                            selected = uiState.durationMinutes == minutes.toString(),
                            onClick = { viewModel.setDuration(minutes.toString()) },
                            label = { Text("$minutes") }
                        )
                    }
                }

                HorizontalDivider()

                // Описание
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.setDescription(it) },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    placeholder = { Text("Что входит в услугу...") }
                )

                // Архивация (только при редактировании)
                if (serviceId != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.isArchived) {
                        // Разархивировать
                        OutlinedButton(
                            onClick = { viewModel.unarchive() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Unarchive, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Восстановить из архива")
                        }
                    } else {
                        // Архивировать
                        OutlinedButton(
                            onClick = { viewModel.archive() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("В архив")
                        }

                        Text(
                            text = "Услуга останется в истории записей, но не будет доступна для выбора",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
