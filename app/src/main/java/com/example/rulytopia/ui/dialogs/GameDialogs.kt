package com.example.rulytopia.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.rulytopia.model.FruitType
import com.example.rulytopia.ui.components.FruitBadge
import com.example.rulytopia.ui.components.RulytopiaButton
import com.example.rulytopia.ui.components.StarRatingDisplay

/**
 * Level Complete Dialog (Victory).
 */
@Composable
fun LevelCompleteDialog(
    score: Int,
    starsEarned: Int,
    onNextLevel: () -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF7FFFFFF)),
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(28.dp), spotColor = Color(0x66000000))
                .border(2.5.dp, Color(0xFFFFD54F), RoundedCornerShape(28.dp))
                .testTag("level_complete_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Celebration Title
                Text(
                    text = "🎉 LEVEL COMPLETE! 🎉",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF01579B),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Star Rating
                StarRatingDisplay(
                    starsEarned = starsEarned,
                    starSize = 42.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Score Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE0F2F1))
                        .border(1.5.dp, Color(0x3300897B), RoundedCornerShape(16.dp))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "SCORE:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00897B),
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "$score",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF01579B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RulytopiaButton(
                        text = "NEXT",
                        icon = Icons.Default.PlayArrow,
                        onClick = onNextLevel,
                        gradientColors = listOf(Color(0xFF00E676), Color(0xFF00C853), Color(0xFF00897B)),
                        borderColor = Color(0xFF004D40),
                        fontSize = 13.sp,
                        horizontalPadding = 6.dp,
                        height = 46.dp,
                        modifier = Modifier.weight(1.15f),
                        testTag = "next_level_button"
                    )

                    RulytopiaButton(
                        text = "RETRY",
                        icon = Icons.Default.Refresh,
                        onClick = onRetry,
                        gradientColors = listOf(Color(0xFF0288D1), Color(0xFF01579B)),
                        borderColor = Color(0xFF002F6C),
                        fontSize = 13.sp,
                        horizontalPadding = 6.dp,
                        height = 46.dp,
                        modifier = Modifier.weight(1f),
                        testTag = "retry_button"
                    )

                    RulytopiaButton(
                        text = "LEVELS",
                        icon = Icons.Default.List,
                        onClick = onHome,
                        gradientColors = listOf(Color(0xFF00ACC1), Color(0xFF00838F)),
                        borderColor = Color(0xFF006064),
                        fontSize = 13.sp,
                        horizontalPadding = 6.dp,
                        height = 46.dp,
                        modifier = Modifier.weight(1f),
                        testTag = "home_button"
                    )
                }
            }
        }
    }
}

/**
 * Level Failed Dialog.
 */
@Composable
fun LevelFailedDialog(
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF7FFFFFF)),
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(28.dp), spotColor = Color(0x66000000))
                .border(2.5.dp, Color(0xFFEF5350), RoundedCornerShape(28.dp))
                .testTag("level_failed_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🐵 LEVEL FAILED 🐵",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFD32F2F),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "The mischievous monkeys survived!\nAdjust your trajectory or tap mid-flight to trigger fruit special abilities.",
                    fontSize = 13.sp,
                    color = Color(0xFF455A64),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RulytopiaButton(
                        text = "TRY AGAIN",
                        icon = Icons.Default.Refresh,
                        onClick = onRetry,
                        gradientColors = listOf(Color(0xFF00E676), Color(0xFF00C853)),
                        borderColor = Color(0xFF004D40),
                        fontSize = 13.sp,
                        horizontalPadding = 8.dp,
                        height = 46.dp,
                        modifier = Modifier.weight(1.2f),
                        testTag = "failed_retry_button"
                    )
                    RulytopiaButton(
                        text = "LEVELS",
                        icon = Icons.Default.List,
                        onClick = onHome,
                        gradientColors = listOf(Color(0xFF78909C), Color(0xFF546E7A)),
                        borderColor = Color(0xFF37474F),
                        fontSize = 13.sp,
                        horizontalPadding = 8.dp,
                        height = 46.dp,
                        modifier = Modifier.weight(1f),
                        testTag = "failed_home_button"
                    )
                }
            }
        }
    }
}

/**
 * Pause Dialog.
 */
@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    isSoundEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    isMusicEnabled: Boolean,
    onToggleMusic: (Boolean) -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF7FFFFFF)),
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(28.dp), spotColor = Color(0x66000000))
                .border(2.5.dp, Color(0x3301579B), RoundedCornerShape(28.dp))
                .testTag("pause_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GAME PAUSED",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF01579B),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sound & Music Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = { onToggleSound(!isSoundEnabled) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isSoundEnabled) Color(0xFF00C853) else Color(0xFFCFD8DC))
                    ) {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound SFX",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { onToggleMusic(!isMusicEnabled) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isMusicEnabled) Color(0xFF00ACC1) else Color(0xFFCFD8DC))
                    ) {
                        Icon(
                            imageVector = if (isMusicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                            contentDescription = "Music",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RulytopiaButton(
                        text = "RESUME",
                        icon = Icons.Default.PlayArrow,
                        onClick = onResume,
                        gradientColors = listOf(Color(0xFF00E676), Color(0xFF00C853)),
                        borderColor = Color(0xFF004D40),
                        fontSize = 13.sp,
                        horizontalPadding = 6.dp,
                        height = 46.dp,
                        modifier = Modifier.weight(1.15f),
                        testTag = "resume_button"
                    )
                    RulytopiaButton(
                        text = "RETRY",
                        icon = Icons.Default.Refresh,
                        onClick = onRestart,
                        gradientColors = listOf(Color(0xFF0288D1), Color(0xFF01579B)),
                        borderColor = Color(0xFF002F6C),
                        fontSize = 13.sp,
                        horizontalPadding = 6.dp,
                        height = 46.dp,
                        modifier = Modifier.weight(1f),
                        testTag = "pause_restart_button"
                    )
                    RulytopiaButton(
                        text = "LEVELS",
                        icon = Icons.Default.List,
                        onClick = onHome,
                        gradientColors = listOf(Color(0xFF78909C), Color(0xFF546E7A)),
                        borderColor = Color(0xFF37474F),
                        fontSize = 13.sp,
                        horizontalPadding = 6.dp,
                        height = 46.dp,
                        modifier = Modifier.weight(1f),
                        testTag = "pause_home_button"
                    )
                }
            }
        }
    }
}

/**
 * Fruit Introduction Tutorial Dialog.
 */
@Composable
fun FruitTutorialDialog(
    fruitType: FruitType,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF7FFFFFF)),
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(28.dp), spotColor = Color(0x66000000))
                .border(2.5.dp, fruitType.accentColor, RoundedCornerShape(28.dp))
                .testTag("fruit_tutorial_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NEW HERO UNLOCKED!",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF01579B),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FruitBadge(fruitType = fruitType, size = 52.dp)

                    Column {
                        Text(
                            text = fruitType.displayName.uppercase(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = fruitType.accentColor
                        )
                        Text(
                            text = fruitType.personality,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78909C)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE0F2F1))
                        .border(1.dp, Color(0x3300897B), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Special Ability: ${fruitType.abilityName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00695C)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = fruitType.abilityDescription,
                            fontSize = 12.sp,
                            color = Color(0xFF37474F),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                RulytopiaButton(
                    text = "LET'S GO!",
                    icon = Icons.Default.Check,
                    onClick = onDismiss,
                    gradientColors = listOf(fruitType.primaryColor, fruitType.accentColor),
                    borderColor = fruitType.accentColor,
                    height = 46.dp,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    testTag = "tutorial_dismiss_button"
                )
            }
        }
    }
}

/**
 * Settings Dialog.
 */
@Composable
fun SettingsDialog(
    isSoundEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    isMusicEnabled: Boolean,
    onToggleMusic: (Boolean) -> Unit,
    isVibrationEnabled: Boolean,
    onToggleVibration: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF7FFFFFF)),
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(28.dp), spotColor = Color(0x66000000))
                .border(2.5.dp, Color(0x3301579B), RoundedCornerShape(28.dp))
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SETTINGS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF01579B),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingToggleRow(
                    label = "Sound Effects",
                    icon = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    isChecked = isSoundEnabled,
                    onCheckedChange = onToggleSound
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingToggleRow(
                    label = "Background Music",
                    icon = if (isMusicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                    isChecked = isMusicEnabled,
                    onCheckedChange = onToggleMusic
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingToggleRow(
                    label = "Haptic Vibration",
                    icon = if (isVibrationEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                    isChecked = isVibrationEnabled,
                    onCheckedChange = onToggleVibration
                )

                Spacer(modifier = Modifier.height(16.dp))

                RulytopiaButton(
                    text = "CLOSE",
                    icon = Icons.Default.Close,
                    onClick = onDismiss,
                    gradientColors = listOf(Color(0xFF01579B), Color(0xFF0288D1)),
                    borderColor = Color(0xFF002F6C),
                    height = 46.dp,
                    modifier = Modifier.fillMaxWidth(0.6f),
                    testTag = "close_settings_button"
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE0F2F1))
            .border(1.dp, Color(0x3300897B), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF01579B),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F)
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00C853),
                checkedTrackColor = Color(0xFFA5D6A7),
                uncheckedThumbColor = Color(0xFFB0BEC5),
                uncheckedTrackColor = Color(0xFFECEFF1)
            )
        )
    }
}
