package com.example.rulytopia.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.isActive
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rulytopia.model.FruitType
import com.example.rulytopia.model.LevelRepository
import com.example.rulytopia.model.Vector2D
import com.example.rulytopia.rendering.GameRenderer
import com.example.rulytopia.ui.GameScreen
import com.example.rulytopia.ui.GameViewModel
import com.example.rulytopia.ui.PlayState
import com.example.rulytopia.ui.components.ScoreStarProgressBar
import com.example.rulytopia.ui.components.SlingshotFruitCrate
import com.example.rulytopia.ui.components.StudioIconButton
import com.example.rulytopia.ui.dialogs.*

@Composable
fun GamePlayScreen(
    viewModel: GameViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val levelDef = LevelRepository.getLevel(uiState.currentLevelId)
    val physics = viewModel.physicsEngine

    var frameTick by remember { mutableLongStateOf(0L) }
    var currentScaleFactor by remember { mutableFloatStateOf(1.0f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (isActive) {
            withFrameNanos { nowNanos ->
                if (lastNanos != 0L) {
                    val dt = ((nowNanos - lastNanos) / 1_000_000_000f).coerceIn(0.004f, 0.033f)
                    viewModel.onGameFrame(dt)
                }
                lastNanos = nowNanos
                frameTick = nowNanos
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("gameplay_screen")
    ) {
        // Main Dynamic Interactive Physics Canvas (Resolution-Independent Scaling)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(uiState.playState, currentScaleFactor) {
                    detectTapGestures(
                        onTap = {
                            viewModel.onScreenTapped()
                        }
                    )
                }
                .pointerInput(uiState.playState, currentScaleFactor) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val scale = currentScaleFactor.coerceAtLeast(0.1f)
                            val worldX = (offset.x / scale) + uiState.cameraOffsetX
                            val worldY = (offset.y / scale) + uiState.cameraOffsetY
                            viewModel.onSlingshotTouchStart(Vector2D(worldX, worldY))
                        },
                        onDrag = { change, _ ->
                            val scale = currentScaleFactor.coerceAtLeast(0.1f)
                            val worldX = (change.position.x / scale) + uiState.cameraOffsetX
                            val worldY = (change.position.y / scale) + uiState.cameraOffsetY
                            viewModel.onSlingshotTouchMove(Vector2D(worldX, worldY))
                        },
                        onDragEnd = {
                            viewModel.onSlingshotTouchRelease()
                        },
                        onDragCancel = {
                            viewModel.onSlingshotTouchRelease()
                        }
                    )
                }
        ) {
            // Read frameTick for 60 FPS continuous redraw synchronization
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTick

            // Calculate dynamic scale factor to map virtual 600f height to physical device canvas
            val scale = size.height / 600f
            currentScaleFactor = scale

            // Apply Resolution Scaling and Camera Viewport Offset with Screen Shake
            withTransform({
                scale(scale, scale, pivot = Offset.Zero)
                translate(
                    left = -uiState.cameraOffsetX + uiState.cameraShakeOffsetX,
                    top = -uiState.cameraOffsetY + uiState.cameraShakeOffsetY
                )
            }) {
                // 1. Endless Parallax Environment
                GameRenderer.drawEnvironment(
                    scope = this,
                    worldWidth = levelDef.worldWidth,
                    worldHeight = 600f,
                    groundY = levelDef.groundY
                )

                // 2. Trajectory prediction guide
                if (uiState.isSlingshotDragging && uiState.currentFruit != null) {
                    GameRenderer.drawTrajectory(
                        scope = this,
                        points = uiState.trajectoryPoints,
                        fruitType = uiState.currentFruit!!
                    )
                }

                // 3. Slingshot Back prong & rubber band
                GameRenderer.drawSlingshotBack(
                    scope = this,
                    anchor = uiState.slingshotAnchor,
                    pullPos = uiState.slingshotPullPos,
                    isPulling = uiState.isSlingshotDragging
                )

                // 4. If aiming and dragging, draw the loaded fruit in pouch
                if (uiState.playState == PlayState.AIMING && uiState.currentFruit != null) {
                    val loadedFruit = com.example.rulytopia.model.FruitEntity(
                        id = 0,
                        type = uiState.currentFruit!!,
                        position = if (uiState.isSlingshotDragging) uiState.slingshotPullPos else uiState.slingshotAnchor
                    )
                    GameRenderer.drawFruit(scope = this, fruit = loadedFruit)
                }

                // 5. Slingshot Front prong & rubber band
                GameRenderer.drawSlingshotFront(
                    scope = this,
                    anchor = uiState.slingshotAnchor,
                    pullPos = uiState.slingshotPullPos,
                    isPulling = uiState.isSlingshotDragging
                )

                // 6. Destructible Blocks (Wood, Glass, Stone, Metal)
                for (block in physics.blocks) {
                    if (!block.isBroken) {
                        GameRenderer.drawBlock(scope = this, block = block)
                    }
                }

                // 7. Mischievous Monkeys
                for (monkey in physics.monkeys) {
                    if (!monkey.isDefeated) {
                        GameRenderer.drawMonkey(scope = this, monkey = monkey)
                    }
                }

                // 8. Active flying fruits
                for (fruit in physics.fruits) {
                    if (fruit.isLaunched && !fruit.isDead) {
                        GameRenderer.drawFruit(scope = this, fruit = fruit)
                    }
                }

                // 9. Particle effects (debris, splashes, smoke, sparks, confetti)
                GameRenderer.drawParticles(scope = this, particles = physics.particles)
            }
        }

        // --- TOP STUDIO HUD BAR (Landscape Optimized) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Carved Wooden Level Badge
            Box(
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF5D4037), Color(0xFF3E2723))
                        )
                    )
                    .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        text = "LEVEL ${uiState.currentLevelId.toString().padStart(2, '0')}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD54F),
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = levelDef.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Center: 3-Star Score Progress Bar
            ScoreStarProgressBar(
                currentScore = uiState.score,
                star1Threshold = levelDef.starThresholds.first,
                star2Threshold = levelDef.starThresholds.second,
                star3Threshold = levelDef.starThresholds.third
            )

            // Right: Fast Restart & Pause Controls
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StudioIconButton(
                    icon = Icons.Default.Refresh,
                    onClick = { viewModel.restartLevel() },
                    tint = Color(0xFF01579B),
                    testTag = "game_restart_button"
                )

                StudioIconButton(
                    icon = Icons.Default.Pause,
                    onClick = { viewModel.pauseGame() },
                    tint = Color(0xFF01579B),
                    testTag = "game_pause_button"
                )
            }
        }

        // --- BOTTOM-LEFT: SLINGSHOT AMMO CRATE ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 16.dp, bottom = 12.dp)
        ) {
            SlingshotFruitCrate(
                currentFruit = uiState.currentFruit,
                remainingFruits = uiState.remainingFruits
            )
        }

        // --- BOTTOM-RIGHT: SPECIAL ABILITY TRIGGER BUTTON ---
        AnimatedVisibility(
            visible = uiState.playState == PlayState.AIRBORNE && uiState.activeFruitAbilityAvailable && uiState.currentFruit != FruitType.APPLE,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 16.dp)
        ) {
            val fruit = uiState.currentFruit
            val (title, iconEmoji, gradColors) = when (fruit) {
                FruitType.BANANA -> Triple("TURBO BOOST", "⚡", listOf(Color(0xFFFFEA00), Color(0xFFFF9100)))
                FruitType.ORANGE -> Triple("BURST EXPLODE", "💥", listOf(Color(0xFFFF9100), Color(0xFFFF3D00)))
                FruitType.CHERRY -> Triple("TRIPLE SPLIT", "🍒", listOf(Color(0xFFFF4081), Color(0xFFC51162)))
                FruitType.DURIAN -> Triple("METEOR SLAM", "🔨", listOf(Color(0xFF76FF03), Color(0xFF33691E)))
                else -> Triple("ABILITY", "✨", listOf(Color(0xFF00E676), Color(0xFF00897B)))
            }

            val infiniteTransition = rememberInfiniteTransition(label = "pulse_ability")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(
                    animation = tween(450, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "ability_scale"
            )

            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .shadow(12.dp, RoundedCornerShape(26.dp), spotColor = Color(0x88000000))
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.horizontalGradient(gradColors))
                    .border(2.5.dp, Color.White, RoundedCornerShape(26.dp))
                    .clickable { viewModel.onScreenTapped() }
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = iconEmoji, fontSize = 22.sp)
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }

        // --- OVERLAYS & DIALOGS ---

        // Level Complete Dialog
        if (uiState.playState == PlayState.LEVEL_COMPLETE) {
            LevelCompleteDialog(
                score = uiState.score,
                starsEarned = uiState.starsEarned,
                onNextLevel = { viewModel.nextLevel() },
                onRetry = { viewModel.restartLevel() },
                onHome = { viewModel.navigateTo(GameScreen.LEVEL_SELECT) }
            )
        }

        // Level Failed Dialog
        if (uiState.playState == PlayState.LEVEL_FAILED) {
            LevelFailedDialog(
                onRetry = { viewModel.restartLevel() },
                onHome = { viewModel.navigateTo(GameScreen.LEVEL_SELECT) }
            )
        }

        // Pause Dialog
        if (uiState.playState == PlayState.PAUSED) {
            PauseDialog(
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.restartLevel() },
                onHome = { viewModel.navigateTo(GameScreen.LEVEL_SELECT) },
                isSoundEnabled = uiState.isSoundEnabled,
                onToggleSound = { viewModel.toggleSound(it) },
                isMusicEnabled = uiState.isMusicEnabled,
                onToggleMusic = { viewModel.toggleMusic(it) }
            )
        }

        // Tutorial Dialog for newly unlocked fruit
        if (uiState.showTutorialDialog != null) {
            FruitTutorialDialog(
                fruitType = uiState.showTutorialDialog!!,
                onDismiss = { viewModel.dismissTutorial() }
            )
        }
    }
}
