package com.example.rulytopia.model

import androidx.compose.ui.graphics.Color

/**
 * 4 Structure Materials in Rulytopia: Wood, Glass, Stone, Metal.
 */
enum class MaterialType(
    val displayName: String,
    val density: Float, // for mass calculation
    val maxHpPerArea: Float, // durability
    val restitution: Float,
    val friction: Float,
    val minImpactToDamage: Float,
    val scoreValue: Int,
    val primaryColor: Color,
    val borderColor: Color,
    val highlightColor: Color
) {
    WOOD(
        displayName = "Wood",
        density = 1.0f,
        maxHpPerArea = 0.035f,
        restitution = 0.25f,
        friction = 0.65f,
        minImpactToDamage = 12f,
        scoreValue = 800,
        primaryColor = Color(0xFFBCAAA4),
        borderColor = Color(0xFF6D4C41),
        highlightColor = Color(0xFFD7CCC8)
    ),
    GLASS(
        displayName = "Glass",
        density = 0.7f,
        maxHpPerArea = 0.015f,
        restitution = 0.15f,
        friction = 0.35f,
        minImpactToDamage = 6f,
        scoreValue = 600,
        primaryColor = Color(0x99B2EBF2),
        borderColor = Color(0xFF00ACC1),
        highlightColor = Color(0xDDFFFFFF)
    ),
    STONE(
        displayName = "Stone",
        density = 2.4f,
        maxHpPerArea = 0.080f,
        restitution = 0.12f,
        friction = 0.80f,
        minImpactToDamage = 26f,
        scoreValue = 1200,
        primaryColor = Color(0xFFB0BEC5),
        borderColor = Color(0xFF455A64),
        highlightColor = Color(0xFFECEFF1)
    ),
    METAL(
        displayName = "Metal",
        density = 3.8f,
        maxHpPerArea = 0.160f,
        restitution = 0.30f,
        friction = 0.50f,
        minImpactToDamage = 45f,
        scoreValue = 2000,
        primaryColor = Color(0xFF78909C),
        borderColor = Color(0xFF263238),
        highlightColor = Color(0xFFCFD8DC)
    )
}
