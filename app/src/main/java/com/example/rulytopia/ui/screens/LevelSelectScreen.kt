package com.example.rulytopia.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.rulytopia.data.GamePreferences
import com.example.rulytopia.model.LevelDef
import com.example.rulytopia.model.LevelRepository
import com.example.rulytopia.ui.components.FruitBadge
import com.example.rulytopia.ui.components.StarRatingDisplay

@Composable
fun LevelSelectScreen(
    preferences: GamePreferences,
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val highestUnlocked = preferences.highestUnlockedLevel
    val totalStars = preferences.getTotalStars()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("level_select_screen")
    ) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.bg_rulytopia_jungle),
            contentDescription = "Jungle Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Backdrop tint
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x33000000))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            // Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xF2FFFFFF))
                        .border(1.5.dp, Color(0x3301579B), RoundedCornerShape(16.dp))
                        .testTag("level_select_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF01579B)
                    )
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xF2FFFFFF)),
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .border(1.5.dp, Color(0x3301579B), RoundedCornerShape(20.dp))
                ) {
                    Text(
                        text = "SELECT LEVEL",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF01579B),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                // Stars Badge
                Row(
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xF2FFFFFF))
                        .border(1.5.dp, Color(0x3301579B), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFACC15),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$totalStars",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color(0xFF01579B)
                    )
                }
            }

            // Grid of 10 Levels
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(LevelRepository.levels) { level ->
                    val isUnlocked = level.id <= highestUnlocked
                    val stars = preferences.getLevelStars(level.id)
                    val highScore = preferences.getLevelHighScore(level.id)

                    LevelCard(
                        level = level,
                        isUnlocked = isUnlocked,
                        starsEarned = stars,
                        highScore = highScore,
                        onClick = {
                            if (isUnlocked) {
                                onLevelSelected(level.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: LevelDef,
    isUnlocked: Boolean,
    starsEarned: Int,
    highScore: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xF7FFFFFF) else Color(0xCCE0F2F1)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(165.dp)
            .shadow(if (isUnlocked) 8.dp else 2.dp, RoundedCornerShape(24.dp))
            .border(
                2.dp,
                if (isUnlocked) Color(0x4401579B) else Color(0xFFB0BEC5),
                RoundedCornerShape(24.dp)
            )
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("level_card_${level.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isUnlocked) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Level Number Circle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF01579B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${level.id}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    // Level Title
                    Text(
                        text = level.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF01579B),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    // Fruit Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        level.fruitQueue.distinct().forEach { fruit ->
                            FruitBadge(fruitType = fruit, size = 20.dp)
                        }
                    }

                    // Stars
                    StarRatingDisplay(
                        starsEarned = starsEarned,
                        starSize = 22.dp
                    )

                    // High score if played
                    if (highScore > 0) {
                        Text(
                            text = "Best: $highScore",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF546E7A)
                        )
                    }
                }
            } else {
                // Locked View
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF90A4AE),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Level ${level.id}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78909C)
                    )
                }
            }
        }
    }
}
