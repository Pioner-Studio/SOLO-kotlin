package ru.crmplatforma.solo.ui.ice

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

/**
 * ICEEditorScreen — редактирование экстренной карточки.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ICEEditorScreen(
    navController: NavController,
    viewModel: ICEEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var showBloodTypePicker by remember { mutableStateOf(false) }

    val iceRed = Color(0xFFDC2626)

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать ICE") },
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = iceRed,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
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
                // === Секция: О себе ===
                Text(
                    text = "О себе",
                    style = MaterialTheme.typography.titleMedium,
                    color = iceRed
                )

                OutlinedTextField(
                    value = uiState.ownerName,
                    onValueChange = { viewModel.setOwnerName(it) },
                    label = { Text("ФИО *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.ownerName.isBlank(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )

                // Группа крови
                OutlinedCard(
                    onClick = { showBloodTypePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Bloodtype,
                            contentDescription = null,
                            tint = iceRed
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Группа крови",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = uiState.ownerBloodType.ifBlank { "Не указана" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }

                OutlinedTextField(
                    value = uiState.ownerAllergies,
                    onValueChange = { viewModel.setOwnerAllergies(it) },
                    label = { Text("Аллергии") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text("Пенициллин, орехи, пыльца...") },
                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = iceRed) }
                )

                OutlinedTextField(
                    value = uiState.ownerMedications,
                    onValueChange = { viewModel.setOwnerMedications(it) },
                    label = { Text("Принимаемые препараты") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text("Инсулин, аспирин...") }
                )

                OutlinedTextField(
                    value = uiState.ownerMedicalNotes,
                    onValueChange = { viewModel.setOwnerMedicalNotes(it) },
                    label = { Text("Медицинские особенности") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text("Диабет, астма, кардиостимулятор...") }
                )

                HorizontalDivider()

                // === Контакт 1 (обязательный) ===
                Text(
                    text = "Экстренный контакт 1 *",
                    style = MaterialTheme.typography.titleMedium,
                    color = iceRed
                )

                ContactFields(
                    name = uiState.contact1Name,
                    onNameChange = { viewModel.setContact1Name(it) },
                    phone = uiState.contact1Phone,
                    onPhoneChange = { viewModel.setContact1Phone(it) },
                    relation = uiState.contact1Relation,
                    onRelationChange = { viewModel.setContact1Relation(it) },
                    isRequired = true
                )

                HorizontalDivider()

                // === Контакт 2 (опциональный) ===
                Text(
                    text = "Экстренный контакт 2",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ContactFields(
                    name = uiState.contact2Name,
                    onNameChange = { viewModel.setContact2Name(it) },
                    phone = uiState.contact2Phone,
                    onPhoneChange = { viewModel.setContact2Phone(it) },
                    relation = uiState.contact2Relation,
                    onRelationChange = { viewModel.setContact2Relation(it) }
                )

                HorizontalDivider()

                // === Контакт 3 (опциональный) ===
                Text(
                    text = "Экстренный контакт 3",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ContactFields(
                    name = uiState.contact3Name,
                    onNameChange = { viewModel.setContact3Name(it) },
                    phone = uiState.contact3Phone,
                    onPhoneChange = { viewModel.setContact3Phone(it) },
                    relation = uiState.contact3Relation,
                    onRelationChange = { viewModel.setContact3Relation(it) }
                )

                // Удаление
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { viewModel.delete() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Удалить карточку")
                }
            }
        }
    }

    // Blood Type Picker
    if (showBloodTypePicker) {
        AlertDialog(
            onDismissRequest = { showBloodTypePicker = false },
            title = { Text("Группа крови") },
            text = {
                Column {
                    bloodTypes.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.ownerBloodType == type,
                                onClick = {
                                    viewModel.setOwnerBloodType(type)
                                    showBloodTypePicker = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(type)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBloodTypePicker = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun ContactFields(
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    relation: String,
    onRelationChange: (String) -> Unit,
    isRequired: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(if (isRequired) "Имя *" else "Имя") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = isRequired && name.isBlank()
            )
            OutlinedTextField(
                value = relation,
                onValueChange = onRelationChange,
                label = { Text("Кто это") },
                modifier = Modifier.weight(0.6f),
                singleLine = true,
                placeholder = { Text("Мама") }
            )
        }

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text(if (isRequired) "Телефон *" else "Телефон") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = isRequired && phone.isBlank(),
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
        )
    }
}
