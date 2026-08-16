package com.example.rulytopia.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.rulytopia.model.WorldDef
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

    // Determine initial active world from player's highest unlocked level
    val initialWorldId = ((highestUnlocked - 1) / LevelRepository.LEVELS_PER_WORLD + 1).coerceIn(1, LevelRepository.TOTAL_WORLDS)
    var selectedWorldId by remember { mutableIntStateOf(initialWorldId) }

    val currentWorld = LevelRepository.getWorld(selectedWorldId)
    val worldLevels = LevelRepository.getLevelsForWorld(selectedWorldId)

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
                .background(Color(0x44000000))
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
                    .padding(bottom = 10.dp),
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
                        text = "100 LEVELS",
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
                        text = "$totalStars / ${LevelRepository.TOTAL_LEVELS * 3}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color(0xFF01579B)
                    )
                }
            }

            // World Selector Tabs (5 Worlds)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LevelRepository.worlds.forEach { world ->
                    val isWorldUnlocked = highestUnlocked >= world.levelRange.first
                    val isSelected = world.id == selectedWorldId
                    val worldStars = preferences.getWorldStars(world.id)
                    val maxWorldStars = world.levelRange.count() * 3

                    WorldTabChip(
                        world = world,
                        isSelected = isSelected,
                        isUnlocked = isWorldUnlocked,
                        worldStars = worldStars,
                        maxWorldStars = maxWorldStars,
                        onClick = { selectedWorldId = world.id }
                    )
                }
            }

            // World Banner Summary Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xEEFFFFFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp))
                    .border(1.5.dp, Color(currentWorld.themeColorHex).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(currentWorld.themeColorHex))
                            )
                            Text(
                                text = "World ${currentWorld.id}: ${currentWorld.name}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(currentWorld.themeColorHex)
                            )
                            Text(
                                text = "(${currentWorld.levelRange.first}-${currentWorld.levelRange.last})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78909C)
                            )
                        }
                        Text(
                            text = currentWorld.description,
                            fontSize = 11.5.sp,
                            color = Color(0xFF455A64),
                            maxLines = 2,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // World Stars Counter
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(currentWorld.themeColorHex).copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${preferences.getWorldStars(currentWorld.id)}/60",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(currentWorld.themeColorHex)
                            )
                        }
                    }
                }
            }

            // Grid of 20 Levels for Selected World
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 145.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(worldLevels, key = { it.id }) { level ->
                    val isUnlocked = level.id <= highestUnlocked
                    val stars = preferences.getLevelStars(level.id)
                    val highScore = preferences.getLevelHighScore(level.id)

                    LevelCard(
                        level = level,
                        world = currentWorld,
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
private fun WorldTabChip(
    world: WorldDef,
    isSelected: Boolean,
    isUnlocked: Boolean,
    worldStars: Int,
    maxWorldStars: Int,
    onClick: () -> Unit
) {
    val worldColor = Color(world.themeColorHex)
    val containerBg = if (isSelected) worldColor else Color(0xEEFFFFFF)
    val textMainColor = if (isSelected) Color.White else worldColor
    val textSubColor = if (isSelected) Color(0xCCFFFFFF) else Color(0xFF78909C)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        modifier = Modifier
            .shadow(if (isSelected) 6.dp else 2.dp, RoundedCornerShape(16.dp))
            .border(
                1.5.dp,
                if (isSelected) Color.White.copy(alpha = 0.6f) else worldColor.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = if (isSelected) Color.White else Color(0xFF90A4AE),
                    modifier = Modifier.size(14.dp)
                )
            }
            Column {
                Text(
                    text = "W${world.id}: ${world.name}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Black,
                    color = textMainColor
                )
                Text(
                    text = "★ $worldStars/$maxWorldStars",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSubColor
                )
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: LevelDef,
    world: WorldDef,
    isUnlocked: Boolean,
    starsEarned: Int,
    highScore: Int,
    onClick: () -> Unit
) {
    val worldColor = Color(world.themeColorHex)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xF9FFFFFF) else Color(0xCCE0F2F1)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(if (isUnlocked) 6.dp else 2.dp, RoundedCornerShape(22.dp))
            .border(
                2.dp,
                if (isUnlocked) worldColor.copy(alpha = 0.35f) else Color(0xFFB0BEC5),
                RoundedCornerShape(22.dp)
            )
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("level_card_${level.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isUnlocked) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Level Number Badge
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(worldColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${level.id}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    // Level Title
                    Text(
                        text = level.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF01579B),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    // Fruit Badges Preview
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        level.fruitQueue.distinct().forEach { fruit ->
                            FruitBadge(fruitType = fruit, size = 18.dp)
                        }
                    }

                    // Stars Display
                    StarRatingDisplay(
                        starsEarned = starsEarned,
                        starSize = 20.dp
                    )

                    // High Score or Target
                    if (highScore > 0) {
                        Text(
                            text = "Best: $highScore",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF546E7A)
                        )
                    } else {
                        Text(
                            text = level.subtitle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF90A4AE),
                            maxLines = 1
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
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Level ${level.id}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78909C)
                    )
                }
            }
        }
    }
}
