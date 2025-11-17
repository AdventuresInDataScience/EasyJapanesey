package com.example.easyjapanesey.ui.flashcard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    navController: NavController,
    category: String,
    level1: String,
    level2: String?
) {
    val context = LocalContext.current
    val viewModel = remember { FlashcardViewModel(context, category, level1, level2) }
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (level2 != null) "$category - $level1 - $level2" else "$category - $level1") 
                },
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
        if (uiState.cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No cards available")
            }
        } else {
            val currentCard = uiState.cards[uiState.currentIndex]
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Progress indicator
                Text(
                    text = "${uiState.currentIndex + 1} / ${uiState.cards.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Main content - changes layout based on orientation
                if (isLandscape) {
                    // Landscape: Row layout (emoji left, card center, buttons right)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji on left
                        Text(
                            text = currentCard.emoji,
                            fontSize = 70.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                        
                        // Card in center
                        key(uiState.currentIndex) {
                            FlippableCard(
                                isFlipped = uiState.isFlipped,
                                frontContent = currentCard.english,
                                backContent = currentCard.romaji,
                                onFlip = { viewModel.flipCard() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(140.dp)
                            )
                        }
                        
                        // Right column: Progress buttons + Speaker button stacked
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            var wrongPressed by remember { mutableStateOf(false) }
                            var correctPressed by remember { mutableStateOf(false) }
                            
                            val wrongScale by animateFloatAsState(
                                targetValue = if (wrongPressed) 0.85f else 1f,
                                animationSpec = tween(100),
                                label = "wrongScale"
                            )
                            
                            val correctScale by animateFloatAsState(
                                targetValue = if (correctPressed) 0.85f else 1f,
                                animationSpec = tween(100),
                                label = "correctScale"
                            )
                            
                            // Reset pressed states when card changes
                            LaunchedEffect(uiState.currentIndex) {
                                wrongPressed = false
                                correctPressed = false
                            }
                            
                            // Wrong button (Red X)
                            IconButton(
                                onClick = { 
                                    wrongPressed = true
                                    viewModel.markCardWrong()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .graphicsLayer {
                                        scaleX = wrongScale
                                        scaleY = wrongScale
                                    }
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("❌", fontSize = 20.sp)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Correct button (Green Check)
                            IconButton(
                                onClick = { 
                                    correctPressed = true
                                    viewModel.markCardCorrect()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .graphicsLayer {
                                        scaleX = correctScale
                                        scaleY = correctScale
                                    }
                            ) {
                                Surface(
                                    color = Color(0xFF4CAF50),
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("✅", fontSize = 20.sp)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Speaker button
                            IconButton(
                                onClick = { 
                                    if (uiState.ttsAvailable) {
                                        viewModel.speakWord(currentCard.romaji)
                                    }
                                },
                                enabled = uiState.ttsAvailable,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Text("🔊", fontSize = 24.sp)
                            }
                            
                            if (uiState.ttsError != null) {
                                Text(
                                    text = "N/A",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                } else {
                    // Portrait: Column layout (emoji top, card below)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Emoji
                        Text(
                            text = currentCard.emoji,
                            fontSize = 120.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Flippable card
                        key(uiState.currentIndex) {
                            FlippableCard(
                                isFlipped = uiState.isFlipped,
                                frontContent = currentCard.english,
                                backContent = currentCard.romaji,
                                onFlip = { viewModel.flipCard() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Speaker button
                        if (uiState.ttsAvailable) {
                            Button(
                                onClick = { viewModel.speakWord(currentCard.romaji) },
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text("🔊 Play Sound")
                            }
                        } else if (uiState.ttsError != null) {
                            Text(
                                text = uiState.ttsError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // Progress buttons (Correct/Wrong) - different layout per orientation
                if (isLandscape) {
                    // In landscape, buttons are on the right in the main Row
                } else {
                    // Portrait: buttons centered below card
                    var wrongPressed by remember { mutableStateOf(false) }
                    var correctPressed by remember { mutableStateOf(false) }
                    
                    val wrongScale by animateFloatAsState(
                        targetValue = if (wrongPressed) 0.85f else 1f,
                        animationSpec = tween(100),
                        label = "wrongScale"
                    )
                    
                    val correctScale by animateFloatAsState(
                        targetValue = if (correctPressed) 0.85f else 1f,
                        animationSpec = tween(100),
                        label = "correctScale"
                    )
                    
                    // Reset pressed states when card changes
                    LaunchedEffect(uiState.currentIndex) {
                        wrongPressed = false
                        correctPressed = false
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Wrong button (Red X)
                        IconButton(
                            onClick = { 
                                wrongPressed = true
                                viewModel.markCardWrong()
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .graphicsLayer {
                                    scaleX = wrongScale
                                    scaleY = wrongScale
                                }
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.error,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("❌", fontSize = 24.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Correct button (Green Check)
                        IconButton(
                            onClick = { 
                                correctPressed = true
                                viewModel.markCardCorrect()
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .graphicsLayer {
                                    scaleX = correctScale
                                    scaleY = correctScale
                                }
                        ) {
                            Surface(
                                color = Color(0xFF4CAF50),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("✅", fontSize = 24.sp)
                                }
                            }
                        }
                    }
                }
                
                // Navigation arrows (same for both orientations)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.previousCard() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { viewModel.nextCard() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun FlippableCard(
    isFlipped: Boolean,
    frontContent: String,
    backContent: String,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlip"
    )
    
    Card(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { onFlip() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Front side
                Text(
                    text = frontContent,
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    maxLines = 5,
                    softWrap = true,
                    lineHeight = 36.sp,
                    fontSize = if (frontContent.length > 50) 24.sp 
                              else if (frontContent.length > 30) 32.sp 
                              else 40.sp
                )
            } else {
                // Back side (reversed text for proper display when flipped)
                Text(
                    text = backContent,
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .graphicsLayer {
                            rotationY = 180f
                        },
                    maxLines = 5,
                    softWrap = true,
                    lineHeight = 32.sp,
                    fontSize = if (backContent.length > 50) 20.sp 
                              else if (backContent.length > 30) 28.sp 
                              else 36.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FlippableCardPreview() {
    com.example.easyjapanesey.ui.theme.EasyJapaneseyTheme {
        var isFlipped by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🍎", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(16.dp))
            FlippableCard(
                isFlipped = isFlipped,
                frontContent = "apple",
                backContent = "ringo",
                onFlip = { isFlipped = !isFlipped },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}
