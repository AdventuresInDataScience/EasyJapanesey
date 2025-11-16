package com.example.easyjapanesey.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.easyjapanesey.navigation.Screen

@Composable
fun HomeScreen(navController: NavController) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    // Adjust sizes based on orientation
    val imageSize = if (isLandscape) 120.dp else 200.dp
    val logoFontSize = if (isLandscape) 48.sp else 64.sp
    val titleFontSize = if (isLandscape) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge
    val buttonHeight = if (isLandscape) 48.dp else 56.dp
    val verticalSpacing = if (isLandscape) 16.dp else 24.dp
    val topBottomPadding = if (isLandscape) 16.dp else 32.dp
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = topBottomPadding)
        ) {
            // Placeholder for SVG/Image
            // TODO: Replace this with your SVG image
            // To replace: Use Image(painter = painterResource(R.drawable.your_image), ...)
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🇯🇵\n日本語",
                    fontSize = logoFontSize,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayLarge
                )
            }
            
            Spacer(modifier = Modifier.height(verticalSpacing))
            
            Text(
                text = "Easy Japanesey",
                style = titleFontSize,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(verticalSpacing * 2))
            
            // Menu Button
            Button(
                onClick = { navController.navigate(Screen.Sidebar.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "📚 Start Learning",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Settings Button
            OutlinedButton(
                onClick = { navController.navigate(Screen.Settings.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight)
            ) {
                Text(
                    text = "⚙️ Settings",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    com.example.easyjapanesey.ui.theme.EasyJapaneseyTheme {
        HomeScreen(navController = rememberNavController())
    }
}
