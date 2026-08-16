package com.example.rulytopia.model

import androidx.compose.ui.graphics.Color

enum class BlockShape {
    RECTANGLE,
    TRIANGLE
}

enum class MonkeyState {
    IDLE,
    SCARED,
    HIT,
    DEFEATED
}

/**
 * An active flying/rolling fruit entity in the physics world.
 */
data class FruitEntity(
    val id: Long,
    val type: FruitType,
    var position: Vector2D,
    var velocity: Vector2D = Vector2D(0f, 0f),
    var angle: Float = 0f,
    var angularVelocity: Float = 0f,
    var radius: Float = type.radius,
    var mass: Float = type.mass,
    var restitution: Float = type.restitution,
    var isLaunched: Boolean = false,
    var isResting: Boolean = false,
    var isDead: Boolean = false,
    var hasUsedAbility: Boolean = false,
    var flightTime: Float = 0f,
    var restingTimer: Float = 0f,
    var alpha: Float = 1.0f,
    val trail: MutableList<Vector2D> = mutableListOf(),
    var isSplitChild: Boolean = false
) {
    fun addTrailPoint(pos: Vector2D) {
        trail.add(pos.copy())
        if (trail.size > 24) {
            trail.removeAt(0)
        }
    }
}

/**
 * A destructible block forming towers, bridges, fortresses.
 */
data class BlockEntity(
    val id: Long,
    var position: Vector2D,
    val width: Float,
    val height: Float,
    var angle: Float = 0f,
    var velocity: Vector2D = Vector2D(0f, 0f),
    var angularVelocity: Float = 0f,
    val material: MaterialType,
    val shape: BlockShape = BlockShape.RECTANGLE,
    var currentHp: Float,
    val maxHp: Float,
    val mass: Float,
    var isBroken: Boolean = false,
    var isResting: Boolean = false,
    var crackIntensity: Float = 0f, // 0.0 = clean, 1.0 = heavy cracks
    var damageCooldown: Float = 0f
) {
    fun takeDamage(amount: Float) {
        currentHp -= amount
        crackIntensity = (1f - (currentHp / maxHp)).coerceIn(0f, 1f)
        if (currentHp <= 0f) {
            isBroken = true
        }
    }
}

/**
 * A monkey enemy inside or around the structures.
 */
data class MonkeyEntity(
    val id: Long,
    val type: MonkeyType,
    var position: Vector2D,
    var velocity: Vector2D = Vector2D(0f, 0f),
    var angle: Float = 0f,
    var angularVelocity: Float = 0f,
    var currentHp: Float = type.maxHp,
    val maxHp: Float = type.maxHp,
    val radius: Float = type.radius,
    var isDefeated: Boolean = false,
    var isResting: Boolean = false,
    var state: MonkeyState = MonkeyState.IDLE,
    var scaredTimer: Float = 0f,
    var hitTimer: Float = 0f,
    var blinkTimer: Float = 0f,
    var isBlinking: Boolean = false,
    var damageCooldown: Float = 0f
) {
    fun takeDamage(rawDamage: Float, isDirectFrontalHit: Boolean = false): Float {
        if (isDefeated) return 0f
        var effectiveDamage = rawDamage
        if (type == MonkeyType.ARMORED) {
            effectiveDamage *= (1f - type.armorDamageReduction)
        } else if (type == MonkeyType.SHIELDED && isDirectFrontalHit) {
            effectiveDamage *= (1f - type.armorDamageReduction)
        }
        currentHp -= effectiveDamage
        hitTimer = 0.5f
        state = MonkeyState.HIT
        if (currentHp <= 0f) {
            isDefeated = true
            state = MonkeyState.DEFEATED
        }
        return effectiveDamage
    }
}

enum class ParticleShape {
    CIRCLE,
    SHARD,
    STAR,
    SMOKE,
    RING,
    FEATHER
}

/**
 * Visual particle effect for impact, explosions, destructions, confetti.
 */
data class ParticleEntity(
    var position: Vector2D,
    var velocity: Vector2D,
    val color: Color,
    val size: Float,
    val maxLifeTime: Float,
    var lifeTime: Float = maxLifeTime,
    val shape: ParticleShape = ParticleShape.CIRCLE,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f,
    val gravityFactor: Float = 1.0f
) {
    val progress: Float get() = (1f - (lifeTime / maxLifeTime)).coerceIn(0f, 1f)
    val alpha: Float get() = (lifeTime / maxLifeTime).coerceIn(0f, 1f)
}

/**
 * Animated floating score popup text (e.g. "+5000", "BOOM!", "SMASH!").
 */
data class ScorePopup(
    var position: Vector2D,
    val text: String,
    val color: Color,
    val maxLifeTime: Float = 1.2f,
    var lifeTime: Float = 1.2f,
    val isCritical: Boolean = false
) {
    val alpha: Float get() = (lifeTime / maxLifeTime).coerceIn(0f, 1f)
}
