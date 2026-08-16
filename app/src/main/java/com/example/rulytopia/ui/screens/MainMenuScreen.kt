package com.example.rulytopia.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.rulytopia.model.FruitType
import com.example.rulytopia.ui.components.FruitBadge
import com.example.rulytopia.ui.components.RulytopiaButton

@Composable
fun MainMenuScreen(
    totalStars: Int,
    onPlay: () -> Unit,
    onLevelSelect: () -> Unit,
    onSettings: () -> Unit
) {
    // Gentle floating animation for title
    val infiniteTransition = rememberInfiniteTransition(label = "menu_bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_bounce"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_menu_screen")
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.bg_rulytopia_jungle),
            contentDescription = "Jungle Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x11000000), Color(0x66000000)),
                        radius = 1200f
                    )
                )
        )

        // Landscape 2-Column Main Menu
        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 36.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT COLUMN: Game Title & Fruit Roster Card
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF2FFFFFF)),
                modifier = Modifier
                    .weight(1.1f)
                    .offset(y = bounceOffset.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color(0x55000000))
                    .border(2.5.dp, Color(0xFFFFD54F), RoundedCornerShape(32.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Fruit avatars header row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FruitBadge(FruitType.APPLE, size = 34.dp)
                        FruitBadge(FruitType.BANANA, size = 34.dp)
                        FruitBadge(FruitType.ORANGE, size = 34.dp)
                        FruitBadge(FruitType.CHERRY, size = 34.dp)
                        FruitBadge(FruitType.DURIAN, size = 34.dp)
                    }

                    Text(
                        text = "RULYTOPIA",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF01579B),
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.5.sp
                    )

                    Text(
                        text = "FRUIT ASSAULT • PHYSICS PUZZLE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00897B),
                        letterSpacing = 1.8.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Pull, aim, and topple monkey fortresses with unique fruit abilities!",
                        color = Color(0xFF546E7A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.width(32.dp))

            // RIGHT COLUMN: Star counter & Game Play Buttons
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Star Counter Pill
                Box(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0x33000000))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xF2FFFFFF))
                        .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "$totalStars / 30 STARS",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF01579B),
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RulytopiaButton(
                        text = "PLAY GAME",
                        icon = Icons.Default.PlayArrow,
                        onClick = onPlay,
                        gradientColors = listOf(Color(0xFF00E676), Color(0xFF00C853), Color(0xFF00897B)),
                        borderColor = Color(0xFF004D40),
                        height = 56.dp,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "main_play_button"
                    )

                    RulytopiaButton(
                        text = "SELECT LEVEL",
                        icon = Icons.Default.List,
                        onClick = onLevelSelect,
                        gradientColors = listOf(Color(0xFF0288D1), Color(0xFF01579B)),
                        borderColor = Color(0xFF002F6C),
                        height = 50.dp,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "main_levels_button"
                    )

                    RulytopiaButton(
                        text = "SETTINGS",
                        icon = Icons.Default.Settings,
                        onClick = onSettings,
                        gradientColors = listOf(Color(0xFF00BCD4), Color(0xFF00838F)),
                        borderColor = Color(0xFF006064),
                        height = 48.dp,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "main_settings_button"
                    )
                }
            }
        }
    }
}
