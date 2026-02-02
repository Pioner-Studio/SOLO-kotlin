package ru.crmplatforma.solo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import ru.crmplatforma.solo.ui.appointment.AppointmentEditorScreen
import ru.crmplatforma.solo.ui.calendar.CalendarScreen
import ru.crmplatforma.solo.ui.clients.ClientEditorScreen
import ru.crmplatforma.solo.ui.clients.ClientPickerScreen
import ru.crmplatforma.solo.ui.clients.ClientsScreen
import ru.crmplatforma.solo.ui.services.ServiceEditorScreen
import ru.crmplatforma.solo.ui.services.ServicePickerScreen
import ru.crmplatforma.solo.ui.services.ServicesScreen
import ru.crmplatforma.solo.ui.dashboard.DashboardScreen
import ru.crmplatforma.solo.ui.finance.FinanceScreen
import ru.crmplatforma.solo.ui.finance.TransactionEditorScreen
import ru.crmplatforma.solo.ui.tasks.TaskEditorScreen
import ru.crmplatforma.solo.ui.tasks.TasksScreen
import ru.crmplatforma.solo.ui.more.MoreScreen
import ru.crmplatforma.solo.ui.onboarding.OnboardingScreen
import ru.crmplatforma.solo.ui.ice.ICEScreen
import ru.crmplatforma.solo.ui.ice.ICEEditorScreen
import ru.crmplatforma.solo.ui.subscriptions.SubscriptionsScreen
import ru.crmplatforma.solo.ui.subscriptions.SubscriptionEditorScreen
import java.time.LocalDate

// Роуты навигации
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Onboarding : Screen("onboarding", "Онбординг", Icons.Default.Today)
    data object Dashboard : Screen("dashboard", "Сегодня", Icons.Default.Today)
    data object Calendar : Screen("calendar", "Календарь", Icons.Default.CalendarMonth)
    data object Clients : Screen("clients", "Клиенты", Icons.Default.People)
    data object Finance : Screen("finance", "Финансы", Icons.Default.Wallet)
    data object More : Screen("more", "Ещё", Icons.Default.Menu)

    // Детальные экраны — Записи
    data object AppointmentNew : Screen("appointment/new/{date}", "Новая запись", Icons.Default.Add) {
        fun createRoute(date: LocalDate) = "appointment/new/${date}"
    }
    data object AppointmentEdit : Screen("appointment/edit/{id}", "Редактировать", Icons.Default.Edit) {
        fun createRoute(id: String) = "appointment/edit/$id"
    }

    // Детальные экраны — Клиенты
    data object ClientNew : Screen("client/new", "Новый клиент", Icons.Default.PersonAdd)
    data object ClientEdit : Screen("client/edit/{id}", "Редактировать клиента", Icons.Default.Edit) {
        fun createRoute(id: String) = "client/edit/$id"
    }
    data object ClientPicker : Screen("client/pick", "Выбор клиента", Icons.Default.People)

    // Детальные экраны — Услуги
    data object Services : Screen("services", "Услуги", Icons.Default.ContentCut)
    data object ServiceNew : Screen("service/new", "Новая услуга", Icons.Default.Add)
    data object ServiceEdit : Screen("service/edit/{id}", "Редактировать услугу", Icons.Default.Edit) {
        fun createRoute(id: String) = "service/edit/$id"
    }
    data object ServicePicker : Screen("service/pick", "Выбор услуг", Icons.Default.ContentCut)

    // Детальные экраны — Финансы
    data object TransactionNew : Screen("transaction/new", "Новая операция", Icons.Default.Add)

    // Детальные экраны — Задачи
    data object Tasks : Screen("tasks", "Задачи", Icons.Default.CheckCircle)
    data object TaskNew : Screen("task/new", "Новая задача", Icons.Default.Add)
    data object TaskEdit : Screen("task/edit/{id}", "Редактировать задачу", Icons.Default.Edit) {
        fun createRoute(id: String) = "task/edit/$id"
    }

    // Детальные экраны — ICE
    data object ICE : Screen("ice", "ICE", Icons.Default.Warning)
    data object ICEEdit : Screen("ice/edit", "Редактировать ICE", Icons.Default.Edit)

    // Детальные экраны — Подписки
    data object Subscriptions : Screen("subscriptions", "Подписки", Icons.Default.CreditCard)
    data object SubscriptionNew : Screen("subscription/new", "Новая подписка", Icons.Default.Add)
    data object SubscriptionEdit : Screen("subscription/edit/{id}", "Редактировать подписку", Icons.Default.Edit) {
        fun createRoute(id: String) = "subscription/edit/$id"
    }
}

// Список вкладок Bottom Navigation
val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Calendar,
    Screen.Clients,
    Screen.Finance,
    Screen.More
)

@Composable
fun SoloApp(
    viewModel: SoloAppViewModel = hiltViewModel()
) {
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState(initial = null)

    // Пока не знаем статус онбординга — ничего не показываем
    if (onboardingCompleted == null) return

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Определяем стартовый экран
    val startDestination = if (onboardingCompleted == true) {
        Screen.Dashboard.route
    } else {
        Screen.Onboarding.route
    }

    // Проверяем, нужно ли показывать bottom navigation
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = {
                                Text(
                                    text = screen.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 11.sp
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Онбординг (без bottom bar)
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Основные экраны
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController = navController)
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(navController = navController)
            }
            composable(Screen.Clients.route) {
                ClientsScreen(navController = navController)
            }
            composable(Screen.Finance.route) {
                FinanceScreen(navController = navController)
            }
            composable(Screen.More.route) {
                MoreScreen(navController = navController)
            }

            // Редактор записей — новая запись
            composable(
                route = Screen.AppointmentNew.route,
                arguments = listOf(
                    navArgument("date") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val dateStr = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
                val date = try { LocalDate.parse(dateStr) } catch (e: Exception) { LocalDate.now() }
                AppointmentEditorScreen(
                    navController = navController,
                    appointmentId = null,
                    initialDate = date
                )
            }

            // Редактор записей — редактирование
            composable(
                route = Screen.AppointmentEdit.route,
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val appointmentId = backStackEntry.arguments?.getString("id")
                AppointmentEditorScreen(
                    navController = navController,
                    appointmentId = appointmentId
                )
            }

            // Редактор клиентов — новый клиент
            composable(Screen.ClientNew.route) {
                ClientEditorScreen(
                    navController = navController,
                    clientId = null
                )
            }

            // Редактор клиентов — редактирование
            composable(
                route = Screen.ClientEdit.route,
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("id")
                ClientEditorScreen(
                    navController = navController,
                    clientId = clientId
                )
            }

            // Выбор клиента для записи
            composable(Screen.ClientPicker.route) {
                ClientPickerScreen(navController = navController)
            }

            // Список услуг
            composable(Screen.Services.route) {
                ServicesScreen(navController = navController)
            }

            // Редактор услуг — новая услуга
            composable(Screen.ServiceNew.route) {
                ServiceEditorScreen(
                    navController = navController,
                    serviceId = null
                )
            }

            // Редактор услуг — редактирование
            composable(
                route = Screen.ServiceEdit.route,
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val serviceId = backStackEntry.arguments?.getString("id")
                ServiceEditorScreen(
                    navController = navController,
                    serviceId = serviceId
                )
            }

            // Новая транзакция (доход/расход)
            composable(Screen.TransactionNew.route) {
                TransactionEditorScreen(navController = navController)
            }

            // Выбор услуг для записи
            composable(Screen.ServicePicker.route) {
                ServicePickerScreen(navController = navController)
            }

            // Список задач
            composable(Screen.Tasks.route) {
                TasksScreen(navController = navController)
            }

            // Новая задача
            composable(Screen.TaskNew.route) {
                TaskEditorScreen(navController = navController, taskId = null)
            }

            // Редактирование задачи
            composable(
                route = Screen.TaskEdit.route,
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("id")
                TaskEditorScreen(navController = navController, taskId = taskId)
            }

            // ICE — экстренная карточка
            composable(Screen.ICE.route) {
                ICEScreen(navController = navController)
            }

            // Редактор ICE
            composable(Screen.ICEEdit.route) {
                ICEEditorScreen(navController = navController)
            }

            // Список подписок
            composable(Screen.Subscriptions.route) {
                SubscriptionsScreen(navController = navController)
            }

            // Новая подписка
            composable(Screen.SubscriptionNew.route) {
                SubscriptionEditorScreen(navController = navController)
            }

            // Редактирование подписки
            composable(
                route = Screen.SubscriptionEdit.route,
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val subscriptionId = backStackEntry.arguments?.getString("id")
                SubscriptionEditorScreen(navController = navController, subscriptionId = subscriptionId)
            }
        }
    }
}
