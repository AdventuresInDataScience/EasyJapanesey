package com.example.easyjapanesey.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.easyjapanesey.data.model.FilterMode
import com.example.easyjapanesey.data.preferences.UserProgressRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { UserProgressRepository(context) }
    
    var currentFilter by remember { mutableStateOf(repository.getFilterMode()) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showPositionResetDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
            // Filter Mode Section
            Text(
                text = "Question Filter",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FilterMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentFilter == mode,
                                onClick = {
                                    currentFilter = mode
                                    repository.setFilterMode(mode)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when (mode) {
                                        FilterMode.ALL -> "All Questions"
                                        FilterMode.WRONG_ONLY -> "Wrong Only"
                                        FilterMode.WRONG_AND_UNSEEN -> "Wrong + Unseen"
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = when (mode) {
                                        FilterMode.ALL -> "Show all vocabulary cards"
                                        FilterMode.WRONG_ONLY -> "Only show cards marked wrong"
                                        FilterMode.WRONG_AND_UNSEEN -> "Show wrong and unseen cards"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Reset Options Section
            Text(
                text = "Reset Options",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Reset Positions Button
            OutlinedButton(
                onClick = { showPositionResetDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("🔄 Reset All Positions")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Reset Progress Button
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("⚠️ Reset All Progress")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Note: Resetting progress will clear all correct/wrong marks.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            }
        }
    }
    
    // Reset Position Confirmation Dialog
    if (showPositionResetDialog) {
        AlertDialog(
            onDismissRequest = { showPositionResetDialog = false },
            title = { Text("Reset Positions?") },
            text = { Text("This will reset your current position in all vocabulary collections. Your progress (correct/wrong marks) will not be affected.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.resetAllPositions()
                        showPositionResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPositionResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Reset Progress Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Progress?") },
            text = { Text("This will permanently delete all your progress including correct/wrong marks. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.resetAllProgress()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    com.example.easyjapanesey.ui.theme.EasyJapaneseyTheme {
        SettingsScreen(navController = rememberNavController())
    }
}
