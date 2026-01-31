package ru.crmplatforma.solo.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

/**
 * OnboardingScreen — 4 шага за 60 секунд.
 *
 * 1. Welcome — приветствие
 * 2. Specialization — выбор специализации
 * 3. Notifications — разрешение на уведомления
 * 4. Ready — готово, переход к Dashboard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    val selectedSpecialization by viewModel.selectedSpecialization.collectAsState()

    // Launcher для запроса разрешения на уведомления
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setNotificationsEnabled(isGranted)
        // Переходим на следующую страницу независимо от результата
        scope.launch {
            pagerState.animateScrollToPage(3)
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = false // Скролл только кнопками
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> SpecializationPage(
                        selected = selectedSpecialization,
                        onSelect = { viewModel.selectSpecialization(it) }
                    )
                    2 -> NotificationsPage()
                    3 -> ReadyPage()
                }
            }

            // Индикаторы страниц
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            // Кнопки навигации
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Кнопка "Назад"
                if (pagerState.currentPage > 0 && pagerState.currentPage < 3) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Text("Назад")
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                // Кнопка "Далее" / "Готово"
                Button(
                    onClick = {
                        when (pagerState.currentPage) {
                            0 -> {
                                scope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            }
                            1 -> {
                                if (selectedSpecialization != null) {
                                    scope.launch {
                                        pagerState.animateScrollToPage(2)
                                    }
                                }
                            }
                            2 -> {
                                // Запрашиваем разрешение на уведомления (Android 13+)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setNotificationsEnabled(true)
                                    scope.launch {
                                        pagerState.animateScrollToPage(3)
                                    }
                                }
                            }
                            3 -> {
                                viewModel.completeOnboarding()
                                onComplete()
                            }
                        }
                    },
                    enabled = pagerState.currentPage != 1 || selectedSpecialization != null
                ) {
                    Text(
                        if (pagerState.currentPage == 3) "Начать" else "Далее"
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    OnboardingPage(
        icon = Icons.Default.Favorite,
        title = "Добро пожаловать в SOLO!",
        description = "Ваш персональный помощник для ведения бизнеса. Записи, клиенты, финансы — всё в одном месте."
    )
}

@Composable
private fun SpecializationPage(
    selected: Specialization?,
    onSelect: (Specialization) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Work,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ваша специализация",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Выберите сферу деятельности для персонализации",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Выбор специализации
        Specialization.entries.forEach { spec ->
            Card(
                onClick = { onSelect(spec) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected == spec)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        spec.icon,
                        contentDescription = null,
                        tint = if (selected == spec)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = spec.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = spec.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (selected == spec) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsPage() {
    OnboardingPage(
        icon = Icons.Default.Notifications,
        title = "Уведомления",
        description = "Разрешите уведомления, чтобы не пропустить записи и важные события. Вы сможете настроить их позже."
    )
}

@Composable
private fun ReadyPage() {
    OnboardingPage(
        icon = Icons.Default.Rocket,
        title = "Всё готово!",
        description = "Начните добавлять клиентов и создавать записи. SOLO всегда под рукой."
    )
}

@Composable
private fun OnboardingPage(
    icon: ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Специализации для выбора в онбординге.
 */
enum class Specialization(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    BEAUTY("beauty", "Салон красоты", "Маникюр, педикюр, визаж", Icons.Default.Face),
    BARBER("barber", "Барбершоп", "Мужские стрижки и бритьё", Icons.Default.ContentCut),
    MASSAGE("massage", "Массаж", "Массаж и СПА", Icons.Default.Spa),
    FITNESS("fitness", "Фитнес", "Тренировки и занятия", Icons.Default.FitnessCenter),
    DENTAL("dental", "Стоматология", "Лечение и профилактика", Icons.Default.MedicalServices),
    OTHER("other", "Другое", "Универсальный вариант", Icons.Default.Category)
}
