package com.example.rulytopia.rendering

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.rulytopia.model.*
import kotlin.math.*

/**
 * AAA Studio-Grade 2D Visual Renderer for Rulytopia.
 * Renders parallax environment, dynamic 3D-shaded slingshot, expressive fruit heroes,
 * animated monkeys with dynamic expressions, textured destructible materials,
 * trajectory predictions with landing reticles, and juicy particle effects.
 */
object GameRenderer {

    // --- PARALLAX BACKGROUND & ENVIRONMENT ---

    fun drawEnvironment(
        scope: DrawScope,
        worldWidth: Float,
        worldHeight: Float,
        groundY: Float
    ) {
        val minX = -800f
        val maxX = worldWidth + 1200f
        val totalWidth = maxX - minX
        val topY = -400f
        val bottomY = worldHeight + 400f

        // 1. Tropical Atmosphere Sky (Multi-stop gradient with warm sunny horizon)
        scope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF29B6F6), // Vibrant Azure Sky Top
                    Color(0xFF81D4FA), // Light Tropical Blue
                    Color(0xFFB2EBF2), // Cyan Haze
                    Color(0xFFFFF9C4), // Golden Sunlit Horizon Glow
                    Color(0xFFE8F5E9)  // Soft Meadow Mist
                ),
                startY = topY,
                endY = groundY
            ),
            topLeft = Offset(minX, topY),
            size = Size(totalWidth, groundY - topY)
        )

        // 2. Sunny Ambient Glow & Radial Sun
        val sunCenter = Offset(worldWidth * 0.75f, groundY * 0.28f)
        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x88FFF59D),
                    Color(0x33FFE082),
                    Color(0x00FFD54F)
                ),
                center = sunCenter,
                radius = 280f
            ),
            radius = 280f,
            center = sunCenter
        )
        scope.drawCircle(Color(0xFFFFF59D), 48f, sunCenter)
        scope.drawCircle(Color(0xFFFFFFFF), 36f, Offset(sunCenter.x - 4f, sunCenter.y - 4f))

        // 3. Distant Majestic Mountain Peaks (Layer 1 - Misty Blue-Violet)
        val mountainPath = Path().apply {
            moveTo(minX, groundY)
            lineTo(minX, groundY - 140f)
            lineTo(minX + 300f, groundY - 260f)
            lineTo(minX + 550f, groundY - 150f)
            lineTo(minX + 900f, groundY - 290f)
            lineTo(minX + 1300f, groundY - 160f)
            lineTo(minX + 1700f, groundY - 280f)
            lineTo(minX + 2100f, groundY - 150f)
            lineTo(maxX, groundY - 200f)
            lineTo(maxX, groundY)
            close()
        }
        scope.drawPath(
            path = mountainPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x665C6BC0), Color(0x333949AB), Color(0x11283593)),
                startY = groundY - 300f,
                endY = groundY
            )
        )

        // 4. Layered Animated Puffy Clouds
        drawPuffyCloud(scope, 80f, 60f, 52f)
        drawPuffyCloud(scope, 420f, 95f, 68f)
        drawPuffyCloud(scope, 820f, 50f, 58f)
        drawPuffyCloud(scope, 1250f, 85f, 64f)
        drawPuffyCloud(scope, -320f, 75f, 56f)

        // 5. Far Jungle Ridge (Layer 2 - Deep Pine Green)
        val farHills = Path().apply {
            moveTo(minX, groundY)
            cubicTo(minX + 300f, groundY - 110f, minX + 600f, groundY - 60f, minX + 900f, groundY - 120f)
            cubicTo(minX + 1200f, groundY - 150f, minX + 1500f, groundY - 70f, minX + 1800f, groundY - 100f)
            cubicTo(minX + 2100f, groundY - 140f, minX + 2400f, groundY - 60f, maxX, groundY - 90f)
            lineTo(maxX, groundY)
            close()
        }
        scope.drawPath(farHills, color = Color(0xFF2E7D32).copy(alpha = 0.55f))

        // 6. Midground Rolling Tropical Hills (Layer 3 - Vibrant Lime-Green)
        val midHills = Path().apply {
            moveTo(minX, groundY)
            cubicTo(minX + 200f, groundY - 75f, minX + 450f, groundY - 30f, minX + 750f, groundY - 70f)
            cubicTo(minX + 1050f, groundY - 95f, minX + 1350f, groundY - 40f, minX + 1650f, groundY - 65f)
            cubicTo(minX + 1950f, groundY - 85f, minX + 2250f, groundY - 35f, maxX, groundY - 55f)
            lineTo(maxX, groundY)
            close()
        }
        scope.drawPath(
            midHills,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF66BB6A), Color(0xFF43A047)),
                startY = groundY - 100f,
                endY = groundY
            )
        )

        // 7. Ground Grass Crest & Dirt Strata
        // Vibrant Top Grass Strip (with rounded bevel edge)
        scope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF7CB342), Color(0xFF558B2F)),
                startY = groundY,
                endY = groundY + 16f
            ),
            topLeft = Offset(minX, groundY),
            size = Size(totalWidth, 16f)
        )

        // Sub-surface Rich Dark Soil
        scope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF4E342E), // Rich Loam
                    Color(0xFF3E2723), // Dark Earth
                    Color(0xFF271916)  // Bedrock
                ),
                startY = groundY + 16f,
                endY = bottomY
            ),
            topLeft = Offset(minX, groundY + 16f),
            size = Size(totalWidth, bottomY - groundY - 16f)
        )

        // Stylized Grass Tufts & Flowers
        var tuftX = minX + 25f
        var seed = 1
        while (tuftX < maxX) {
            seed = (seed * 31 + 17) % 1000
            val h = 8f + (seed % 7)
            // Left blade
            scope.drawLine(
                color = Color(0xFF8BC34A),
                start = Offset(tuftX, groundY + 2f),
                end = Offset(tuftX - 4f, groundY - h),
                strokeWidth = 3.2f,
                cap = StrokeCap.Round
            )
            // Right blade
            scope.drawLine(
                color = Color(0xFF9CCC65),
                start = Offset(tuftX + 4f, groundY + 2f),
                end = Offset(tuftX + 6f, groundY - h - 3f),
                strokeWidth = 3.2f,
                cap = StrokeCap.Round
            )
            // Occasional jungle flower
            if (seed % 5 == 0) {
                scope.drawCircle(Color(0xFFFFEB3B), 3.5f, Offset(tuftX + 1f, groundY - h - 2f))
                scope.drawCircle(Color(0xFFFF4081), 2f, Offset(tuftX + 1f, groundY - h - 2f))
            }
            tuftX += 45f + (seed % 20)
        }

        // Sub-soil rock pebbles
        var pebbleX = minX + 40f
        while (pebbleX < maxX) {
            seed = (seed * 19 + 7) % 500
            val pY = groundY + 28f + (seed % 60)
            val pR = 4f + (seed % 5)
            scope.drawOval(
                color = Color(0x558D6E63),
                topLeft = Offset(pebbleX, pY),
                size = Size(pR * 2f, pR * 1.3f)
            )
            pebbleX += 65f + (seed % 35)
        }
    }

    private fun drawPuffyCloud(scope: DrawScope, x: Float, y: Float, radius: Float) {
        val cloudShadow = Color(0x3381D4FA)
        val cloudWhite = Color(0xF5FFFFFF)

        // Drop shadow for 3D depth
        scope.drawCircle(cloudShadow, radius * 0.72f, Offset(x - radius * 0.6f, y + 4f))
        scope.drawCircle(cloudShadow, radius * 1.02f, Offset(x, y - radius * 0.18f + 4f))
        scope.drawCircle(cloudShadow, radius * 0.82f, Offset(x + radius * 0.7f, y + 4f))

        // Main cloud puffs
        scope.drawCircle(cloudWhite, radius * 0.7f, Offset(x - radius * 0.6f, y))
        scope.drawCircle(cloudWhite, radius, Offset(x, y - radius * 0.2f))
        scope.drawCircle(cloudWhite, radius * 0.8f, Offset(x + radius * 0.7f, y))
        scope.drawCircle(cloudWhite, radius * 0.55f, Offset(x + radius * 0.25f, y + radius * 0.35f))
        scope.drawCircle(cloudWhite, radius * 0.55f, Offset(x - radius * 0.25f, y + radius * 0.35f))
    }

    // --- SLINGSHOT RENDERING (3D Wood Shading & Dynamic Elastic Bands) ---

    fun drawSlingshotBack(
        scope: DrawScope,
        anchor: Vector2D,
        pullPos: Vector2D,
        isPulling: Boolean
    ) {
        val leftFork = Offset(anchor.x - 22f, anchor.y - 48f)
        val basePos = Offset(anchor.x, anchor.y + 75f)

        // 1. Wooden Main Post (Rich Carved Bark)
        scope.drawLine(
            color = Color(0xFF3E2723), // Dark shadow
            start = Offset(anchor.x + 3f, basePos.y),
            end = Offset(anchor.x + 3f, anchor.y),
            strokeWidth = 20f,
            cap = StrokeCap.Round
        )
        scope.drawLine(
            color = Color(0xFF5D4037), // Primary wood
            start = Offset(anchor.x, basePos.y),
            end = Offset(anchor.x, anchor.y),
            strokeWidth = 18f,
            cap = StrokeCap.Round
        )
        // Wood highlight grain
        scope.drawLine(
            color = Color(0xFF8D6E63),
            start = Offset(anchor.x - 3f, basePos.y - 10f),
            end = Offset(anchor.x - 3f, anchor.y + 10f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        // 2. Back Left Fork Arm
        scope.drawLine(
            color = Color(0xFF4E342E),
            start = Offset(anchor.x, anchor.y),
            end = leftFork,
            strokeWidth = 16f,
            cap = StrokeCap.Round
        )
        // Metal Band / Bolt on Left Fork
        scope.drawCircle(Color(0xFFFFB300), 5f, leftFork)
        scope.drawCircle(Color(0xFF37474F), 2.5f, leftFork)

        // 3. Back Elastic Band (Stretches from left fork to pull position)
        if (isPulling) {
            val bandThickness = (8f - (anchor.distanceTo(pullPos) / 80f) * 3f).coerceIn(4f, 8f)
            scope.drawLine(
                color = Color(0xFF4E342E),
                start = leftFork,
                end = Offset(pullPos.x, pullPos.y),
                strokeWidth = bandThickness + 2f,
                cap = StrokeCap.Round
            )
            scope.drawLine(
                color = Color(0xFF8D6E63),
                start = leftFork,
                end = Offset(pullPos.x, pullPos.y),
                strokeWidth = bandThickness,
                cap = StrokeCap.Round
            )
        }
    }

    fun drawSlingshotFront(
        scope: DrawScope,
        anchor: Vector2D,
        pullPos: Vector2D,
        isPulling: Boolean
    ) {
        val rightFork = Offset(anchor.x + 22f, anchor.y - 48f)
        val leftFork = Offset(anchor.x - 22f, anchor.y - 48f)

        // 1. Front Right Fork Arm
        scope.drawLine(
            color = Color(0xFF6D4C41),
            start = Offset(anchor.x, anchor.y),
            end = rightFork,
            strokeWidth = 16f,
            cap = StrokeCap.Round
        )
        scope.drawLine(
            color = Color(0xFF8D6E63),
            start = Offset(anchor.x + 2f, anchor.y - 5f),
            end = Offset(rightFork.x + 1f, rightFork.y + 2f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        // Metal Bolt on Right Fork
        scope.drawCircle(Color(0xFFFFB300), 5f, rightFork)
        scope.drawCircle(Color(0xFF37474F), 2.5f, rightFork)

        // 2. Front Elastic Band & Leather Pouch
        if (isPulling) {
            val bandThickness = (8f - (anchor.distanceTo(pullPos) / 80f) * 3f).coerceIn(4f, 8f)
            scope.drawLine(
                color = Color(0xFF3E2723),
                start = rightFork,
                end = Offset(pullPos.x, pullPos.y),
                strokeWidth = bandThickness + 2f,
                cap = StrokeCap.Round
            )
            scope.drawLine(
                color = Color(0xFFA1887F),
                start = rightFork,
                end = Offset(pullPos.x, pullPos.y),
                strokeWidth = bandThickness,
                cap = StrokeCap.Round
            )

            // Dynamic Leather Pouch wrapping behind fruit
            scope.drawOval(
                color = Color(0xFF271916),
                topLeft = Offset(pullPos.x - 14f, pullPos.y - 14f),
                size = Size(28f, 28f)
            )
            scope.drawOval(
                color = Color(0xFF4E342E),
                topLeft = Offset(pullPos.x - 12f, pullPos.y - 12f),
                size = Size(24f, 24f)
            )
            scope.drawCircle(Color(0xFFFFB300), 2.5f, Offset(pullPos.x - 8f, pullPos.y))
            scope.drawCircle(Color(0xFFFFB300), 2.5f, Offset(pullPos.x + 8f, pullPos.y))
        } else {
            // Relaxed resting elastic band connecting both prongs
            val midSlack = Offset(anchor.x, anchor.y - 36f)
            val bandPath = Path().apply {
                moveTo(leftFork.x, leftFork.y)
                quadraticBezierTo(midSlack.x, midSlack.y, rightFork.x, rightFork.y)
            }
            scope.drawPath(bandPath, color = Color(0xFF4E342E), style = Stroke(width = 7f, cap = StrokeCap.Round))
            scope.drawPath(bandPath, color = Color(0xFF8D6E63), style = Stroke(width = 5f, cap = StrokeCap.Round))

            // Resting Leather Pouch
            scope.drawOval(
                color = Color(0xFF3E2723),
                topLeft = Offset(midSlack.x - 11f, midSlack.y - 8f),
                size = Size(22f, 16f)
            )
        }
    }

    // --- FRUIT HEROES RENDERING (Polished Eyes, Dynamic Shading & Abilities) ---

    fun drawFruit(scope: DrawScope, fruit: FruitEntity) {
        val currentAlpha = fruit.alpha.coerceIn(0f, 1f)
        if (currentAlpha <= 0.02f) return

        // Dynamic flying motion trail
        if (fruit.isLaunched && fruit.trail.size > 1) {
            val trailCount = fruit.trail.size
            for (i in 0 until trailCount - 1) {
                val progress = i.toFloat() / trailCount
                val alpha = progress * 0.55f * currentAlpha
                val p1 = fruit.trail[i]
                val p2 = fruit.trail[i + 1]
                val trailWidth = (fruit.radius * 0.75f) * progress

                scope.drawLine(
                    color = fruit.type.primaryColor.copy(alpha = alpha),
                    start = Offset(p1.x, p1.y),
                    end = Offset(p2.x, p2.y),
                    strokeWidth = trailWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        // Special Ability Aura Glow
        if (fruit.isLaunched && !fruit.hasUsedAbility && fruit.type != FruitType.APPLE && !fruit.isResting) {
            val auraColor = when (fruit.type) {
                FruitType.BANANA -> Color(0xFFFFEB3B)
                FruitType.ORANGE -> Color(0xFFFF9800)
                FruitType.CHERRY -> Color(0xFFFF4081)
                FruitType.DURIAN -> Color(0xFF76FF03)
                else -> Color.White
            }
            scope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(auraColor.copy(alpha = 0.6f * currentAlpha), Color.Transparent),
                    center = Offset(fruit.position.x, fruit.position.y),
                    radius = fruit.radius * 2.2f
                ),
                radius = fruit.radius * 2.2f,
                center = Offset(fruit.position.x, fruit.position.y)
            )
        }

        val center = Offset(fruit.position.x, fruit.position.y)
        scope.rotate(degrees = Math.toDegrees(fruit.angle.toDouble()).toFloat(), pivot = center) {
            if (currentAlpha < 0.99f) {
                scope.drawContext.canvas.saveLayer(
                    androidx.compose.ui.geometry.Rect(
                        center.x - fruit.radius * 2.5f,
                        center.y - fruit.radius * 2.5f,
                        center.x + fruit.radius * 2.5f,
                        center.y + fruit.radius * 2.5f
                    ),
                    Paint().apply { alpha = currentAlpha }
                )
            }

            when (fruit.type) {
                FruitType.APPLE -> drawAppleFruit(scope, center, fruit.radius)
                FruitType.BANANA -> drawBananaFruit(scope, center, fruit.radius)
                FruitType.ORANGE -> drawOrangeFruit(scope, center, fruit.radius)
                FruitType.CHERRY -> drawCherryFruit(scope, center, fruit.radius)
                FruitType.DURIAN -> drawDurianFruit(scope, center, fruit.radius)
            }

            if (currentAlpha < 0.99f) {
                scope.drawContext.canvas.restore()
            }
        }
    }

    private fun drawAppleFruit(scope: DrawScope, center: Offset, r: Float) {
        // Brown curved stem
        scope.drawLine(
            color = Color(0xFF3E2723),
            start = Offset(center.x, center.y - r * 0.75f),
            end = Offset(center.x + 5f, center.y - r * 1.4f),
            strokeWidth = 4.5f,
            cap = StrokeCap.Round
        )
        // Vibrant leaf with spine
        val leafCenter = Offset(center.x + 7f, center.y - r * 1.3f)
        scope.drawOval(
            brush = Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784))),
            topLeft = Offset(leafCenter.x - r * 0.4f, leafCenter.y - r * 0.25f),
            size = Size(r * 0.8f, r * 0.5f)
        )

        // Apple 3D Body (Dark base + bright radial specular sphere)
        scope.drawCircle(Color(0xFFB71C1C), r, center)
        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF5252), Color(0xFFE53935), Color(0xFFC62828)),
                center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                radius = r * 1.1f
            ),
            radius = r * 0.95f,
            center = center
        )

        // Gloss Specular Highlight
        scope.drawOval(
            brush = Brush.verticalGradient(listOf(Color(0xCCFFFFFF), Color(0x00FFFFFF))),
            topLeft = Offset(center.x - r * 0.65f, center.y - r * 0.7f),
            size = Size(r * 0.55f, r * 0.35f)
        )

        // Expressive Face
        drawExpressiveFace(scope, center, r, mood = CharacterMood.DETERMINED)
    }

    private fun drawBananaFruit(scope: DrawScope, center: Offset, r: Float) {
        val bananaPath = Path().apply {
            moveTo(center.x - r * 1.25f, center.y + r * 0.65f)
            cubicTo(
                center.x - r * 0.4f, center.y - r * 1.15f,
                center.x + r * 0.85f, center.y - r * 0.95f,
                center.x + r * 1.35f, center.y - r * 0.2f
            )
            cubicTo(
                center.x + r * 0.55f, center.y + r * 0.35f,
                center.x - r * 0.3f, center.y + r * 0.95f,
                center.x - r * 1.25f, center.y + r * 0.65f
            )
            close()
        }
        scope.drawPath(
            path = bananaPath,
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF176), Color(0xFFFFD54F), Color(0xFFFFA000)),
                center = center,
                radius = r * 1.5f
            )
        )
        // Peel Tips & Edge Shading
        scope.drawCircle(Color(0xFF5D4037), 4f, Offset(center.x - r * 1.2f, center.y + r * 0.6f))
        scope.drawCircle(Color(0xFF388E3C), 4f, Offset(center.x + r * 1.3f, center.y - r * 0.2f))

        // Aviator Goggles on Banana
        val goggleLeft = Offset(center.x - r * 0.22f, center.y - r * 0.12f)
        val goggleRight = Offset(center.x + r * 0.28f, center.y - r * 0.08f)
        scope.drawCircle(Color(0xFF37474F), r * 0.28f, goggleLeft)
        scope.drawCircle(Color(0xFF37474F), r * 0.28f, goggleRight)
        scope.drawCircle(Color(0xFF80DEEA), r * 0.22f, goggleLeft)
        scope.drawCircle(Color(0xFF80DEEA), r * 0.22f, goggleRight)
        scope.drawCircle(Color.White, r * 0.08f, Offset(goggleLeft.x - 2f, goggleLeft.y - 2f))
        scope.drawCircle(Color.White, r * 0.08f, Offset(goggleRight.x - 2f, goggleRight.y - 2f))

        // Smirk
        val smirk = Path().apply {
            moveTo(center.x - r * 0.1f, center.y + r * 0.28f)
            quadraticBezierTo(center.x + r * 0.12f, center.y + r * 0.38f, center.x + r * 0.25f, center.y + r * 0.22f)
        }
        scope.drawPath(smirk, color = Color(0xFF212121), style = Stroke(width = 3f, cap = StrokeCap.Round))
    }

    private fun drawOrangeFruit(scope: DrawScope, center: Offset, r: Float) {
        // Orange Sphere with 3D gradient
        scope.drawCircle(Color(0xFFD84315), r, center)
        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFB74D), Color(0xFFFF9800), Color(0xFFE65100)),
                center = Offset(center.x - r * 0.2f, center.y - r * 0.2f),
                radius = r * 1.1f
            ),
            radius = r * 0.95f,
            center = center
        )

        // Fuse Spark on Top
        scope.drawLine(
            color = Color(0xFF4E342E),
            start = Offset(center.x, center.y - r * 0.8f),
            end = Offset(center.x + 3f, center.y - r * 1.3f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        scope.drawCircle(Color(0xFFFFEB3B), 5f, Offset(center.x + 3f, center.y - r * 1.3f))
        scope.drawCircle(Color(0xFFFF5722), 2.5f, Offset(center.x + 3f, center.y - r * 1.3f))

        // Citrus Dimples
        scope.drawCircle(Color(0x33FFFFFF), 2.5f, Offset(center.x + r * 0.45f, center.y - r * 0.3f))
        scope.drawCircle(Color(0x33FFFFFF), 2.5f, Offset(center.x - r * 0.5f, center.y + r * 0.25f))

        // Determined hero face
        drawExpressiveFace(scope, center, r, mood = CharacterMood.DETERMINED)
    }

    private fun drawCherryFruit(scope: DrawScope, center: Offset, r: Float) {
        // Glossy Crimson Cherry
        scope.drawCircle(Color(0xFF4A148C), r, center)
        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF4081), Color(0xFFE91E63), Color(0xFF880E4F)),
                center = Offset(center.x - r * 0.3f, center.y - r * 0.3f),
                radius = r * 1.1f
            ),
            radius = r * 0.95f,
            center = center
        )
        // Green curved twig
        scope.drawLine(
            color = Color(0xFF2E7D32),
            start = Offset(center.x, center.y - r * 0.75f),
            end = Offset(center.x + 9f, center.y - r * 1.45f),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )
        // Specular shine
        scope.drawCircle(Color(0xDDFFFFFF), r * 0.24f, Offset(center.x - r * 0.35f, center.y - r * 0.35f))

        // Playful Happy Face
        drawExpressiveFace(scope, center, r, mood = CharacterMood.HAPPY)
    }

    private fun drawDurianFruit(scope: DrawScope, center: Offset, r: Float) {
        // Outer Spikes (3D shaded thorns)
        val numSpikes = 12
        for (i in 0 until numSpikes) {
            val a = (i.toFloat() / numSpikes) * 2f * PI.toFloat()
            val spikeBase1 = Offset(center.x + cos(a - 0.15f) * r * 0.85f, center.y + sin(a - 0.15f) * r * 0.85f)
            val spikeBase2 = Offset(center.x + cos(a + 0.15f) * r * 0.85f, center.y + sin(a + 0.15f) * r * 0.85f)
            val spikeTip = Offset(center.x + cos(a) * (r + 8f), center.y + sin(a) * (r + 8f))

            val spikePath = Path().apply {
                moveTo(spikeBase1.x, spikeBase1.y)
                lineTo(spikeTip.x, spikeTip.y)
                lineTo(spikeBase2.x, spikeBase2.y)
                close()
            }
            scope.drawPath(spikePath, color = Color(0xFF33691E))
        }

        // Heavy Armored Core
        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFAED581), Color(0xFF7CB342), Color(0xFF33691E)),
                center = Offset(center.x - r * 0.2f, center.y - r * 0.2f),
                radius = r * 1.1f
            ),
            radius = r * 0.88f,
            center = center
        )

        // Warrior Iron Headband
        scope.drawRect(
            color = Color(0xFF37474F),
            topLeft = Offset(center.x - r * 0.8f, center.y - r * 0.35f),
            size = Size(r * 1.6f, r * 0.32f)
        )
        scope.drawCircle(Color(0xFFFFD54F), 3.5f, Offset(center.x, center.y - r * 0.19f))

        // Fierce strong eyes
        drawExpressiveFace(scope, center, r, mood = CharacterMood.FIERCE)
    }

    enum class CharacterMood { HAPPY, DETERMINED, FIERCE }

    private fun drawExpressiveFace(
        scope: DrawScope,
        center: Offset,
        r: Float,
        mood: CharacterMood
    ) {
        val eyeRadius = r * 0.18f
        val eyeLeft = Offset(center.x - r * 0.32f, center.y - r * 0.05f)
        val eyeRight = Offset(center.x + r * 0.32f, center.y - r * 0.05f)

        // Eyes (Glossy Cartoon Pupils)
        scope.drawCircle(Color(0xFF212121), eyeRadius, eyeLeft)
        scope.drawCircle(Color(0xFF212121), eyeRadius, eyeRight)

        // Bright highlights
        scope.drawCircle(Color.White, eyeRadius * 0.5f, Offset(eyeLeft.x - 1.5f, eyeLeft.y - 1.5f))
        scope.drawCircle(Color.White, eyeRadius * 0.5f, Offset(eyeRight.x - 1.5f, eyeRight.y - 1.5f))
        scope.drawCircle(Color.White, eyeRadius * 0.2f, Offset(eyeLeft.x + 2f, eyeLeft.y + 2f))
        scope.drawCircle(Color.White, eyeRadius * 0.2f, Offset(eyeRight.x + 2f, eyeRight.y + 2f))

        // Rosy Cheeks
        scope.drawOval(Color(0x55FF4081), topLeft = Offset(eyeLeft.x - 5f, eyeLeft.y + r * 0.2f), size = Size(r * 0.35f, r * 0.18f))
        scope.drawOval(Color(0x55FF4081), topLeft = Offset(eyeRight.x - 2f, eyeRight.y + r * 0.2f), size = Size(r * 0.35f, r * 0.18f))

        when (mood) {
            CharacterMood.HAPPY -> {
                val mouth = Path().apply {
                    moveTo(center.x - r * 0.22f, center.y + r * 0.22f)
                    quadraticBezierTo(center.x, center.y + r * 0.42f, center.x + r * 0.22f, center.y + r * 0.22f)
                }
                scope.drawPath(mouth, color = Color(0xFF212121), style = Stroke(width = 3f, cap = StrokeCap.Round))
            }
            CharacterMood.DETERMINED -> {
                // Eyebrows
                scope.drawLine(Color(0xFF212121), Offset(eyeLeft.x - 6f, eyeLeft.y - eyeRadius - 2f), Offset(eyeLeft.x + 6f, eyeLeft.y - eyeRadius + 3f), 2.5f, StrokeCap.Round)
                scope.drawLine(Color(0xFF212121), Offset(eyeRight.x + 6f, eyeRight.y - eyeRadius - 2f), Offset(eyeRight.x - 6f, eyeRight.y - eyeRadius + 3f), 2.5f, StrokeCap.Round)

                val smirk = Path().apply {
                    moveTo(center.x - r * 0.15f, center.y + r * 0.28f)
                    quadraticBezierTo(center.x + r * 0.05f, center.y + r * 0.36f, center.x + r * 0.2f, center.y + r * 0.25f)
                }
                scope.drawPath(smirk, color = Color(0xFF212121), style = Stroke(width = 3f, cap = StrokeCap.Round))
            }
            CharacterMood.FIERCE -> {
                // Heavy Brow
                scope.drawLine(Color(0xFF212121), Offset(eyeLeft.x - 8f, eyeLeft.y - eyeRadius - 4f), Offset(eyeLeft.x + 6f, eyeLeft.y - eyeRadius + 4f), 3.5f, StrokeCap.Round)
                scope.drawLine(Color(0xFF212121), Offset(eyeRight.x + 8f, eyeRight.y - eyeRadius - 4f), Offset(eyeRight.x - 6f, eyeRight.y - eyeRadius + 4f), 3.5f, StrokeCap.Round)

                scope.drawLine(Color(0xFF212121), Offset(center.x - r * 0.22f, center.y + r * 0.32f), Offset(center.x + r * 0.22f, center.y + r * 0.26f), 3.5f, StrokeCap.Round)
            }
        }
    }

    // --- MONKEYS INVADERS RENDERING (Animated Expressions & Outfits) ---

    fun drawMonkey(scope: DrawScope, monkey: MonkeyEntity) {
        val center = Offset(monkey.position.x, monkey.position.y)
        val r = monkey.radius

        scope.rotate(degrees = Math.toDegrees(monkey.angle.toDouble()).toFloat(), pivot = center) {
            // 1. Ears with inner pink flesh
            val earR = r * 0.46f
            val earLeft = Offset(center.x - r * 0.88f, center.y - r * 0.22f)
            val earRight = Offset(center.x + r * 0.88f, center.y - r * 0.22f)

            scope.drawCircle(Color(0xFF5D4037), earR, earLeft)
            scope.drawCircle(Color(0xFFFFCCBC), earR * 0.55f, earLeft)
            scope.drawCircle(Color(0xFF5D4037), earR, earRight)
            scope.drawCircle(Color(0xFFFFCCBC), earR * 0.55f, earRight)

            // 2. Head (Fur Gradient)
            scope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8D6E63), monkey.type.primaryColor, Color(0xFF3E2723)),
                    center = Offset(center.x - r * 0.2f, center.y - r * 0.2f),
                    radius = r * 1.1f
                ),
                radius = r,
                center = center
            )

            // 3. Muzzle Face (Beige Oval)
            scope.drawOval(
                brush = Brush.verticalGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFCCBC))),
                topLeft = Offset(center.x - r * 0.68f, center.y - r * 0.08f),
                size = Size(r * 1.36f, r * 0.96f)
            )

            // 4. Little Cute Nose
            scope.drawCircle(Color(0xFF3E2723), r * 0.12f, Offset(center.x, center.y + r * 0.15f))

            // 5. Dynamic Eyes & Expressions
            if (monkey.state == MonkeyState.HIT) {
                // Dizzy / Knocked out (X eyes & wavy mouth)
                drawCross(scope, Offset(center.x - r * 0.32f, center.y - r * 0.18f), r * 0.18f)
                drawCross(scope, Offset(center.x + r * 0.32f, center.y - r * 0.18f), r * 0.18f)
                scope.drawLine(Color(0xFF212121), Offset(center.x - r * 0.22f, center.y + r * 0.45f), Offset(center.x + r * 0.22f, center.y + r * 0.45f), 3f, StrokeCap.Round)

                // Orbiting yellow dizzy stars around head
                val starOrbitTime = (System.currentTimeMillis() % 1000) / 1000f * 2f * PI.toFloat()
                for (s in 0..2) {
                    val starAngle = starOrbitTime + (s * (2f * PI.toFloat() / 3f))
                    val sx = center.x + cos(starAngle) * (r * 1.15f)
                    val sy = (center.y - r * 0.9f) + sin(starAngle) * (r * 0.35f)
                    scope.drawCircle(Color(0xFFFFD54F), 3.5f, Offset(sx, sy))
                    scope.drawCircle(Color.White, 1.5f, Offset(sx, sy))
                }
            } else if (monkey.state == MonkeyState.SCARED) {
                // Wide terrified eyes & sweat drop
                scope.drawCircle(Color.White, r * 0.34f, Offset(center.x - r * 0.32f, center.y - r * 0.18f))
                scope.drawCircle(Color.White, r * 0.34f, Offset(center.x + r * 0.32f, center.y - r * 0.18f))
                scope.drawCircle(Color.Black, r * 0.16f, Offset(center.x - r * 0.32f, center.y - r * 0.18f))
                scope.drawCircle(Color.Black, r * 0.16f, Offset(center.x + r * 0.32f, center.y - r * 0.18f))
                scope.drawCircle(Color.White, 2.5f, Offset(center.x - r * 0.36f, center.y - r * 0.22f))
                scope.drawCircle(Color.White, 2.5f, Offset(center.x + r * 0.28f, center.y - r * 0.22f))
                // Terrified open 'O' mouth with pink tongue
                scope.drawOval(Color(0xFF212121), topLeft = Offset(center.x - r * 0.2f, center.y + r * 0.34f), size = Size(r * 0.4f, r * 0.32f))
                scope.drawOval(Color(0xFFFF8A80), topLeft = Offset(center.x - r * 0.12f, center.y + r * 0.48f), size = Size(r * 0.24f, r * 0.15f))
                // Dynamic flying panic sweat drops
                scope.drawCircle(Color(0xFF42A5F5), 4.5f, Offset(center.x + r * 0.85f, center.y - r * 0.5f))
                scope.drawCircle(Color(0xFF81D4FA), 3f, Offset(center.x - r * 0.85f, center.y - r * 0.4f))
            } else if (monkey.isBlinking) {
                // Relaxed blinking lines
                scope.drawLine(Color(0xFF212121), Offset(center.x - r * 0.45f, center.y - r * 0.15f), Offset(center.x - r * 0.15f, center.y - r * 0.15f), 3f, StrokeCap.Round)
                scope.drawLine(Color(0xFF212121), Offset(center.x + r * 0.15f, center.y - r * 0.15f), Offset(center.x + r * 0.45f, center.y - r * 0.15f), 3f, StrokeCap.Round)
                scope.drawLine(Color(0xFF212121), Offset(center.x - r * 0.2f, center.y + r * 0.45f), Offset(center.x + r * 0.2f, center.y + r * 0.45f), 2.5f, StrokeCap.Round)
            } else {
                // Mischievous grinning monkey
                scope.drawCircle(Color.White, r * 0.26f, Offset(center.x - r * 0.3f, center.y - r * 0.18f))
                scope.drawCircle(Color.White, r * 0.26f, Offset(center.x + r * 0.3f, center.y - r * 0.18f))
                scope.drawCircle(Color.Black, r * 0.13f, Offset(center.x - r * 0.28f, center.y - r * 0.18f))
                scope.drawCircle(Color.Black, r * 0.13f, Offset(center.x + r * 0.28f, center.y - r * 0.18f))
                scope.drawCircle(Color.White, 2f, Offset(center.x - r * 0.31f, center.y - r * 0.21f))
                scope.drawCircle(Color.White, 2f, Offset(center.x + r * 0.25f, center.y - r * 0.21f))

                val mouth = Path().apply {
                    moveTo(center.x - r * 0.25f, center.y + r * 0.42f)
                    quadraticBezierTo(center.x, center.y + r * 0.58f, center.x + r * 0.25f, center.y + r * 0.42f)
                }
                scope.drawPath(mouth, color = Color(0xFF212121), style = Stroke(width = 3f, cap = StrokeCap.Round))
            }

            // 6. Archetype Accessories: Armor, Helmets, Bucklers, Golden Crown
            when (monkey.type) {
                MonkeyType.ARMORED -> {
                    // Viking / Hardhat Dome
                    scope.drawArc(
                        brush = Brush.verticalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(center.x - r * 1.1f, center.y - r * 1.25f),
                        size = Size(r * 2.2f, r * 1.6f)
                    )
                    // Metal Rim
                    scope.drawLine(
                        color = Color(0xFFFFA000),
                        start = Offset(center.x - r * 1.15f, center.y - r * 0.45f),
                        end = Offset(center.x + r * 1.15f, center.y - r * 0.45f),
                        strokeWidth = 7f,
                        cap = StrokeCap.Round
                    )
                    // Horn tip
                    scope.drawCircle(Color(0xFFECEFF1), 5f, Offset(center.x, center.y - r * 1.3f))
                }
                MonkeyType.SHIELDED -> {
                    // Wooden / Iron Buckler on side
                    scope.drawRoundRect(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF5D4037), Color(0xFF8D6E63))),
                        topLeft = Offset(center.x - r * 1.4f, center.y - r * 0.75f),
                        size = Size(r * 0.5f, r * 1.5f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    scope.drawCircle(Color(0xFFFFD54F), 4.5f, Offset(center.x - r * 1.15f, center.y))
                }
                MonkeyType.HEAVY -> {
                    // Regal 3-Point Gold Crown
                    val crown = Path().apply {
                        moveTo(center.x - r * 0.65f, center.y - r * 0.85f)
                        lineTo(center.x - r * 0.65f, center.y - r * 1.4f)
                        lineTo(center.x - r * 0.22f, center.y - r * 1.1f)
                        lineTo(center.x, center.y - r * 1.55f)
                        lineTo(center.x + r * 0.22f, center.y - r * 1.1f)
                        lineTo(center.x + r * 0.65f, center.y - r * 1.4f)
                        lineTo(center.x + r * 0.65f, center.y - r * 0.85f)
                        close()
                    }
                    scope.drawPath(crown, brush = Brush.verticalGradient(listOf(Color(0xFFFFEB3B), Color(0xFFFFB300))))
                    scope.drawCircle(Color(0xFFD50000), 3.5f, Offset(center.x, center.y - r * 1.4f))
                }
                else -> {}
            }
        }
    }

    private fun drawCross(scope: DrawScope, center: Offset, size: Float) {
        scope.drawLine(Color(0xFF212121), Offset(center.x - size, center.y - size), Offset(center.x + size, center.y + size), 3.2f, StrokeCap.Round)
        scope.drawLine(Color(0xFF212121), Offset(center.x - size, center.y + size), Offset(center.x + size, center.y - size), 3.2f, StrokeCap.Round)
    }

    // --- DESTRUCTIBLE MATERIALS & BLOCKS RENDERING ---

    fun drawBlock(scope: DrawScope, block: BlockEntity) {
        val center = Offset(block.position.x, block.position.y)
        val w = block.width
        val h = block.height
        val halfW = w / 2f
        val halfH = h / 2f

        scope.rotate(degrees = Math.toDegrees(block.angle.toDouble()).toFloat(), pivot = center) {
            val rectTopLeft = Offset(center.x - halfW, center.y - halfH)
            val rectSize = Size(w, h)

            // 1. Base Material Shading
            when (block.material) {
                MaterialType.WOOD -> {
                    scope.drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFBCAAA4), Color(0xFF8D6E63), Color(0xFF6D4C41)),
                            startY = rectTopLeft.y,
                            endY = rectTopLeft.y + h
                        ),
                        topLeft = rectTopLeft,
                        size = rectSize
                    )
                    // Wood Grain horizontal / vertical lines
                    scope.drawLine(
                        color = Color(0xFFD7CCC8).copy(alpha = 0.6f),
                        start = Offset(rectTopLeft.x + 3f, rectTopLeft.y + 3f),
                        end = Offset(rectTopLeft.x + w - 3f, rectTopLeft.y + 3f),
                        strokeWidth = 2.5f
                    )
                    if (h > 35f) {
                        scope.drawLine(
                            color = Color(0xFF4E342E).copy(alpha = 0.5f),
                            start = Offset(rectTopLeft.x + 4f, center.y),
                            end = Offset(rectTopLeft.x + w - 4f, center.y),
                            strokeWidth = 2f
                        )
                    }
                }
                MaterialType.GLASS -> {
                    // Crystalline Ice / Glass (translucent cyan)
                    scope.drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0x88E0F7FA), Color(0x9980DEEA), Color(0x774DD0E1)),
                            startY = rectTopLeft.y,
                            endY = rectTopLeft.y + h
                        ),
                        topLeft = rectTopLeft,
                        size = rectSize
                    )
                    // High-gloss diagonal reflections
                    scope.drawLine(
                        color = Color(0xCCFFFFFF),
                        start = Offset(rectTopLeft.x + 4f, rectTopLeft.y + h - 4f),
                        end = Offset(rectTopLeft.x + w - 4f, rectTopLeft.y + 4f),
                        strokeWidth = 2.5f
                    )
                }
                MaterialType.STONE -> {
                    // Chiseled Granite Stone
                    scope.drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFB0BEC5), Color(0xFF78909C), Color(0xFF546E7A)),
                            startY = rectTopLeft.y,
                            endY = rectTopLeft.y + h
                        ),
                        topLeft = rectTopLeft,
                        size = rectSize
                    )
                    // Top beveled highlight
                    scope.drawLine(
                        color = Color(0xFFCFD8DC),
                        start = Offset(rectTopLeft.x + 2f, rectTopLeft.y + 2f),
                        end = Offset(rectTopLeft.x + w - 2f, rectTopLeft.y + 2f),
                        strokeWidth = 2.5f
                    )
                }
                MaterialType.METAL -> {
                    // Heavy Iron with Metallic Sheen
                    scope.drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF37474F), Color(0xFF546E7A), Color(0xFF263238)),
                            startX = rectTopLeft.x,
                            endX = rectTopLeft.x + w
                        ),
                        topLeft = rectTopLeft,
                        size = rectSize
                    )
                    // 4 Corner Rivet Bolts
                    val r = 3f
                    scope.drawCircle(Color(0xFFFFD54F), r, Offset(rectTopLeft.x + 5f, rectTopLeft.y + 5f))
                    scope.drawCircle(Color(0xFFFFD54F), r, Offset(rectTopLeft.x + w - 5f, rectTopLeft.y + 5f))
                    scope.drawCircle(Color(0xFFFFD54F), r, Offset(rectTopLeft.x + 5f, rectTopLeft.y + h - 5f))
                    scope.drawCircle(Color(0xFFFFD54F), r, Offset(rectTopLeft.x + w - 5f, rectTopLeft.y + h - 5f))
                }
            }

            // 2. Beveled Outline Border
            scope.drawRect(
                color = block.material.borderColor,
                topLeft = rectTopLeft,
                size = rectSize,
                style = Stroke(width = 2.2f)
            )

            // 3. Dynamic Structural Damage Fractures
            if (block.crackIntensity > 0.08f) {
                val crackAlpha = (block.crackIntensity * 1.1f).coerceIn(0.2f, 1f)
                val crackColor = Color(0xFF1B1B1B).copy(alpha = crackAlpha)

                // Primary fracture line
                scope.drawLine(
                    color = crackColor,
                    start = Offset(center.x - halfW * 0.7f, center.y - halfH * 0.6f),
                    end = Offset(center.x + halfW * 0.3f, center.y + halfH * 0.4f),
                    strokeWidth = 3f
                )
                // Secondary branching fractures
                if (block.crackIntensity > 0.4f) {
                    scope.drawLine(
                        color = crackColor,
                        start = Offset(center.x + halfW * 0.3f, center.y + halfH * 0.4f),
                        end = Offset(center.x + halfW * 0.8f, center.y - halfH * 0.3f),
                        strokeWidth = 2.5f
                    )
                }
                if (block.crackIntensity > 0.7f) {
                    scope.drawLine(
                        color = crackColor,
                        start = Offset(center.x - halfW * 0.2f, center.y - halfH * 0.1f),
                        end = Offset(center.x - halfW * 0.6f, center.y + halfH * 0.6f),
                        strokeWidth = 2.2f
                    )
                }
            }
        }
    }

    // --- PARTICLES, DEBRIS & FX ---

    fun drawParticles(scope: DrawScope, particles: List<ParticleEntity>) {
        for (p in particles) {
            val center = Offset(p.position.x, p.position.y)
            val currentAlpha = p.alpha

            when (p.shape) {
                ParticleShape.CIRCLE -> {
                    scope.drawCircle(
                        color = p.color.copy(alpha = currentAlpha),
                        radius = (p.size * (1f - p.progress * 0.35f)).coerceAtLeast(1f),
                        center = center
                    )
                }
                ParticleShape.SHARD -> {
                    scope.rotate(degrees = Math.toDegrees(p.rotation.toDouble()).toFloat(), pivot = center) {
                        scope.drawRect(
                            color = p.color.copy(alpha = currentAlpha),
                            topLeft = Offset(center.x - p.size / 2f, center.y - p.size / 2f),
                            size = Size(p.size, p.size * 0.6f)
                        )
                    }
                }
                ParticleShape.SMOKE -> {
                    val radius = p.size * (0.8f + p.progress * 0.9f)
                    scope.drawCircle(
                        color = p.color.copy(alpha = currentAlpha * 0.65f),
                        radius = radius,
                        center = center
                    )
                }
                ParticleShape.STAR -> {
                    scope.drawCircle(
                        color = p.color.copy(alpha = currentAlpha),
                        radius = p.size * 0.65f,
                        center = center
                    )
                    scope.drawLine(
                        color = p.color.copy(alpha = currentAlpha),
                        start = Offset(center.x - p.size, center.y),
                        end = Offset(center.x + p.size, center.y),
                        strokeWidth = 2.5f
                    )
                    scope.drawLine(
                        color = p.color.copy(alpha = currentAlpha),
                        start = Offset(center.x, center.y - p.size),
                        end = Offset(center.x, center.y + p.size),
                        strokeWidth = 2.5f
                    )
                }
                ParticleShape.RING -> {
                    val ringRadius = p.size * p.progress
                    scope.drawCircle(
                        color = p.color.copy(alpha = currentAlpha * 0.8f),
                        radius = ringRadius,
                        center = center,
                        style = Stroke(width = 6f * (1f - p.progress))
                    )
                }
                else -> {}
            }
        }
    }

    // --- TRAJECTORY PREDICTION WITH TARGET RETICLE ---

    fun drawTrajectory(scope: DrawScope, points: List<Vector2D>, fruitType: FruitType) {
        if (points.size < 2) return

        val count = points.size
        for (i in 0 until count) {
            val pt = points[i]
            val t = i.toFloat() / count
            val radius = (7f * (1f - t * 0.55f)).coerceAtLeast(2.5f)
            val alpha = (0.95f * (1f - t * 0.75f)).coerceIn(0.15f, 1f)

            // Outer glowing dot
            scope.drawCircle(
                color = fruitType.primaryColor.copy(alpha = alpha),
                radius = radius,
                center = Offset(pt.x, pt.y)
            )
            // Inner crisp white core
            scope.drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius * 0.45f,
                center = Offset(pt.x, pt.y)
            )
        }

        // Target Landing Crosshair at end of arc
        val landingPt = points.last()
        scope.drawCircle(
            color = fruitType.primaryColor.copy(alpha = 0.75f),
            radius = 12f,
            center = Offset(landingPt.x, landingPt.y),
            style = Stroke(width = 2.5f)
        )
        scope.drawCircle(Color.White, 3f, Offset(landingPt.x, landingPt.y))
    }
}
