package ru.crmplatforma.solo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.crmplatforma.solo.ui.calendar.CalendarScreen
import ru.crmplatforma.solo.ui.clients.ClientsScreen
import ru.crmplatforma.solo.ui.dashboard.DashboardScreen
import ru.crmplatforma.solo.ui.finance.FinanceScreen
import ru.crmplatforma.solo.ui.more.MoreScreen

// Роуты навигации
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Сегодня", Icons.Default.Today)
    data object Calendar : Screen("calendar", "Календарь", Icons.Default.CalendarMonth)
    data object Clients : Screen("clients", "Клиенты", Icons.Default.People)
    data object Finance : Screen("finance", "Финансы", Icons.Default.Wallet)
    data object More : Screen("more", "Ещё", Icons.Default.Menu)
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
fun SoloApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
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
        }
    }
}
