package com.example.rulytopia.physics

import androidx.compose.ui.graphics.Color
import com.example.rulytopia.audio.SoundManager
import com.example.rulytopia.model.BlockDef
import com.example.rulytopia.model.BlockEntity
import com.example.rulytopia.model.BlockShape
import com.example.rulytopia.model.FruitEntity
import com.example.rulytopia.model.FruitType
import com.example.rulytopia.model.LevelDef
import com.example.rulytopia.model.MaterialType
import com.example.rulytopia.model.MonkeyDef
import com.example.rulytopia.model.MonkeyEntity
import com.example.rulytopia.model.MonkeyState
import com.example.rulytopia.model.MonkeyType
import com.example.rulytopia.model.ParticleEntity
import com.example.rulytopia.model.ParticleShape
import com.example.rulytopia.model.Vector2D
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * High-performance 2D Physics Engine for Rulytopia.
 * Features:
 * - Granular, support-based structural physics (only genuinely unsupported or impacted blocks fall).
 * - Stable resting equilibrium (structures remain perfectly rock solid until physically struck).
 * - Multi-substep fixed-delta numerical integration.
 * - Material-aware friction, restitution, and durability damage.
 * - Dynamic slope rolling and crush damage for monkeys.
 * - Fruit abilities (Banana Turbo Boost, Orange Citrus Blast, Cherry Triple Split, Durian Seismic Quake).
 */
class PhysicsEngine(
    private val soundManager: SoundManager,
    private val onScoreAdded: (points: Int, position: Vector2D, text: String, isCrit: Boolean) -> Unit,
    private val onMonkeyDefeated: (monkey: MonkeyEntity) -> Unit,
    private val onBlockBroken: (block: BlockEntity) -> Unit
) {
    val fruits = mutableListOf<FruitEntity>()
    val blocks = mutableListOf<BlockEntity>()
    val monkeys = mutableListOf<MonkeyEntity>()
    val particles = mutableListOf<ParticleEntity>()

    var gravity = Vector2D(0f, 540f)
    var groundY = 480f
    var worldWidth = 1750f
    var worldHeight = 600f

    enum class ShakeImpact(val trauma: Float) {
        NONE(0f),
        MINOR(0.08f),
        NORMAL(0.18f),
        STRONG(0.30f),
        SPECIAL_ORANGE(0.38f),
        SPECIAL_DURIAN(0.50f)
    }

    var onScreenShake: ((intensity: Float) -> Unit)? = null
    var onShakeImpact: ((ShakeImpact) -> Unit)? = null
    var hasFirstShotOccurred: Boolean = false

    private val random = Random(System.currentTimeMillis())

    fun loadLevel(levelDef: LevelDef) {
        fruits.clear()
        blocks.clear()
        monkeys.clear()
        particles.clear()
        hasFirstShotOccurred = false

        groundY = levelDef.groundY
        worldWidth = levelDef.worldWidth
        worldHeight = levelDef.worldHeight

        // Build Blocks in stable resting equilibrium
        levelDef.blocks.forEachIndexed { index, b ->
            blocks.add(
                BlockEntity(
                    id = index.toLong() + 1,
                    position = Vector2D(b.x, b.y),
                    width = b.width,
                    height = b.height,
                    material = b.material,
                    shape = b.shape,
                    angle = b.angle,
                    isResting = true
                )
            )
        }

        // Build Monkeys in stable resting equilibrium
        levelDef.monkeys.forEachIndexed { index, m ->
            monkeys.add(
                MonkeyEntity(
                    id = index.toLong() + 1,
                    position = Vector2D(m.x, m.y),
                    type = m.type,
                    isResting = true
                )
            )
        }
    }

    fun step(dt: Float) {
        val clampedDt = dt.coerceIn(0.001f, 0.033f)
        val substeps = 6
        val subDt = clampedDt / substeps.toFloat()

        for (step in 0 until substeps) {
            updateSubstep(subDt)
        }

        // Update particle lifespans
        val particleIter = particles.iterator()
        while (particleIter.hasNext()) {
            val p = particleIter.next()
            p.lifeTime -= clampedDt
            if (p.lifeTime <= 0f) {
                particleIter.remove()
            } else {
                p.velocity += gravity * (clampedDt * 0.4f * p.gravityFactor)
                p.position += p.velocity * clampedDt
            }
        }
    }

    private fun updateSubstep(dt: Float) {
        // 1. Integrate Fruits
        for (fruit in fruits) {
            if (!fruit.isLaunched || fruit.isDead) continue

            fruit.flightTime += dt

            // Trailing visual particles
            if (fruit.velocity.lengthSquared() > 8000f && random.nextFloat() < 0.35f) {
                particles.add(
                    ParticleEntity(
                        position = fruit.position.copy() + Vector2D((random.nextFloat() - 0.5f) * 8f, (random.nextFloat() - 0.5f) * 8f),
                        velocity = (fruit.velocity * -0.15f) + Vector2D((random.nextFloat() - 0.5f) * 30f, (random.nextFloat() - 0.5f) * 30f),
                        color = fruit.type.primaryColor.copy(alpha = 0.6f),
                        size = 5f + random.nextFloat() * 4f,
                        maxLifeTime = 0.3f,
                        shape = ParticleShape.CIRCLE
                    )
                )
            }

            // Airborne physics with subtle, time-proportional aerodynamic drag
            fruit.velocity += gravity * dt
            fruit.velocity *= (1.0f - 0.012f * dt).coerceIn(0.9f, 1.0f)
            fruit.position += fruit.velocity * dt
            fruit.angle += fruit.velocity.x * (dt * 0.04f)

            // Ground collision
            if (fruit.position.y + fruit.radius >= groundY) {
                fruit.position.y = groundY - fruit.radius
                if (fruit.velocity.y > 0) {
                    fruit.velocity.y = -fruit.velocity.y * (fruit.restitution * 0.45f)
                    fruit.velocity.x *= 0.88f // ground rolling friction
                }
            }

            // Screen boundaries
            if (fruit.position.x - fruit.radius < 0f) {
                fruit.position.x = fruit.radius
                fruit.velocity.x = -fruit.velocity.x * fruit.restitution
            }
            if (fruit.position.x + fruit.radius > worldWidth) {
                fruit.position.x = worldWidth - fruit.radius
                fruit.velocity.x = -fruit.velocity.x * fruit.restitution
            }

            // Fruit settles and disappears cleanly after resting or landing
            val isSlow = fruit.velocity.lengthSquared() < 36f
            val isOnGround = fruit.position.y + fruit.radius >= groundY - 3f

            if (isOnGround && isSlow && fruit.flightTime > 0.8f) {
                fruit.isResting = true
            }

            // Generous airborne lifespan for long-distance arcs across wide arenas
            if (fruit.flightTime > 4.5f || (fruit.isResting && fruit.flightTime > 1.5f)) {
                fruit.isDead = true
                fruit.isResting = true
                spawnJuiceParticles(fruit.position, fruit.type.primaryColor, 8)
            }
        }

        // 2. Integrate Active Dynamic Blocks (Resting blocks stay static in equilibrium)
        for (block in blocks) {
            if (block.isBroken) continue

            block.damageCooldown = (block.damageCooldown - dt).coerceAtLeast(0f)

            if (block.isResting) {
                block.velocity = Vector2D(0f, 0f)
                block.angularVelocity = 0f
                continue
            }

            // Active physics integration
            block.velocity += gravity * dt
            block.velocity *= 0.992f
            block.angularVelocity *= 0.965f

            block.position += block.velocity * dt
            block.angle += block.angularVelocity * dt

            // Ground collision
            val halfW = block.width / 2f
            val halfH = block.height / 2f
            val cosA = cos(block.angle)
            val sinA = sin(block.angle)

            val cornerOffsets = arrayOf(
                Vector2D(-halfW * cosA + halfH * sinA, -halfW * sinA - halfH * cosA),
                Vector2D(halfW * cosA + halfH * sinA, halfW * sinA - halfH * cosA),
                Vector2D(halfW * cosA - halfH * sinA, halfW * sinA + halfH * cosA),
                Vector2D(-halfW * cosA - halfH * sinA, -halfW * sinA + halfH * cosA)
            )

            var lowestCornerY = block.position.y
            var lowestCornerOffset = Vector2D(0f, 0f)
            for (c in cornerOffsets) {
                val worldCornerY = block.position.y + c.y
                if (worldCornerY > lowestCornerY) {
                    lowestCornerY = worldCornerY
                    lowestCornerOffset = c
                }
            }

            if (lowestCornerY >= groundY) {
                val penetration = lowestCornerY - groundY
                block.position.y -= penetration

                if (block.velocity.y > 0) {
                    val fallSpeed = block.velocity.y
                    block.velocity.y = -block.velocity.y * (block.material.restitution * 0.18f)
                    block.velocity.x *= block.material.friction

                    // Corner ground pivot torque
                    val groundNormal = Vector2D(0f, -1f)
                    val groundImpulse = groundNormal * (fallSpeed * block.mass * 0.2f)
                    val groundTorque = lowestCornerOffset.cross(groundImpulse) / (block.mass * 1000f)
                    block.angularVelocity += groundTorque

                    // Fall impact damage
                    if (fallSpeed > 300f && block.damageCooldown <= 0f) {
                        val fallDamage = (fallSpeed - 240f) * block.material.density * 0.10f
                        block.takeDamage(fallDamage)
                        block.damageCooldown = 0.12f
                        soundManager.playImpact(block.material, fallDamage)
                        if (block.isBroken) {
                            handleBlockBreak(block)
                        }
                    }
                }

                // Settle to resting equilibrium on ground if velocity has subsided
                if (block.velocity.lengthSquared() < 16f && abs(block.angularVelocity) < 0.10f) {
                    block.isResting = true
                    block.velocity = Vector2D(0f, 0f)
                    block.angularVelocity = 0f
                }
            }

            // Screen side bounds
            if (block.position.x - halfW < 0f) {
                block.position.x = halfW
                block.velocity.x = -block.velocity.x * 0.3f
            }
            if (block.position.x + halfW > worldWidth) {
                block.position.x = worldWidth - halfW
                block.velocity.x = -block.velocity.x * 0.3f
            }
        }

        // 3. Integrate Active Monkeys
        for (monkey in monkeys) {
            if (monkey.isDefeated) continue

            monkey.damageCooldown = (monkey.damageCooldown - dt).coerceAtLeast(0f)

            if (monkey.isResting) {
                monkey.velocity = Vector2D(0f, 0f)
                monkey.angularVelocity = 0f
                continue
            }

            monkey.velocity += gravity * dt
            monkey.velocity *= 0.990f
            monkey.angularVelocity *= 0.95f
            monkey.position += monkey.velocity * dt
            monkey.angle += monkey.angularVelocity * dt

            // Ground collision & rolling physics
            if (monkey.position.y + monkey.radius >= groundY) {
                monkey.position.y = groundY - monkey.radius
                if (monkey.velocity.y > 0) {
                    val impactSpeed = monkey.velocity.y
                    monkey.velocity.y = -monkey.velocity.y * 0.20f
                    monkey.velocity.x *= 0.82f // rolling friction

                    monkey.angularVelocity = (monkey.velocity.x / monkey.radius) * 1.2f

                    // Fall damage on hard drop
                    if (impactSpeed > 220f && monkey.damageCooldown <= 0f) {
                        val fallDamage = (impactSpeed - 180f) * 0.60f
                        val dmgTaken = monkey.takeDamage(fallDamage, false)
                        monkey.damageCooldown = 0.18f
                        soundManager.playMonkeyReaction()
                        spawnJuiceParticles(monkey.position, monkey.type.primaryColor, 6)

                        if (monkey.isDefeated) {
                            handleMonkeyDefeat(monkey)
                        } else {
                            onScoreAdded((dmgTaken * 40).toInt(), monkey.position.copy(), "+FALL DAMAGE!", false)
                        }
                    }
                }

                // Settle to resting equilibrium
                if (monkey.velocity.lengthSquared() < 16f && abs(monkey.angularVelocity) < 0.10f) {
                    monkey.isResting = true
                    monkey.velocity = Vector2D(0f, 0f)
                    monkey.angularVelocity = 0f
                }
            }

            if (monkey.position.y > groundY + 60f) {
                monkey.isDefeated = true
                handleMonkeyDefeat(monkey)
            }
        }

        // 4. Resolve Fruit vs Block (Localized impact only)
        for (fruit in fruits) {
            if (!fruit.isLaunched || fruit.isDead) continue
            for (block in blocks) {
                if (block.isBroken) continue
                resolveFruitVsBlock(fruit, block)
            }
        }

        // 5. Resolve Fruit vs Monkey
        for (fruit in fruits) {
            if (!fruit.isLaunched || fruit.isDead) continue
            for (monkey in monkeys) {
                if (monkey.isDefeated) continue
                resolveFruitVsMonkey(fruit, monkey)
            }
        }

        // 6. Resolve Block vs Monkey
        for (block in blocks) {
            if (block.isBroken) continue
            for (monkey in monkeys) {
                if (monkey.isDefeated) continue
                resolveBlockVsMonkey(block, monkey, dt)
            }
        }

        // 7. Resolve Block vs Block (Impulse transfer & chain reactions)
        val numBlocks = blocks.size
        for (i in 0 until numBlocks) {
            val b1 = blocks[i]
            if (b1.isBroken) continue
            for (j in i + 1 until numBlocks) {
                val b2 = blocks[j]
                if (b2.isBroken) continue
                resolveBlockVsBlock(b1, b2)
            }
        }
    }

    private fun resolveFruitVsBlock(fruit: FruitEntity, block: BlockEntity) {
        val halfW = block.width / 2f
        val halfH = block.height / 2f

        val relPos = fruit.position - block.position
        val localPos = relPos.rotate(-block.angle)

        val clampedX = localPos.x.coerceIn(-halfW, halfW)
        val clampedY = localPos.y.coerceIn(-halfH, halfH)
        val closestLocal = Vector2D(clampedX, clampedY)

        val diff = localPos - closestLocal
        val distSq = diff.lengthSquared()

        if (distSq < fruit.radius * fruit.radius && distSq > 0.0001f) {
            val dist = sqrt(distSq)
            val penetration = fruit.radius - dist
            val localNormal = diff / dist
            val worldNormal = localNormal.rotate(block.angle)

            // ONLY wake up this specific impacted block
            block.isResting = false

            // Positional separation based on mass ratio
            val totalMass = fruit.mass + block.mass
            val fruitRatio = block.mass / totalMass
            val blockRatio = fruit.mass / totalMass

            fruit.position += worldNormal * (penetration * fruitRatio)
            block.position -= worldNormal * (penetration * blockRatio * 0.5f)

            // Velocity impulse calculation
            val relVel = fruit.velocity - block.velocity
            val velAlongNormal = relVel.dot(worldNormal)

            if (velAlongNormal < 0) {
                val restitution = min(fruit.restitution, block.material.restitution)
                val impulseMag = -(1f + restitution) * velAlongNormal / ((1f / fruit.mass) + (1f / block.mass))
                val impulse = worldNormal * impulseMag

                fruit.velocity += impulse / fruit.mass
                block.velocity -= impulse / block.mass

                // Calculate realistic rotational torque
                val arm = closestLocal.rotate(block.angle)
                val momentOfInertia = (block.mass * (block.width * block.width + block.height * block.height)) / 12f
                val torque = arm.cross(-impulse)
                block.angularVelocity += (torque / (momentOfInertia * 1.5f)).coerceIn(-18f, 18f)

                // Calculate damage to this specific block based on kinetic impact energy
                val relSpeed = relVel.length()
                val impactEnergy = (0.5f * fruit.mass * relSpeed * relSpeed * 0.0014f) * fruit.type.structuralDamageMult

                if (impactEnergy >= block.material.minImpactToDamage && block.damageCooldown <= 0f) {
                    val damage = (impactEnergy - block.material.minImpactToDamage * 0.4f) * 1.2f
                    val dmgDone = block.takeDamage(damage)
                    block.damageCooldown = 0.08f

                    soundManager.playImpact(block.material, relSpeed)
                    spawnDebris(closestLocal.rotate(block.angle) + block.position, block.material, 5)

                    if (block.isBroken) {
                        handleBlockBreak(block)
                    } else if (dmgDone > 5f) {
                        onScoreAdded((dmgDone * 20).toInt(), block.position.copy(), "+${(dmgDone * 20).toInt()}", false)
                    }
                } else {
                    soundManager.playImpact(block.material, relSpeed * 0.5f)
                }

                if (relSpeed >= 200f) {
                    onShakeImpact?.invoke(if (relSpeed > 450f) ShakeImpact.NORMAL else ShakeImpact.MINOR)
                }
            }
        }
    }

    private fun resolveFruitVsMonkey(fruit: FruitEntity, monkey: MonkeyEntity) {
        val relPos = monkey.position - fruit.position
        val distSq = relPos.lengthSquared()
        val totalRadius = fruit.radius + monkey.radius

        if (distSq < totalRadius * totalRadius && distSq > 0.0001f) {
            val dist = sqrt(distSq)
            val normal = relPos / dist
            val penetration = totalRadius - dist

            monkey.isResting = false

            fruit.position -= normal * (penetration * 0.35f)
            monkey.position += normal * (penetration * 0.65f)

            val relVel = fruit.velocity - monkey.velocity
            val velAlongNormal = relVel.dot(normal)

            if (velAlongNormal > 0) {
                val impulseMag = (1f + 0.3f) * velAlongNormal / ((1f / fruit.mass) + (1f / monkey.mass))
                val impulse = normal * impulseMag

                fruit.velocity -= impulse / fruit.mass
                monkey.velocity += impulse / monkey.mass

                val impactSpeed = relVel.length()
                val isDirectHeadHit = normal.y > 0.45f
                val isCrit = isDirectHeadHit || impactSpeed > 450f

                val baseDmg = (impactSpeed * 0.35f + 15f) * fruit.type.structuralDamageMult
                val damageTaken = monkey.takeDamage(baseDmg, isCrit)

                soundManager.playMonkeyReaction()
                spawnJuiceParticles(monkey.position, monkey.type.primaryColor, if (isCrit) 16 else 8)

                if (monkey.isDefeated) {
                    handleMonkeyDefeat(monkey)
                } else {
                    val popupText = if (isCrit) "CRITICAL HIT!" else "+${(damageTaken * 50).toInt()}"
                    onScoreAdded((damageTaken * 50).toInt(), monkey.position.copy(), popupText, isCrit)
                }

                onShakeImpact?.invoke(if (isCrit) ShakeImpact.NORMAL else ShakeImpact.MINOR)
            }
        }
    }

    private fun resolveBlockVsMonkey(block: BlockEntity, monkey: MonkeyEntity, dt: Float) {
        // If both are resting in static equilibrium, skip
        if (block.isResting && monkey.isResting) return

        val halfW = block.width / 2f
        val halfH = block.height / 2f

        val relPos = monkey.position - block.position
        val localPos = relPos.rotate(-block.angle)

        val clampedX = localPos.x.coerceIn(-halfW, halfW)
        val clampedY = localPos.y.coerceIn(-halfH, halfH)
        val closestLocal = Vector2D(clampedX, clampedY)

        val diff = localPos - closestLocal
        val distSq = diff.lengthSquared()

        if (distSq < monkey.radius * monkey.radius && distSq > 0.0001f) {
            val dist = sqrt(distSq)
            val penetration = monkey.radius - dist
            val worldNormal = (diff / dist).rotate(block.angle)

            // Positional separation
            monkey.position += worldNormal * penetration

            val relVel = monkey.velocity - block.velocity
            val velAlongNormal = relVel.dot(worldNormal)

            // Dynamic slope physics
            val isPerchedOnTop = localPos.y <= -halfH + 4f
            if (isPerchedOnTop && (abs(block.angle) > 0.04f || block.velocity.lengthSquared() > 40f)) {
                monkey.isResting = false
                val slopeTangent = Vector2D(cos(block.angle), sin(block.angle))
                val gravityAlongSlope = gravity.dot(slopeTangent)
                monkey.velocity += slopeTangent * (gravityAlongSlope * dt * 2.0f)
                monkey.angularVelocity = (monkey.velocity.dot(slopeTangent) / monkey.radius) * 1.5f
                monkey.state = MonkeyState.SCARED
            }

            if (velAlongNormal < 0) {
                // If collision has significant momentum, wake up both entities
                val relSpeed = relVel.length()
                if (relSpeed > 30f) {
                    block.isResting = false
                    monkey.isResting = false
                }

                val impulseMag = -(1f + 0.20f) * velAlongNormal / ((1f / 1.5f) + (1f / block.mass))
                monkey.velocity += worldNormal * (impulseMag / 1.5f)
                block.velocity -= worldNormal * (impulseMag / block.mass)

                // Crush damage from falling block or hard impact
                val isBlockFallingOnMonkey = block.velocity.y > 80f && block.position.y < monkey.position.y
                val isMonkeyFallingOnBlock = monkey.velocity.y > 140f

                if ((relSpeed > 140f || isBlockFallingOnMonkey || isMonkeyFallingOnBlock) && monkey.damageCooldown <= 0f) {
                    val crushDamage = (relSpeed - 90f) * (block.mass * 0.07f + 0.4f) + 20f
                    val dmgDone = monkey.takeDamage(crushDamage, false)
                    monkey.damageCooldown = 0.16f
                    soundManager.playImpact(block.material, relSpeed * 0.5f)
                    soundManager.playMonkeyReaction()
                    spawnJuiceParticles(monkey.position, monkey.type.primaryColor, 6)

                    if (monkey.isDefeated) {
                        handleMonkeyDefeat(monkey)
                    } else {
                        onScoreAdded((dmgDone * 40).toInt(), monkey.position.copy(), "+CRUSHED!", true)
                    }
                }
            }
        }
    }

    private fun resolveBlockVsBlock(b1: BlockEntity, b2: BlockEntity) {
        // If both blocks are in static equilibrium, skip
        if (b1.isResting && b2.isResting) return

        val delta = b2.position - b1.position
        val totalHalfW = (b1.width + b2.width) / 2f
        val totalHalfH = (b1.height + b2.height) / 2f

        val overlapX = totalHalfW - abs(delta.x)
        val overlapY = totalHalfH - abs(delta.y)

        if (overlapX > 0f && overlapY > 0f) {
            val normal: Vector2D
            val penetration: Float

            if (overlapX < overlapY) {
                penetration = overlapX
                normal = if (delta.x > 0) Vector2D(1f, 0f) else Vector2D(-1f, 0f)
            } else {
                penetration = overlapY
                normal = if (delta.y > 0) Vector2D(0f, 1f) else Vector2D(0f, -1f)
            }

            val relVel = b2.velocity - b1.velocity
            val velAlongNormal = relVel.dot(normal)
            val relSpeed = relVel.length()

            // Wake up a resting block ONLY if struck with dynamic momentum (> 25 px/s)
            if (b1.isResting && (relSpeed > 25f || abs(velAlongNormal) > 15f)) {
                b1.isResting = false
            }
            if (b2.isResting && (relSpeed > 25f || abs(velAlongNormal) > 15f)) {
                b2.isResting = false
            }

            // Positional separation based on mass
            val totalMass = b1.mass + b2.mass
            val m1Ratio = b2.mass / totalMass
            val m2Ratio = b1.mass / totalMass

            if (!b1.isResting && !b2.isResting) {
                b1.position -= normal * (penetration * m1Ratio * 0.6f)
                b2.position += normal * (penetration * m2Ratio * 0.6f)
            } else if (!b1.isResting) {
                b1.position -= normal * (penetration * 0.9f)
            } else if (!b2.isResting) {
                b2.position += normal * (penetration * 0.9f)
            }

            if (velAlongNormal < 0) {
                val restitution = min(b1.material.restitution, b2.material.restitution) * 0.25f
                val j = -(1f + restitution) * velAlongNormal / ((1f / b1.mass) + (1f / b2.mass))
                val impulse = normal * j

                if (!b1.isResting) b1.velocity -= impulse / b1.mass
                if (!b2.isResting) b2.velocity += impulse / b2.mass

                // Realistic rotational torque
                val arm1 = normal * (b1.height / 2f)
                val momentOfInertia1 = (b1.mass * (b1.width * b1.width + b1.height * b1.height)) / 12f
                if (!b1.isResting) {
                    b1.angularVelocity -= (arm1.cross(impulse) / (momentOfInertia1 * 2.0f)).coerceIn(-12f, 12f)
                }

                val arm2 = normal * -(b2.height / 2f)
                val momentOfInertia2 = (b2.mass * (b2.width * b2.width + b2.height * b2.height)) / 12f
                if (!b2.isResting) {
                    b2.angularVelocity += (arm2.cross(impulse) / (momentOfInertia2 * 2.0f)).coerceIn(-12f, 12f)
                }

                // Heavy impact crush damage between blocks
                if (relSpeed > 280f && (b1.damageCooldown <= 0f || b2.damageCooldown <= 0f)) {
                    val crushEnergy = (relSpeed - 220f) * 0.20f
                    if (crushEnergy > b1.material.minImpactToDamage && b1.damageCooldown <= 0f) {
                        b1.takeDamage(crushEnergy)
                        b1.damageCooldown = 0.12f
                        if (b1.isBroken) handleBlockBreak(b1)
                    }
                    if (crushEnergy > b2.material.minImpactToDamage && b2.damageCooldown <= 0f) {
                        b2.takeDamage(crushEnergy)
                        b2.damageCooldown = 0.12f
                        if (b2.isBroken) handleBlockBreak(b2)
                    }
                }
            }
        }
    }

    /**
     * Handles block destruction and evaluates genuine physical support for remaining blocks and monkeys.
     * Only blocks that were physically dependent on the destroyed block for vertical support begin falling.
     */
    private fun handleBlockBreak(block: BlockEntity) {
        soundManager.playBreak(block.material)
        spawnDebris(block.position, block.material, 16)
        onBlockBroken(block)
        onScoreAdded(block.material.scoreValue, block.position.copy(), "+${block.material.scoreValue}", false)
        onShakeImpact?.invoke(ShakeImpact.STRONG)

        // Evaluate support for blocks resting above the broken block
        for (b in blocks) {
            if (b.isBroken || b.id == block.id) continue

            // If block rests on the ground, it is permanently supported
            if (b.position.y + b.height / 2f >= groundY - 4f) continue

            // Check if b was resting directly on top of the destroyed block
            val isAbove = b.position.y < block.position.y
            val verticalContact = abs((b.position.y + b.height / 2f) - (block.position.y - block.height / 2f)) < 14f
            val horizontalOverlap = abs(b.position.x - block.position.x) < ((b.width + block.width) / 2f - 4f)

            if (isAbove && verticalContact && horizontalOverlap) {
                // Check if b has ANY other supporting block beneath it
                val hasOtherSupport = blocks.any { other ->
                    other.id != block.id && other.id != b.id && !other.isBroken &&
                            other.position.y > b.position.y &&
                            abs((b.position.y + b.height / 2f) - (other.position.y - other.height / 2f)) < 14f &&
                            abs(b.position.x - other.position.x) < ((b.width + other.width) / 2f - 4f)
                }

                // If b lost its primary support, it begins falling/settling naturally
                if (!hasOtherSupport) {
                    b.isResting = false
                }
            }
        }

        // Evaluate support for monkeys resting on the broken block
        for (m in monkeys) {
            if (m.isDefeated) continue
            val isAbove = m.position.y < block.position.y
            val verticalContact = abs((m.position.y + m.radius) - (block.position.y - block.height / 2f)) < 16f
            val horizontalOverlap = abs(m.position.x - block.position.x) < (block.width / 2f + m.radius)

            if (isAbove && verticalContact && horizontalOverlap) {
                m.isResting = false
                m.state = MonkeyState.SCARED
            }
        }
    }

    private fun handleMonkeyDefeat(monkey: MonkeyEntity) {
        soundManager.playMonkeyDefeat()
        spawnPoofParticles(monkey.position)
        onMonkeyDefeated(monkey)
        onShakeImpact?.invoke(ShakeImpact.NORMAL)
        onScoreAdded(monkey.type.scoreValue, monkey.position.copy(), "+${monkey.type.scoreValue}", true)
    }

    // --- FRUIT SPECIAL ABILITIES ---

    fun triggerBananaBoost(fruit: FruitEntity) {
        if (fruit.hasUsedAbility || !fruit.isLaunched || fruit.isDead) return
        fruit.hasUsedAbility = true
        soundManager.playAbilityBanana()
        // Banana boost produces speed streak particles and whoosh sound without shaking the camera
        onShakeImpact?.invoke(ShakeImpact.NONE)

        val dir = if (fruit.velocity.lengthSquared() > 100f) fruit.velocity.normalized() else Vector2D(1f, -0.25f).normalized()
        fruit.velocity = dir * (fruit.velocity.length().coerceAtLeast(450f) * 1.8f + 250f)

        for (i in 0 until 18) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 80f + random.nextFloat() * 200f
            particles.add(
                ParticleEntity(
                    position = fruit.position.copy(),
                    velocity = Vector2D(cos(angle) * speed, sin(angle) * speed) - (dir * 200f),
                    color = if (i % 2 == 0) Color(0xFFFFEB3B) else Color(0xFFFF9800),
                    size = 6f + random.nextFloat() * 6f,
                    maxLifeTime = 0.5f,
                    shape = ParticleShape.STAR
                )
            )
        }
    }

    fun triggerOrangeBurst(fruit: FruitEntity) {
        if (fruit.hasUsedAbility || !fruit.isLaunched || fruit.isDead) return
        fruit.hasUsedAbility = true
        soundManager.playAbilityOrange()
        onShakeImpact?.invoke(ShakeImpact.SPECIAL_ORANGE)

        val blastRadius = 180f
        val blastCenter = fruit.position

        spawnExplosionEffect(blastCenter, Color(0xFFFF9800), Color(0xFFFF5722))

        for (block in blocks) {
            if (block.isBroken) continue
            val dist = block.position.distanceTo(blastCenter)
            if (dist < blastRadius) {
                block.isResting = false
                val factor = (1f - (dist / blastRadius)).coerceIn(0f, 1f)
                val dir = (block.position - blastCenter).normalized()
                val force = dir * (factor * 950f)
                block.velocity += force / block.mass
                block.angularVelocity += (random.nextFloat() - 0.5f) * factor * 10f
                block.takeDamage(factor * 95f)
                if (block.isBroken) handleBlockBreak(block)
            }
        }

        for (monkey in monkeys) {
            if (monkey.isDefeated) continue
            val dist = monkey.position.distanceTo(blastCenter)
            if (dist < blastRadius) {
                monkey.isResting = false
                val factor = (1f - (dist / blastRadius)).coerceIn(0f, 1f)
                val dir = (monkey.position - blastCenter).normalized()
                monkey.velocity += dir * (factor * 800f)
                monkey.takeDamage(factor * 75f, false)
                if (monkey.isDefeated) handleMonkeyDefeat(monkey)
            }
        }
    }

    fun triggerCherrySplit(fruit: FruitEntity): List<FruitEntity> {
        if (fruit.hasUsedAbility || !fruit.isLaunched || fruit.isDead) return emptyList()
        fruit.hasUsedAbility = true
        soundManager.playAbilityCherry()
        // Cherry split has juice pop feedback without shaking the camera
        onShakeImpact?.invoke(ShakeImpact.NONE)

        val baseSpeed = fruit.velocity.length().coerceAtLeast(350f)
        val baseAngle = fruit.velocity.angle()

        fruit.velocity = Vector2D(cos(baseAngle) * baseSpeed * 1.12f, sin(baseAngle) * baseSpeed * 1.12f)

        val angleUp = baseAngle - 0.25f
        val angleDown = baseAngle + 0.25f

        val child1 = FruitEntity(
            id = System.currentTimeMillis() + 1,
            type = FruitType.CHERRY,
            position = fruit.position.copy() + Vector2D(0f, -12f),
            velocity = Vector2D(cos(angleUp) * baseSpeed * 1.15f, sin(angleUp) * baseSpeed * 1.15f),
            radius = fruit.radius * 0.9f,
            mass = fruit.mass * 0.9f,
            isLaunched = true,
            hasUsedAbility = true,
            isSplitChild = true
        )

        val child2 = FruitEntity(
            id = System.currentTimeMillis() + 2,
            type = FruitType.CHERRY,
            position = fruit.position.copy() + Vector2D(0f, 12f),
            velocity = Vector2D(cos(angleDown) * baseSpeed * 1.15f, sin(angleDown) * baseSpeed * 1.15f),
            radius = fruit.radius * 0.9f,
            mass = fruit.mass * 0.9f,
            isLaunched = true,
            hasUsedAbility = true,
            isSplitChild = true
        )

        spawnJuiceParticles(fruit.position, Color(0xFFE91E63), 12)
        return listOf(child1, child2)
    }

    fun triggerDurianSmash(fruit: FruitEntity) {
        if (fruit.hasUsedAbility || !fruit.isLaunched || fruit.isDead) return
        fruit.hasUsedAbility = true
        soundManager.playAbilityDurian()
        onShakeImpact?.invoke(ShakeImpact.SPECIAL_DURIAN)

        fruit.velocity = Vector2D(fruit.velocity.x * 0.2f, 1050f)

        val shockRadius = 220f
        val shockCenter = fruit.position

        spawnExplosionEffect(shockCenter, Color(0xFF8D6E63), Color(0xFFC0CA33))

        for (block in blocks) {
            if (block.isBroken) continue
            val dist = block.position.distanceTo(shockCenter)
            if (dist < shockRadius) {
                block.isResting = false
                val factor = (1f - (dist / shockRadius))
                block.velocity += Vector2D((random.nextFloat() - 0.5f) * 220f, -factor * 500f)
                block.angularVelocity += (random.nextFloat() - 0.5f) * 12f
                block.takeDamage(factor * 120f)
                if (block.isBroken) handleBlockBreak(block)
            }
        }

        for (monkey in monkeys) {
            if (monkey.isDefeated) continue
            val dist = monkey.position.distanceTo(shockCenter)
            if (dist < shockRadius) {
                monkey.isResting = false
                val factor = (1f - (dist / shockRadius))
                monkey.velocity += Vector2D((random.nextFloat() - 0.5f) * 200f, -factor * 450f)
                monkey.takeDamage(factor * 95f, false)
                if (monkey.isDefeated) handleMonkeyDefeat(monkey)
            }
        }
    }

    // --- TRAJECTORY PREDICTION (Exact Parabola Physics Match) ---

    fun calculateTrajectory(
        startPos: Vector2D,
        initialVelocity: Vector2D,
        fruitType: FruitType,
        steps: Int = 48,
        stepDt: Float = 0.05f
    ): List<Vector2D> {
        val points = mutableListOf<Vector2D>()
        var pos = startPos.copy()
        var vel = initialVelocity.copy()

        points.add(pos.copy())

        for (i in 0 until steps) {
            vel += gravity * stepDt
            vel *= (1.0f - 0.012f * stepDt).coerceIn(0.9f, 1.0f)
            pos += vel * stepDt

            // Stop trajectory if hitting ground
            if (pos.y + fruitType.radius >= groundY) {
                points.add(Vector2D(pos.x, groundY - fruitType.radius))
                break
            }

            // Stop trajectory if hitting the first block
            var hitBlock = false
            for (b in blocks) {
                if (b.isBroken) continue
                val halfW = b.width / 2f
                val halfH = b.height / 2f
                val relPos = pos - b.position
                val localPos = relPos.rotate(-b.angle)
                if (abs(localPos.x) <= halfW + fruitType.radius && abs(localPos.y) <= halfH + fruitType.radius) {
                    points.add(pos.copy())
                    hitBlock = true
                    break
                }
            }
            if (hitBlock) break

            points.add(pos.copy())
        }

        return points
    }

    // --- PARTICLE EMITTERS ---

    private fun spawnDebris(pos: Vector2D, material: MaterialType, count: Int) {
        for (i in 0 until count) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 50f + random.nextFloat() * 180f
            particles.add(
                ParticleEntity(
                    position = pos.copy(),
                    velocity = Vector2D(cos(angle) * speed, sin(angle) * speed - 60f),
                    color = if (i % 2 == 0) material.primaryColor else material.borderColor,
                    size = 4f + random.nextFloat() * 5f,
                    maxLifeTime = 0.6f + random.nextFloat() * 0.4f,
                    shape = ParticleShape.SHARD
                )
            )
        }
    }

    private fun spawnJuiceParticles(pos: Vector2D, color: Color, count: Int) {
        for (i in 0 until count) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 40f + random.nextFloat() * 140f
            particles.add(
                ParticleEntity(
                    position = pos.copy(),
                    velocity = Vector2D(cos(angle) * speed, sin(angle) * speed - 40f),
                    color = color,
                    size = 4f + random.nextFloat() * 4f,
                    maxLifeTime = 0.5f + random.nextFloat() * 0.3f,
                    shape = ParticleShape.CIRCLE
                )
            )
        }
    }

    private fun spawnPoofParticles(pos: Vector2D) {
        for (i in 0 until 14) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 30f + random.nextFloat() * 100f
            particles.add(
                ParticleEntity(
                    position = pos.copy(),
                    velocity = Vector2D(cos(angle) * speed, sin(angle) * speed - 30f),
                    color = Color.White.copy(alpha = 0.75f),
                    size = 7f + random.nextFloat() * 7f,
                    maxLifeTime = 0.45f + random.nextFloat() * 0.25f,
                    shape = ParticleShape.SMOKE
                )
            )
        }
    }

    private fun spawnExplosionEffect(pos: Vector2D, color1: Color, color2: Color) {
        for (i in 0 until 24) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 80f + random.nextFloat() * 260f
            particles.add(
                ParticleEntity(
                    position = pos.copy(),
                    velocity = Vector2D(cos(angle) * speed, sin(angle) * speed - 60f),
                    color = if (i % 2 == 0) color1 else color2,
                    size = 6f + random.nextFloat() * 8f,
                    maxLifeTime = 0.6f + random.nextFloat() * 0.3f,
                    shape = ParticleShape.STAR
                )
            )
        }
    }

    fun spawnConfettiVictory(center: Vector2D = Vector2D(worldWidth * 0.65f, 250f)) {
        val colors = listOf(
            Color(0xFFFFD54F),
            Color(0xFFFF4081),
            Color(0xFF00E676),
            Color(0xFF00E5FF),
            Color(0xFFFF9100),
            Color(0xFFE040FB)
        )
        for (i in 0 until 45) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 100f + random.nextFloat() * 320f
            particles.add(
                ParticleEntity(
                    position = center.copy() + Vector2D((random.nextFloat() - 0.5f) * 120f, (random.nextFloat() - 0.5f) * 60f),
                    velocity = Vector2D(cos(angle) * speed, sin(angle) * speed - 180f),
                    color = colors[random.nextInt(colors.size)],
                    size = 5f + random.nextFloat() * 6f,
                    maxLifeTime = 1.4f + random.nextFloat() * 0.6f,
                    shape = ParticleShape.STAR,
                    gravityFactor = 0.5f
                )
            )
        }
    }
}
