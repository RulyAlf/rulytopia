package com.example.rulytopia.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rulytopia.model.FruitType
import com.example.ui.theme.ImmersiveGold
import com.example.ui.theme.ImmersiveOceanBlue

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/**
 * AAA Studio Tactile Candy/Wood Game Button with 3D spring press physics.
 */
@Composable
fun RulytopiaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradientColors: List<Color> = listOf(Color(0xFFFFB300), Color(0xFFFF8F00), Color(0xFFE65100)),
    borderColor: Color = Color(0xFFBF360C),
    textColor: Color = Color.White,
    fontSize: TextUnit = 14.sp,
    horizontalPadding: Dp = 10.dp,
    height: Dp = 50.dp,
    testTag: String = "rulytopia_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(height)
            .shadow(if (isPressed) 2.dp else 8.dp, RoundedCornerShape(22.dp), spotColor = Color(0x66000000))
            .clip(RoundedCornerShape(22.dp))
            .background(brush = Brush.verticalGradient(gradientColors))
            .border(2.5.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = horizontalPadding, vertical = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Top bevel highlight gloss line
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.9f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0x55FFFFFF))
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * AAA Studio Glossy Round Icon Button for In-Game HUD (Pause, Restart, Settings)
 */
@Composable
fun StudioIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    tint: Color = Color(0xFF01579B),
    gradientColors: List<Color> = listOf(Color(0xFFFFFFFF), Color(0xFFE1F5FE)),
    borderColor: Color = Color(0x6601579B),
    testTag: String = "studio_icon_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "icon_btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(size)
            .shadow(if (isPressed) 1.dp else 6.dp, CircleShape, spotColor = Color(0x44000000))
            .clip(CircleShape)
            .background(Brush.verticalGradient(gradientColors))
            .border(2.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Top specular gleam
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 3.dp)
                .size(width = 16.dp, height = 4.dp)
                .clip(CircleShape)
                .background(Color(0x88FFFFFF))
        )

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

/**
 * AAA Studio Star Score Progress Bar
 * Fills up smoothly towards 1★, 2★, and 3★ thresholds.
 */
@Composable
fun ScoreStarProgressBar(
    currentScore: Int,
    star1Threshold: Int,
    star2Threshold: Int,
    star3Threshold: Int,
    modifier: Modifier = Modifier
) {
    val progress = (currentScore.toFloat() / star3Threshold.coerceAtLeast(1)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "score_bar_progress")

    val hasStar1 = currentScore >= star1Threshold
    val hasStar2 = currentScore >= star2Threshold
    val hasStar3 = currentScore >= star3Threshold

    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x33000000))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xF2FFFFFF))
            .border(2.dp, Color(0x3301579B), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Score Display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "$currentScore",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF01579B)
                )
            }

            // Progress Bar with Star Markers
            Box(
                modifier = Modifier
                    .width(170.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFCFD8DC))
            ) {
                // Filling Gradient
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00E676), Color(0xFFFFD600), Color(0xFFFF9100))
                            )
                        )
                )

                // 3 Star Pins
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StarPin(achieved = hasStar1)
                    StarPin(achieved = hasStar2)
                    StarPin(achieved = hasStar3)
                }
            }
        }
    }
}

@Composable
private fun StarPin(achieved: Boolean) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(if (achieved) Color(0xFFFFD600) else Color(0xFF90A4AE))
            .border(1.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(9.dp)
        )
    }
}

/**
 * Compact Studio Slingshot Ammo Crate (Bottom-Left Corner)
 */
@Composable
fun SlingshotFruitCrate(
    currentFruit: FruitType?,
    remainingFruits: List<FruitType>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = Color(0x55000000))
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF4E342E), Color(0xFF3E2723))
                )
            )
            .border(2.dp, Color(0xFF8D6E63), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Current Loaded Fruit Badge
            if (currentFruit != null) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFF9C4), Color(0xFFFFE082))
                            )
                        )
                        .border(2.dp, currentFruit.accentColor, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (currentFruit) {
                            FruitType.APPLE -> "🍎"
                            FruitType.BANANA -> "🍌"
                            FruitType.ORANGE -> "🍊"
                            FruitType.CHERRY -> "🍒"
                            FruitType.DURIAN -> "🍈"
                        },
                        fontSize = 22.sp
                    )
                }
            }

            // Next In Queue Mini Pips
            if (remainingFruits.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    remainingFruits.take(3).forEach { fruit ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0x44FFFFFF))
                                .border(1.dp, Color(0x88FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (fruit) {
                                    FruitType.APPLE -> "🍎"
                                    FruitType.BANANA -> "🍌"
                                    FruitType.ORANGE -> "🍊"
                                    FruitType.CHERRY -> "🍒"
                                    FruitType.DURIAN -> "🍈"
                                },
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Star Rating Display (0..3 stars) with glowing yellow accents.
 */
@Composable
fun StarRatingDisplay(
    starsEarned: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 38.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..3) {
            val isEarned = i <= starsEarned
            val scale by animateFloatAsState(
                targetValue = if (isEarned) 1.15f else 1.0f,
                label = "star_scale"
            )

            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(starSize)
                    .clip(CircleShape)
                    .background(if (isEarned) ImmersiveGold.copy(alpha = 0.25f) else Color(0x22FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isEarned) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star $i",
                    tint = if (isEarned) Color(0xFFFFD600) else Color(0x66B0BEC5),
                    modifier = Modifier.size(starSize * 0.88f)
                )
            }
        }
    }
}

/**
 * Fruit preview token showing fruit circle + emoji.
 */
@Composable
fun FruitBadge(
    fruitType: FruitType,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(fruitType.primaryColor)
            .border(2.dp, fruitType.accentColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (fruitType) {
                FruitType.APPLE -> "🍎"
                FruitType.BANANA -> "🍌"
                FruitType.ORANGE -> "🍊"
                FruitType.CHERRY -> "🍒"
                FruitType.DURIAN -> "🍈"
            },
            fontSize = (size.value * 0.55f).sp
        )
    }
}

/**
 * Sleek Fruit Queue Card for the Immersive UI Bottom Dock.
 */
@Composable
fun FruitQueueCard(
    fruitType: FruitType,
    index: Int,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    val bgTint = when (fruitType) {
        FruitType.APPLE -> Color(0xFFFFEBEE)
        FruitType.BANANA -> Color(0xFFFFFDE7)
        FruitType.ORANGE -> Color(0xFFFFF3E0)
        FruitType.CHERRY -> Color(0xFFFCE4EC)
        FruitType.DURIAN -> Color(0xFFF1F8E9)
    }

    Box(
        modifier = modifier
            .size(if (isCurrent) 56.dp else 48.dp)
            .shadow(if (isCurrent) 6.dp else 2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (isCurrent) bgTint else Color(0xFFF8FAFC))
            .border(
                width = if (isCurrent) 2.5.dp else 1.5.dp,
                color = if (isCurrent) fruitType.accentColor else Color(0xFFCFD8DC),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (fruitType) {
                FruitType.APPLE -> "🍎"
                FruitType.BANANA -> "🍌"
                FruitType.ORANGE -> "🍊"
                FruitType.CHERRY -> "🍒"
                FruitType.DURIAN -> "🍈"
            },
            fontSize = if (isCurrent) 24.sp else 20.sp
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-3).dp, y = 3.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(if (isCurrent) fruitType.accentColor else Color(0xFF90A4AE)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$index",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}
