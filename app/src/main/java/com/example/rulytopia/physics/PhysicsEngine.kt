package com.example.rulytopia.physics

import androidx.compose.ui.graphics.Color
import com.example.rulytopia.audio.SoundManager
import com.example.rulytopia.model.*
import kotlin.math.*
import kotlin.random.Random

/**
 * 2D Physics & Collision Engine for Rulytopia.
 * Features realistic impulse physics, rotational inertia, structural instability & cascading collapses,
 * dynamic monkey rolling & slope physics, fall/crush damage, and punchy fruit abilities.
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

    var onScreenShake: ((intensity: Float) -> Unit)? = null

    var gravity = Vector2D(0f, 780f) // px / s^2
    var groundY = 480f
    var worldWidth = 1100f
    var worldHeight = 600f

    var hasFirstShotOccurred: Boolean = false

    private val random = Random(System.currentTimeMillis())

    fun loadLevel(levelDef: LevelDef) {
        fruits.clear()
        blocks.clear()
        monkeys.clear()
        particles.clear()

        groundY = levelDef.groundY
        worldWidth = levelDef.worldWidth
        worldHeight = levelDef.worldHeight
        hasFirstShotOccurred = false

        var nextId = 1L

        for (b in levelDef.blocks) {
            val area = b.width * b.height
            val hp = area * b.material.maxHpPerArea
            val mass = area * b.material.density * 0.08f
            blocks.add(
                BlockEntity(
                    id = nextId++,
                    position = Vector2D(b.x, b.y),
                    width = b.width,
                    height = b.height,
                    angle = b.angle,
                    velocity = Vector2D.Zero,
                    angularVelocity = 0f,
                    material = b.material,
                    shape = b.shape,
                    currentHp = hp,
                    maxHp = hp,
                    mass = mass,
                    isResting = true
                )
            )
        }

        for (m in levelDef.monkeys) {
            monkeys.add(
                MonkeyEntity(
                    id = nextId++,
                    type = m.type,
                    position = Vector2D(m.x, m.y),
                    velocity = Vector2D.Zero,
                    angularVelocity = 0f,
                    currentHp = m.type.maxHp,
                    maxHp = m.type.maxHp,
                    radius = m.type.radius,
                    isResting = true
                )
            )
        }
    }

    fun step(dt: Float) {
        val clampedDt = dt.coerceIn(0.001f, 0.033f)

        val substeps = 6
        val subDt = clampedDt / substeps

        for (step in 0 until substeps) {
            updateSubstep(subDt)
        }

        updateVisuals(clampedDt)
    }

    private fun updateSubstep(dt: Float) {
        // 1. Integrate Fruits
        for (fruit in fruits) {
            if (!fruit.isLaunched || fruit.isDead) continue

            val airDrag = if (fruit.type == FruitType.CHERRY) 0.996f else 0.999f
            fruit.velocity += gravity * dt
            fruit.velocity *= airDrag
            fruit.position += fruit.velocity * dt
            fruit.angle += fruit.angularVelocity * dt
            fruit.flightTime += dt

            // Ground collision
            if (fruit.position.y + fruit.radius >= groundY) {
                fruit.position.y = groundY - fruit.radius
                if (fruit.velocity.y > 0) {
                    val impactForce = abs(fruit.velocity.y)
                    fruit.velocity.y = -fruit.velocity.y * fruit.restitution
                    fruit.velocity.x *= 0.88f
                    fruit.angularVelocity *= 0.85f

                    if (impactForce > 80f) {
                        spawnJuiceParticles(fruit.position, fruit.type.primaryColor, 4)
                        soundManager.playImpact(MaterialType.WOOD, impactForce * 0.4f)
                    }
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

            if (fruit.flightTime > 0.8f && fruit.velocity.lengthSquared() < 36f && fruit.position.y + fruit.radius >= groundY - 3f) {
                fruit.isResting = true
            }
        }

        // 2. Integrate Blocks (Only active moving/unsupported blocks undergo physics)
        for (block in blocks) {
            if (block.isBroken) continue

            block.damageCooldown = (block.damageCooldown - dt).coerceAtLeast(0f)

            // If block is in stable resting state, it remains completely static in equilibrium
            if (block.isResting) {
                block.velocity = Vector2D.Zero
                block.angularVelocity = 0f
                continue
            }

            // Dynamic active block integration
            block.velocity += gravity * dt

            // Instability tipping torque for dynamic tilted blocks
            if (abs(block.angle) > 0.05f) {
                val tiltTorque = sin(block.angle) * (gravity.y * 0.0004f)
                block.angularVelocity += tiltTorque * dt
            }

            block.velocity *= 0.992f // subtle air damping
            block.angularVelocity *= 0.980f
            block.position += block.velocity * dt
            block.angle += block.angularVelocity * dt

            // Ground collision with multi-corner ground contact torque
            val halfH = block.height / 2f
            val halfW = block.width / 2f

            val cosA = cos(block.angle)
            val sinA = sin(block.angle)
            val cornerOffsets = arrayOf(
                Vector2D(-halfW * cosA + halfH * sinA, -halfW * sinA - halfH * cosA),
                Vector2D(halfW * cosA + halfH * sinA, halfW * sinA - halfH * cosA),
                Vector2D(halfW * cosA - halfH * sinA, halfW * sinA + halfH * cosA),
                Vector2D(-halfW * cosA - halfH * sinA, -halfW * sinA + halfH * cosA)
            )

            var lowestCornerY = block.position.y
            var lowestCornerOffset = Vector2D.Zero
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
                    block.velocity.y = -block.velocity.y * (block.material.restitution * 0.2f)
                    block.velocity.x *= block.material.friction

                    // Contact normal torque around pivot corner
                    val groundNormal = Vector2D(0f, -1f)
                    val groundImpulse = groundNormal * (fallSpeed * block.mass * 0.25f)
                    val groundTorque = lowestCornerOffset.cross(groundImpulse) / (block.mass * 900f)
                    block.angularVelocity += groundTorque

                    // High impact fall damage on hard collapse
                    if (fallSpeed > 280f && block.damageCooldown <= 0f) {
                        val fallDamage = (fallSpeed - 220f) * block.material.density * 0.12f
                        block.takeDamage(fallDamage)
                        block.damageCooldown = 0.12f
                        soundManager.playImpact(block.material, fallDamage)
                        if (block.isBroken) {
                            handleBlockBreak(block)
                        }
                    }
                }

                // Settle to sleep on ground if velocity is negligible
                if (block.velocity.lengthSquared() < 9f && abs(block.angularVelocity) < 0.08f) {
                    block.isResting = true
                    block.velocity = Vector2D.Zero
                    block.angularVelocity = 0f
                }
            }

            // Left/right bounds
            if (block.position.x - halfW < 0f) {
                block.position.x = halfW
                block.velocity.x = -block.velocity.x * 0.3f
            }
            if (block.position.x + halfW > worldWidth) {
                block.position.x = worldWidth - halfW
                block.velocity.x = -block.velocity.x * 0.3f
            }
        }

        // 3. Integrate Monkeys (Rolling Physics & Fall Damage)
        for (monkey in monkeys) {
            if (monkey.isDefeated) continue

            monkey.damageCooldown = (monkey.damageCooldown - dt).coerceAtLeast(0f)

            if (monkey.isResting) {
                monkey.velocity = Vector2D.Zero
                monkey.angularVelocity = 0f
                continue
            }

            monkey.velocity += gravity * dt
            monkey.velocity *= 0.990f
            monkey.angularVelocity *= 0.95f
            monkey.position += monkey.velocity * dt
            monkey.angle += monkey.angularVelocity * dt

            // Ground collision & Ground Fall Damage
            if (monkey.position.y + monkey.radius >= groundY) {
                monkey.position.y = groundY - monkey.radius
                if (monkey.velocity.y > 0) {
                    val impactSpeed = monkey.velocity.y
                    monkey.velocity.y = -monkey.velocity.y * 0.22f
                    monkey.velocity.x *= 0.80f // rolling on ground friction

                    // Synchronize visual rolling rotation with horizontal velocity
                    monkey.angularVelocity = (monkey.velocity.x / monkey.radius) * 1.2f

                    // Fatal / Heavy Fall Damage Calculation
                    if (impactSpeed > 160f && monkey.damageCooldown <= 0f) {
                        val fallDamage = (impactSpeed - 130f) * 0.55f
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

                // Settle to sleep on ground if stationary
                if (monkey.velocity.lengthSquared() < 9f && abs(monkey.angularVelocity) < 0.08f) {
                    monkey.isResting = true
                    monkey.velocity = Vector2D.Zero
                    monkey.angularVelocity = 0f
                }
            }

            // Check if monkey fell off the world
            if (monkey.position.y > groundY + 60f) {
                monkey.isDefeated = true
                handleMonkeyDefeat(monkey)
            }
        }

        // 4. Resolve Collisions: Fruit vs Block
        for (fruit in fruits) {
            if (!fruit.isLaunched || fruit.isDead) continue
            for (block in blocks) {
                if (block.isBroken) continue
                resolveFruitVsBlock(fruit, block)
            }
        }

        // 5. Resolve Collisions: Fruit vs Monkey
        for (fruit in fruits) {
            if (!fruit.isLaunched || fruit.isDead) continue
            for (monkey in monkeys) {
                if (monkey.isDefeated) continue
                resolveFruitVsMonkey(fruit, monkey)
            }
        }

        // 6. Resolve Collisions: Block vs Monkey (Dynamic Slope Rolling & Crush Damage)
        for (block in blocks) {
            if (block.isBroken) continue
            for (monkey in monkeys) {
                if (monkey.isDefeated) continue
                resolveBlockVsMonkey(block, monkey, dt)
            }
        }

        // 7. Resolve Collisions: Block vs Block (Realistic Stacking, Toppling & Chain Collapse)
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

            // Wake up hit block immediately
            block.isResting = false

            // Positional separation
            fruit.position += worldNormal * (penetration * 0.75f)
            block.position -= worldNormal * (penetration * 0.25f)

            val relVel = fruit.velocity - block.velocity
            val velAlongNormal = relVel.dot(worldNormal)

            if (velAlongNormal < 0) {
                val restitution = min(fruit.restitution, block.material.restitution)
                // Boost impulse magnitude for powerful, satisfying fruit impact
                val impulseMag = -(1f + restitution * 1.1f) * velAlongNormal / ((1f / fruit.mass) + (1f / block.mass))

                val impulse = worldNormal * impulseMag
                fruit.velocity += impulse / fruit.mass
                block.velocity -= impulse / block.mass

                // Strong Rotational Torque onto block to flip it over!
                val arm = closestLocal.rotate(block.angle)
                block.angularVelocity -= (arm.cross(impulse) / (block.mass * 600f))

                // Wake up connected and surrounding blocks for physical momentum transfer
                wakeUpSurroundingBlocks(block.position, 180f)

                // Damage calculation
                val impactStrength = abs(velAlongNormal) * fruit.mass * fruit.type.structuralDamageMult * 1.35f
                soundManager.playImpact(block.material, impactStrength)

                if (impactStrength >= block.material.minImpactToDamage && block.damageCooldown <= 0f) {
                    val dmg = (impactStrength - block.material.minImpactToDamage * 0.4f) * 1.6f
                    block.takeDamage(dmg)
                    block.damageCooldown = 0.06f
                    spawnDebris(closestLocal.rotate(block.angle) + block.position, block.material, 6)

                    if (block.isBroken) {
                        handleBlockBreak(block)
                    }
                }

                // Abilities triggering on impact
                if (fruit.type == FruitType.DURIAN && !fruit.hasUsedAbility && impactStrength > 50f) {
                    triggerDurianSmash(fruit)
                }
                if (fruit.type == FruitType.ORANGE && !fruit.hasUsedAbility && impactStrength > 40f) {
                    triggerOrangeBurst(fruit)
                }
            }
        }
    }

    private fun resolveFruitVsMonkey(fruit: FruitEntity, monkey: MonkeyEntity) {
        val delta = fruit.position - monkey.position
        val distSq = delta.lengthSquared()
        val minDist = fruit.radius + monkey.radius

        if (distSq < minDist * minDist && distSq > 0.0001f) {
            val dist = sqrt(distSq)
            val normal = delta / dist
            val penetration = minDist - dist

            // Wake up monkey immediately on physical impact
            monkey.isResting = false

            fruit.position += normal * (penetration * 0.5f)
            monkey.position -= normal * (penetration * 0.5f)

            val relVel = fruit.velocity - monkey.velocity
            val velAlongNormal = relVel.dot(normal)

            if (velAlongNormal < 0) {
                val impulseMag = -(1f + 0.4f) * velAlongNormal / ((1f / fruit.mass) + (1f / 1.5f))
                val impulse = normal * impulseMag

                fruit.velocity += impulse / fruit.mass
                monkey.velocity -= impulse / 1.5f

                // High spinning velocity from fruit strike
                monkey.angularVelocity = (normal.cross(impulse) / 100f).coerceIn(-18f, 18f)

                val isDirectFrontal = (fruit.velocity.x > 0 && monkey.position.x > fruit.position.x)
                val rawDamage = (abs(velAlongNormal) * fruit.mass * 2.2f) + 25f
                val dmgDone = monkey.takeDamage(rawDamage, isDirectFrontal)

                soundManager.playMonkeyReaction()
                spawnJuiceParticles(monkey.position, monkey.type.primaryColor, 8)
                onScreenShake?.invoke(6f)

                if (monkey.isDefeated) {
                    handleMonkeyDefeat(monkey)
                } else {
                    onScoreAdded((dmgDone * 50).toInt(), monkey.position.copy(), "CRUSH!", true)
                }
            }
        }
    }

    private fun resolveBlockVsMonkey(block: BlockEntity, monkey: MonkeyEntity, dt: Float) {
        // If both are in stable rest, no need to process dynamic collision
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
            // Wake both up when they interact dynamically
            block.isResting = false
            monkey.isResting = false

            val dist = sqrt(distSq)
            val penetration = monkey.radius - dist
            val worldNormal = (diff / dist).rotate(block.angle)

            monkey.position += worldNormal * penetration

            val relVel = monkey.velocity - block.velocity
            val velAlongNormal = relVel.dot(worldNormal)

            // --- SLOPE PHYSICS & GRAVITY ROLLING ---
            // If monkey is perched on top of a tilted block, accelerate down the slope!
            val isPerchedOnTop = localPos.y <= -halfH + 4f
            if (isPerchedOnTop) {
                val slopeAngle = block.angle
                val slopeTangent = Vector2D(cos(slopeAngle), sin(slopeAngle))
                val gravityAlongSlope = gravity.dot(slopeTangent)

                // Accelerate monkey down the slope if block is tilted or moving
                if (abs(slopeAngle) > 0.03f || block.velocity.lengthSquared() > 40f) {
                    monkey.velocity += slopeTangent * (gravityAlongSlope * dt * 2.2f)
                    // Visual rolling rotation matches velocity
                    monkey.angularVelocity = (monkey.velocity.dot(slopeTangent) / monkey.radius) * 1.6f
                    monkey.state = MonkeyState.SCARED
                }
            }

            if (velAlongNormal < 0) {
                val impulseMag = -(1f + 0.25f) * velAlongNormal / ((1f / 1.5f) + (1f / block.mass))
                monkey.velocity += worldNormal * (impulseMag / 1.5f)
                block.velocity -= worldNormal * (impulseMag / block.mass)

                // CRUSH DAMAGE / FALL-ONTO-BLOCK DAMAGE
                val relSpeed = relVel.length()
                val isBlockFallingOnMonkey = block.velocity.y > 80f && block.position.y < monkey.position.y
                val isMonkeyFallingOnBlock = monkey.velocity.y > 130f

                if ((relSpeed > 130f || isBlockFallingOnMonkey || isMonkeyFallingOnBlock) && monkey.damageCooldown <= 0f) {
                    val crushDamage = (relSpeed - 80f) * (block.mass * 0.08f + 0.4f) + 20f
                    val dmgDone = monkey.takeDamage(crushDamage, false)
                    monkey.damageCooldown = 0.15f
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
        // If both blocks are in resting equilibrium, skip collision calculation
        if (b1.isResting && b2.isResting) return

        val delta = b2.position - b1.position
        val totalHalfW = (b1.width + b2.width) / 2f
        val totalHalfH = (b1.height + b2.height) / 2f

        val overlapX = totalHalfW - abs(delta.x)
        val overlapY = totalHalfH - abs(delta.y)

        if (overlapX > 0 && overlapY > 0) {
            // Wake up both blocks upon physical contact
            b1.isResting = false
            b2.isResting = false

            val normal: Vector2D
            val penetration: Float

            if (overlapX < overlapY) {
                penetration = overlapX
                normal = if (delta.x > 0) Vector2D(1f, 0f) else Vector2D(-1f, 0f)
            } else {
                penetration = overlapY
                normal = if (delta.y > 0) Vector2D(0f, 1f) else Vector2D(0f, -1f)
            }

            val m1Ratio = b2.mass / (b1.mass + b2.mass)
            val m2Ratio = b1.mass / (b1.mass + b2.mass)

            b1.position -= normal * (penetration * m1Ratio * 0.8f)
            b2.position += normal * (penetration * m2Ratio * 0.8f)

            val relVel = b2.velocity - b1.velocity
            val velAlongNormal = relVel.dot(normal)

            if (velAlongNormal < 0) {
                val e = min(b1.material.restitution, b2.material.restitution) * 0.35f
                val j = -(1f + e) * velAlongNormal / ((1f / b1.mass) + (1f / b2.mass))
                val impulse = normal * j

                b1.velocity -= impulse / b1.mass
                b2.velocity += impulse / b2.mass

                // Toppling torque from eccentric contact
                val contactArm1 = delta * 0.5f
                val contactArm2 = -delta * 0.5f
                b1.angularVelocity -= (contactArm1.cross(impulse) / (b1.mass * 800f))
                b2.angularVelocity += (contactArm2.cross(impulse) / (b2.mass * 800f))

                // Friction along contact plane
                val tangent = Vector2D(-normal.y, normal.x)
                val velAlongTangent = relVel.dot(tangent)
                val friction = (b1.material.friction + b2.material.friction) / 2f
                val frictionImpulse = tangent * (-velAlongTangent * friction * 0.55f)

                b1.velocity -= frictionImpulse / b1.mass
                b2.velocity += frictionImpulse / b2.mass

                // Structural crush damage during high-velocity impacts
                val relSpeed = relVel.length()
                if (relSpeed > 180f) {
                    val crushImpulse = (relSpeed - 140f) * min(b1.mass, b2.mass) * 0.08f
                    if (crushImpulse > 30f) {
                        if (b1.damageCooldown <= 0f) {
                            b1.takeDamage(crushImpulse * 0.2f)
                            b1.damageCooldown = 0.12f
                            if (b1.isBroken) handleBlockBreak(b1)
                        }
                        if (b2.damageCooldown <= 0f) {
                            b2.takeDamage(crushImpulse * 0.2f)
                            b2.damageCooldown = 0.12f
                            if (b2.isBroken) handleBlockBreak(b2)
                        }
                    }
                }
            }
        }
    }

    private fun wakeUpSurroundingBlocks(center: Vector2D, radius: Float) {
        for (b in blocks) {
            if (b.isBroken) continue
            if (b.position.distanceTo(center) < radius) {
                b.isResting = false
            }
        }
        for (m in monkeys) {
            if (m.isDefeated) continue
            if (m.position.distanceTo(center) < radius) {
                m.isResting = false
                m.state = MonkeyState.SCARED
            }
        }
    }

    private fun handleBlockBreak(block: BlockEntity) {
        soundManager.playBreak(block.material)
        spawnDebris(block.position, block.material, 12)
        onBlockBroken(block)
        onScreenShake?.invoke(5f)
        onScoreAdded(block.material.scoreValue, block.position.copy(), "+${block.material.scoreValue}", false)

        // Cascading collapse: wake up all blocks resting above or directly supported by this block
        for (b in blocks) {
            if (b.isBroken) continue
            val dx = abs(b.position.x - block.position.x)
            val isAboveOrTouching = b.position.y <= block.position.y + 15f && b.position.y >= block.position.y - 180f && dx < (b.width + block.width) * 0.85f
            if (isAboveOrTouching) {
                b.isResting = false
            }
        }
        for (m in monkeys) {
            if (m.isDefeated) continue
            val dx = abs(m.position.x - block.position.x)
            val isAboveOrTouching = m.position.y <= block.position.y + 10f && m.position.y >= block.position.y - 140f && dx < (m.radius + block.width / 2f) + 20f
            if (isAboveOrTouching) {
                m.isResting = false
                m.state = MonkeyState.SCARED
            }
        }
    }

    private fun handleMonkeyDefeat(monkey: MonkeyEntity) {
        soundManager.playMonkeyDefeat()
        spawnPoofParticles(monkey.position)
        onMonkeyDefeated(monkey)
        onScreenShake?.invoke(10f)
        onScoreAdded(monkey.type.scoreValue, monkey.position.copy(), "+${monkey.type.scoreValue}", true)
    }

    // --- FRUIT SPECIAL ABILITIES ---

    fun triggerBananaBoost(fruit: FruitEntity) {
        if (fruit.hasUsedAbility || !fruit.isLaunched || fruit.isDead) return
        fruit.hasUsedAbility = true
        soundManager.playAbilityBanana()
        onScreenShake?.invoke(6f)

        val dir = if (fruit.velocity.lengthSquared() > 100f) fruit.velocity.normalized() else Vector2D(1f, -0.3f).normalized()
        fruit.velocity = dir * (fruit.velocity.length().coerceAtLeast(450f) * 2.1f + 350f)

        for (i in 0 until 18) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 90f + random.nextFloat() * 220f
            particles.add(
                ParticleEntity(
                    position = fruit.position.copy(),
                    velocity = Vector2D(cos(angle) * speed, sin(angle) * speed) - (dir * 220f),
                    color = if (i % 2 == 0) Color(0xFFFFEB3B) else Color(0xFFFF9800),
                    size = 7f + random.nextFloat() * 7f,
                    maxLifeTime = 0.55f,
                    shape = ParticleShape.STAR
                )
            )
        }
    }

    fun triggerOrangeBurst(fruit: FruitEntity) {
        if (fruit.hasUsedAbility || !fruit.isLaunched || fruit.isDead) return
        fruit.hasUsedAbility = true
        soundManager.playAbilityOrange()
        onScreenShake?.invoke(18f)

        val blastRadius = 220f
        val blastCenter = fruit.position

        spawnExplosionEffect(blastCenter, Color(0xFFFF9800), Color(0xFFFF5722))

        for (block in blocks) {
            if (block.isBroken) continue
            val dist = block.position.distanceTo(blastCenter)
            if (dist < blastRadius) {
                val factor = (1f - (dist / blastRadius)).coerceIn(0f, 1f)
                val dir = (block.position - blastCenter).normalized()
                val force = dir * (factor * 1200f)
                block.velocity += force / block.mass
                block.angularVelocity += (random.nextFloat() - 0.5f) * factor * 14f
                block.takeDamage(factor * 110f)
                if (block.isBroken) handleBlockBreak(block)
            }
        }

        for (monkey in monkeys) {
            if (monkey.isDefeated) continue
            val dist = monkey.position.distanceTo(blastCenter)
            if (dist < blastRadius) {
                val factor = (1f - (dist / blastRadius)).coerceIn(0f, 1f)
                val dir = (monkey.position - blastCenter).normalized()
                monkey.velocity += dir * (factor * 950f)
                monkey.takeDamage(factor * 85f, false)
                if (monkey.isDefeated) handleMonkeyDefeat(monkey)
            }
        }
    }

    fun triggerCherrySplit(fruit: FruitEntity): List<FruitEntity> {
        if (fruit.hasUsedAbility || !fruit.isLaunched || fruit.isDead) return emptyList()
        fruit.hasUsedAbility = true
        soundManager.playAbilityCherry()
        onScreenShake?.invoke(4f)

        val baseSpeed = fruit.velocity.length().coerceAtLeast(350f)
        val baseAngle = fruit.velocity.angle()

        fruit.velocity = Vector2D(cos(baseAngle) * baseSpeed * 1.15f, sin(baseAngle) * baseSpeed * 1.15f)

        val angleUp = baseAngle - 0.28f
        val angleDown = baseAngle + 0.28f

        val child1 = FruitEntity(
            id = System.currentTimeMillis() + 1,
            type = FruitType.CHERRY,
            position = fruit.position.copy() + Vector2D(0f, -14f),
            velocity = Vector2D(cos(angleUp) * baseSpeed * 1.2f, sin(angleUp) * baseSpeed * 1.2f),
            radius = fruit.radius * 0.9f,
            mass = fruit.mass * 0.9f,
            isLaunched = true,
            hasUsedAbility = true,
            isSplitChild = true
        )

        val child2 = FruitEntity(
            id = System.currentTimeMillis() + 2,
            type = FruitType.CHERRY,
            position = fruit.position.copy() + Vector2D(0f, 14f),
            velocity = Vector2D(cos(angleDown) * baseSpeed * 1.2f, sin(angleDown) * baseSpeed * 1.2f),
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
        onScreenShake?.invoke(20f)

        fruit.velocity = Vector2D(fruit.velocity.x * 0.3f, 1100f)

        val shockRadius = 280f
        val shockCenter = fruit.position

        spawnExplosionEffect(shockCenter, Color(0xFF8D6E63), Color(0xFFC0CA33))

        for (block in blocks) {
            if (block.isBroken) continue
            val dist = block.position.distanceTo(shockCenter)
            if (dist < shockRadius) {
                val factor = (1f - (dist / shockRadius))
                block.velocity += Vector2D((random.nextFloat() - 0.5f) * 280f, -factor * 600f)
                block.angularVelocity += (random.nextFloat() - 0.5f) * 16f
                block.takeDamage(factor * 140f)
                if (block.isBroken) handleBlockBreak(block)
            }
        }

        for (monkey in monkeys) {
            if (monkey.isDefeated) continue
            val dist = monkey.position.distanceTo(shockCenter)
            if (dist < shockRadius) {
                val factor = (1f - (dist / shockRadius))
                monkey.velocity += Vector2D((random.nextFloat() - 0.5f) * 350f, -factor * 650f)
                monkey.takeDamage(factor * 120f, false)
                if (monkey.isDefeated) handleMonkeyDefeat(monkey)
            }
        }
    }

    // --- TRAJECTORY CALCULATION ---

    fun calculateTrajectory(
        startPos: Vector2D,
        initialVelocity: Vector2D,
        fruitType: FruitType,
        numPoints: Int = 28
    ): List<Vector2D> {
        val points = mutableListOf<Vector2D>()
        var pos = startPos.copy()
        var vel = initialVelocity.copy()
        val dt = 0.040f

        points.add(pos.copy())
        for (i in 1 until numPoints) {
            vel += gravity * dt
            pos += vel * dt
            if (pos.y >= groundY) {
                points.add(Vector2D(pos.x, groundY))
                break
            }
            points.add(pos.copy())
        }
        return points
    }

    // --- VISUALS & PARTICLES ---

    private fun updateVisuals(dt: Float) {
        for (fruit in fruits) {
            if (!fruit.isLaunched || fruit.isDead) continue

            // Dynamic trajectory trail
            if (fruit.velocity.lengthSquared() > 100f) {
                fruit.addTrailPoint(fruit.position)
            }

            // Check if fruit has stopped moving (resting on ground or blocks)
            val isSlow = fruit.velocity.lengthSquared() < 36f
            val isNearRest = fruit.flightTime > 0.6f && (isSlow || fruit.isResting)

            if (isNearRest) {
                fruit.isResting = true
                fruit.restingTimer += dt

                // Disappear after 3 seconds of resting
                if (fruit.restingTimer >= 3.0f) {
                    // Smooth fade out over 0.5s
                    fruit.alpha = (1f - (fruit.restingTimer - 3.0f) / 0.5f).coerceIn(0f, 1f)
                }

                if (fruit.restingTimer >= 3.5f) {
                    // Poof particle effect and mark as dead
                    spawnJuiceParticles(fruit.position, fruit.type.primaryColor, 8)
                    spawnPoofParticles(fruit.position)
                    fruit.isDead = true
                }
            } else {
                fruit.restingTimer = 0f
                fruit.alpha = 1.0f
            }
        }

        val activeFruit = fruits.firstOrNull { it.isLaunched && !it.isResting && !it.isDead }
        for (monkey in monkeys) {
            if (monkey.isDefeated) continue

            monkey.blinkTimer -= dt
            if (monkey.blinkTimer <= 0f) {
                monkey.isBlinking = !monkey.isBlinking
                monkey.blinkTimer = if (monkey.isBlinking) 0.15f else (2f + random.nextFloat() * 3f)
            }

            if (monkey.hitTimer > 0f) {
                monkey.hitTimer -= dt
                monkey.state = MonkeyState.HIT
            } else if (activeFruit != null && monkey.position.distanceTo(activeFruit.position) < 260f) {
                monkey.state = MonkeyState.SCARED
            } else if (abs(monkey.velocity.x) > 35f || monkey.velocity.y > 45f || abs(monkey.angularVelocity) > 2f) {
                monkey.state = MonkeyState.SCARED
            } else {
                monkey.state = MonkeyState.IDLE
            }
        }

        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.lifeTime -= dt
            if (p.lifeTime <= 0f) {
                iterator.remove()
            } else {
                p.velocity += gravity * (dt * p.gravityFactor * 0.4f)
                p.position += p.velocity * dt
                p.rotation += p.rotationSpeed * dt
            }
        }
    }

    fun spawnJuiceParticles(pos: Vector2D, color: Color, count: Int) {
        for (i in 0 until count) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 50f + random.nextFloat() * 220f
            particles.add(
                ParticleEntity(
                    position = pos.copy(),
                    velocity = Vector2D(cos(angle) * speed, sin(angle) * speed),
                    color = color,
                    size = 5f + random.nextFloat() * 6f,
                    maxLifeTime = 0.5f + random.nextFloat() * 0.4f,
                    shape = ParticleShape.CIRCLE
                )
            )
        }
    }

    fun spawnDebris(pos: Vector2D, material: MaterialType, count: Int) {
        for (i in 0 until count) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 60f + random.nextFloat() * 260f
            particles.add(
                ParticleEntity(
                    position = pos.copy(),
                    velocity = Vector2D(cos(angle) * speed, sin(angle) * speed),
                    color = if (i % 2 == 0) material.primaryColor else material.borderColor,
                    size = 6f + random.nextFloat() * 8f,
                    maxLifeTime = 0.6f + random.nextFloat() * 0.4f,
                    shape = if (material == MaterialType.GLASS) ParticleShape.SHARD else ParticleShape.CIRCLE,
                    rotationSpeed = (random.nextFloat() - 0.5f) * 10f
                )
            )
        }
    }

    fun spawnPoofParticles(pos: Vector2D) {
        for (i in 0 until 14) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 40f + random.nextFloat() * 140f
            particles.add(
                ParticleEntity(
                    position = pos.copy(),
                    velocity = Vector2D(cos(angle) * speed, sin(angle) * speed),
                    color = Color(0xEEFFFFFF),
                    size = 14f + random.nextFloat() * 16f,
                    maxLifeTime = 0.55f + random.nextFloat() * 0.3f,
                    shape = ParticleShape.SMOKE,
                    gravityFactor = -0.2f
                )
            )
        }
    }

    fun spawnExplosionEffect(pos: Vector2D, color1: Color, color2: Color) {
        particles.add(
            ParticleEntity(
                position = pos.copy(),
                velocity = Vector2D.Zero,
                color = color1,
                size = 140f,
                maxLifeTime = 0.4f,
                shape = ParticleShape.RING,
                gravityFactor = 0f
            )
        )
        for (i in 0 until 20) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 90f + random.nextFloat() * 280f
            particles.add(
                ParticleEntity(
                    position = pos.copy(),
                    velocity = Vector2D(cos(angle) * speed, sin(angle) * speed),
                    color = if (i % 2 == 0) color1 else color2,
                    size = 8f + random.nextFloat() * 10f,
                    maxLifeTime = 0.55f + random.nextFloat() * 0.35f,
                    shape = ParticleShape.STAR,
                    gravityFactor = 0.3f
                )
            )
        }
    }

    fun spawnConfettiVictory() {
        val colors = listOf(
            Color(0xFFFFEB3B), Color(0xFFFF5722), Color(0xFF4CAF50),
            Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFF9C27B0)
        )
        for (i in 0 until 50) {
            val startX = 200f + random.nextFloat() * 600f
            val startY = 100f + random.nextFloat() * 150f
            val angle = (random.nextFloat() - 0.5f) * PI.toFloat()
            val speed = 100f + random.nextFloat() * 300f
            particles.add(
                ParticleEntity(
                    position = Vector2D(startX, startY),
                    velocity = Vector2D(cos(angle) * speed, sin(angle) * speed - 150f),
                    color = colors[random.nextInt(colors.size)],
                    size = 7f + random.nextFloat() * 7f,
                    maxLifeTime = 1.8f + random.nextFloat() * 1.2f,
                    shape = ParticleShape.SHARD,
                    rotationSpeed = (random.nextFloat() - 0.5f) * 12f,
                    gravityFactor = 0.5f
                )
            )
        }
    }
}
