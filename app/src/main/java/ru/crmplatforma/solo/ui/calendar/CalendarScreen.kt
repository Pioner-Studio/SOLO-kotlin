package ru.crmplatforma.solo.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController) {
    var selectedView by remember { mutableStateOf(0) }
    val viewOptions = listOf("День", "2 недели", "Месяц")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Календарь") },
                actions = {
                    IconButton(onClick = { /* Jump to today */ }) {
                        Icon(Icons.Default.Today, contentDescription = "Сегодня")
                    }
                    IconButton(onClick = { /* Add appointment */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить")
                    }
                },
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
            // View switcher
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                viewOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = viewOptions.size),
                        onClick = { selectedView = index },
                        selected = selectedView == index
                    ) {
                        Text(label)
                    }
                }
            }

            // Calendar content placeholder
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📅",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (selectedView) {
                            0 -> "День"
                            1 -> "2 недели"
                            else -> "Месяц"
                        },
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Календарь в разработке",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
