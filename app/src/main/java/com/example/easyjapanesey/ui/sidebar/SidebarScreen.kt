package com.example.easyjapanesey.ui.sidebar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.easyjapanesey.data.model.Category
import com.example.easyjapanesey.data.model.Level1Group
import com.example.easyjapanesey.data.model.Level2Group
import com.example.easyjapanesey.data.repository.VocabularyRepository
import com.example.easyjapanesey.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { VocabularyRepository(context) }
    val categories = remember { repository.loadVocabulary() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Easy Japanesey") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Grammar placeholder
            item {
                GrammarPlaceholder()
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Vocabulary section
            item {
                VocabularySection(
                    categories = categories,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun GrammarPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📚 Grammar (Coming Soon)",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun VocabularySection(
    categories: List<Category>,
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            // Vocabulary header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📖 Vocabulary",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            // Expanded content - Categories
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 24.dp, end = 16.dp, bottom = 8.dp)) {
                    categories.forEach { category ->
                        CategoryItem(
                            category = category,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }
    
    val categoryEmoji = when (category.name) {
        "Noun" -> "📦"
        "Verb" -> "⚡"
        "Adjective" -> "🎨"
        "Adverb" -> "💫"
        else -> "📌"
    }
    
    Column {
        // Category header (Noun, Verb, etc.)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = if (expanded) "Collapse" else "Expand",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$categoryEmoji ${category.name}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        // Expanded content - Level 1 groups
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 28.dp)) {
                category.level1Groups.forEach { level1 ->
                    Level1Item(
                        category = category.name,
                        level1 = level1,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun Level1Item(
    category: String,
    level1: Level1Group,
    navController: NavController
) {
    if (level1.hasSubLevels) {
        // Has level 2 - show expandable
        var expanded by remember { mutableStateOf(false) }
        
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = level1.name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 28.dp)) {
                    level1.level2Groups.forEach { level2 ->
                        Level2Item(
                            category = category,
                            level1 = level1.name,
                            level2 = level2,
                            navController = navController
                        )
                    }
                }
            }
        }
    } else {
        // No level 2 - navigate directly
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate(
                        Screen.Flashcard.createRoute(category, level1.name, null)
                    )
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Empty space instead of arrow icon for consistency
            Spacer(modifier = Modifier.width(28.dp))
            Text(
                text = level1.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun Level2Item(
    category: String,
    level1: String,
    level2: Level2Group,
    navController: NavController
) {
    Text(
        text = level2.name,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(
                    Screen.Flashcard.createRoute(category, level1, level2.name)
                )
            }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Preview(showBackground = true)
@Composable
fun SidebarScreenPreview() {
    com.example.easyjapanesey.ui.theme.EasyJapaneseyTheme {
        SidebarScreen(navController = rememberNavController())
    }
}
