package com.example.rulytopia.model

import androidx.compose.ui.graphics.Color

/**
 * Multiple Monkey Archetypes in Rulytopia.
 */
enum class MonkeyType(
    val displayName: String,
    val maxHp: Float,
    val radius: Float,
    val armorDamageReduction: Float, // 0.0 = full damage, 0.5 = 50% damage reduction
    val scoreValue: Int,
    val primaryColor: Color,
    val description: String
) {
    BASIC(
        displayName = "Playful Monkey",
        maxHp = 30f,
        radius = 22f,
        armorDamageReduction = 0.0f,
        scoreValue = 5000,
        primaryColor = Color(0xFF8D6E63),
        description = "Standard mischievous monkey."
    ),
    ARMORED(
        displayName = "Armored Monkey",
        maxHp = 60f,
        radius = 23f,
        armorDamageReduction = 0.50f,
        scoreValue = 7500,
        primaryColor = Color(0xFF78909C),
        description = "Wears a protective hard hat. Takes reduced damage from standard hits."
    ),
    NIMBLE(
        displayName = "Nimble Monkey",
        maxHp = 20f,
        radius = 16f,
        armorDamageReduction = 0.0f,
        scoreValue = 6000,
        primaryColor = Color(0xFFA1887F),
        description = "Small, agile monkey perched in tight crevices."
    ),
    SHIELDED(
        displayName = "Shielded Monkey",
        maxHp = 45f,
        radius = 22f,
        armorDamageReduction = 0.85f, // Frontal shield protection
        scoreValue = 8500,
        primaryColor = Color(0xFF5D4037),
        description = "Carries a wooden shield facing front. Vulnerable from above or behind."
    ),
    HEAVY(
        displayName = "Heavy King Monkey",
        maxHp = 110f,
        radius = 32f,
        armorDamageReduction = 0.20f,
        scoreValue = 12000,
        primaryColor = Color(0xFF4E342E),
        description = "Massive chieftain monkey with heavy constitution. Needs heavy fruit strikes!"
    )
}
