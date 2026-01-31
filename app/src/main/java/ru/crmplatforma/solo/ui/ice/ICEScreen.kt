package ru.crmplatforma.solo.ui.ice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.crmplatforma.solo.data.local.entity.ICEContactEntity
import ru.crmplatforma.solo.ui.Screen

/**
 * ICEScreen — экстренная карточка для врачей/спасателей.
 *
 * Отображается крупным текстом, высококонтрастно.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ICEScreen(
    navController: NavController,
    viewModel: ICEViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Красная тема для экстренной информации
    val iceRed = Color(0xFFDC2626)
    val iceRedLight = Color(0xFFFEE2E2)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ICE — Экстренная карточка") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(Screen.ICEEdit.route) }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!uiState.hasData) {
            EmptyICEState(
                onCreateICE = { navController.navigate(Screen.ICEEdit.route) },
                modifier = Modifier.padding(padding)
            )
        } else {
            val ice = uiState.ice!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(iceRedLight)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Владелец
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = ice.ownerName,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )

                        if (ice.ownerBloodType != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = iceRed
                                ) {
                                    Text(
                                        text = ice.ownerBloodType,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                                Text(
                                    text = "Группа крови",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Аллергии (ВАЖНО!)
                if (!ice.ownerAllergies.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = iceRed)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "АЛЛЕРГИИ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = ice.ownerAllergies,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Препараты
                if (!ice.ownerMedications.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Принимаемые препараты",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ice.ownerMedications,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Медицинские заметки
                if (!ice.ownerMedicalNotes.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Медицинские особенности",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ice.ownerMedicalNotes,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Экстренные контакты
                Text(
                    text = "ЭКСТРЕННЫЕ КОНТАКТЫ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = iceRed
                )

                // Контакт 1
                ContactCard(
                    name = ice.contact1Name,
                    phone = ice.contact1Phone,
                    relation = ice.contact1Relation,
                    iceRed = iceRed,
                    onCall = { viewModel.callPhone(ice.contact1Phone) }
                )

                // Контакт 2
                if (!ice.contact2Name.isNullOrBlank() && !ice.contact2Phone.isNullOrBlank()) {
                    ContactCard(
                        name = ice.contact2Name,
                        phone = ice.contact2Phone,
                        relation = ice.contact2Relation,
                        iceRed = iceRed,
                        onCall = { viewModel.callPhone(ice.contact2Phone) }
                    )
                }

                // Контакт 3
                if (!ice.contact3Name.isNullOrBlank() && !ice.contact3Phone.isNullOrBlank()) {
                    ContactCard(
                        name = ice.contact3Name,
                        phone = ice.contact3Phone,
                        relation = ice.contact3Relation,
                        iceRed = iceRed,
                        onCall = { viewModel.callPhone(ice.contact3Phone) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactCard(
    name: String,
    phone: String,
    relation: String?,
    iceRed: Color,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (relation != null) {
                    Text(
                        text = relation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = phone,
                    style = MaterialTheme.typography.bodyLarge,
                    color = iceRed
                )
            }

            FilledTonalButton(
                onClick = onCall,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = iceRed,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Phone, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Позвонить")
            }
        }
    }
}

@Composable
private fun EmptyICEState(
    onCreateICE: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iceRed = Color(0xFFDC2626)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = iceRed
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ICE не заполнена",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Заполните экстренную карточку на случай ЧП. Врачи и спасатели смогут быстро связаться с вашими близкими.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCreateICE,
                colors = ButtonDefaults.buttonColors(containerColor = iceRed)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать карточку")
            }
        }
    }
}
