package com.example.rulytopia.model

import androidx.compose.ui.graphics.Color

/**
 * 5 Unique Fruits in Rulytopia, each with distinct physics properties and special abilities.
 * Tuned for punchy, high-velocity slingshot gameplay and satisfying structural destruction.
 */
enum class FruitType(
    val displayName: String,
    val personality: String,
    val mass: Float,
    val radius: Float, // in game physics units
    val restitution: Float, // bounciness (0.0 = dead weight, 1.0 = super bouncy)
    val structuralDamageMult: Float,
    val launchSpeedMult: Float,
    val primaryColor: Color,
    val accentColor: Color,
    val abilityName: String,
    val abilityDescription: String
) {
    APPLE(
        displayName = "Apple",
        personality = "Heavy & Dependable",
        mass = 2.4f,
        radius = 22f,
        restitution = 0.40f,
        structuralDamageMult = 1.4f,
        launchSpeedMult = 1.0f,
        primaryColor = Color(0xFFE53935),
        accentColor = Color(0xFF43A047),
        abilityName = "Impact Smash",
        abilityDescription = "Heavy kinetic fruit with high momentum to shatter dense wood and stone."
    ),
    BANANA(
        displayName = "Banana",
        personality = "Aerodynamic & Hyper-Fast",
        mass = 1.6f,
        radius = 19f,
        restitution = 0.70f,
        structuralDamageMult = 1.1f,
        launchSpeedMult = 1.12f,
        primaryColor = Color(0xFFFFD54F),
        accentColor = Color(0xFFF57F17),
        abilityName = "Turbo Boost",
        abilityDescription = "Tap while airborne to trigger an explosive supersonic speed burst!"
    ),
    ORANGE(
        displayName = "Orange",
        personality = "Energetic & Explosive",
        mass = 2.0f,
        radius = 21f,
        restitution = 0.45f,
        structuralDamageMult = 1.2f,
        launchSpeedMult = 1.02f,
        primaryColor = Color(0xFFFF9800),
        accentColor = Color(0xFFE65100),
        abilityName = "Citrus Burst",
        abilityDescription = "Tap in air or on impact to detonate a radial shockwave that blasts structures!"
    ),
    CHERRY(
        displayName = "Cherry",
        personality = "Small, Fast & Triple Threat",
        mass = 1.0f,
        radius = 15f,
        restitution = 0.55f,
        structuralDamageMult = 0.9f,
        launchSpeedMult = 1.20f,
        primaryColor = Color(0xFFD81B60),
        accentColor = Color(0xFF880E4F),
        abilityName = "Triple Split",
        abilityDescription = "Tap while flying to split into 3 high-velocity cluster projectiles!"
    ),
    DURIAN(
        displayName = "Durian",
        personality = "Heavy King of Fruits",
        mass = 4.2f,
        radius = 26f,
        restitution = 0.18f,
        structuralDamageMult = 2.4f,
        launchSpeedMult = 0.88f,
        primaryColor = Color(0xFF8D6E63),
        accentColor = Color(0xFFC0CA33),
        abilityName = "Seismic Quake",
        abilityDescription = "Tap to slam directly downward and trigger a devastating structural earthquake!"
    );

    companion object {
        fun fromName(name: String): FruitType {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: APPLE
        }
    }
}
