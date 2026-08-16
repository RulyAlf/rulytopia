package com.example.rulytopia.data

import android.content.Context
import android.content.SharedPreferences
import com.example.rulytopia.model.FruitType
import com.example.rulytopia.model.LevelRepository

class GamePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("rulytopia_save_data", Context.MODE_PRIVATE)

    var highestUnlockedLevel: Int
        get() = prefs.getInt(KEY_HIGHEST_UNLOCKED_LEVEL, 1)
        set(value) = prefs.edit().putInt(KEY_HIGHEST_UNLOCKED_LEVEL, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC_ENABLED, value).apply()

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()

    fun getLevelStars(levelId: Int): Int {
        return prefs.getInt("${KEY_LEVEL_STARS_PREFIX}$levelId", 0)
    }

    fun setLevelStars(levelId: Int, stars: Int) {
        val current = getLevelStars(levelId)
        if (stars > current) {
            prefs.edit().putInt("${KEY_LEVEL_STARS_PREFIX}$levelId", stars).apply()
        }
    }

    fun getLevelHighScore(levelId: Int): Int {
        return prefs.getInt("${KEY_LEVEL_SCORE_PREFIX}$levelId", 0)
    }

    fun setLevelHighScore(levelId: Int, score: Int) {
        val current = getLevelHighScore(levelId)
        if (score > current) {
            prefs.edit().putInt("${KEY_LEVEL_SCORE_PREFIX}$levelId", score).apply()
        }
    }

    fun unlockLevel(levelId: Int) {
        if (levelId > highestUnlockedLevel) {
            highestUnlockedLevel = levelId.coerceAtMost(LevelRepository.TOTAL_LEVELS)
        }
    }

    fun hasSeenFruitTutorial(fruitType: FruitType): Boolean {
        return prefs.getBoolean("${KEY_FRUIT_TUTORIAL_PREFIX}${fruitType.name}", false)
    }

    fun setSeenFruitTutorial(fruitType: FruitType) {
        prefs.edit().putBoolean("${KEY_FRUIT_TUTORIAL_PREFIX}${fruitType.name}", true).apply()
    }

    fun getTotalStars(): Int {
        var total = 0
        for (i in 1..LevelRepository.TOTAL_LEVELS) {
            total += getLevelStars(i)
        }
        return total
    }

    fun getWorldStars(worldId: Int): Int {
        val world = LevelRepository.getWorld(worldId)
        var total = 0
        for (lvl in world.levelRange) {
            total += getLevelStars(lvl)
        }
        return total
    }

    fun resetAllProgress() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_HIGHEST_UNLOCKED_LEVEL = "highest_unlocked_level"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_LEVEL_STARS_PREFIX = "level_stars_"
        private const val KEY_LEVEL_SCORE_PREFIX = "level_score_"
        private const val KEY_FRUIT_TUTORIAL_PREFIX = "fruit_tutorial_"
    }
}
