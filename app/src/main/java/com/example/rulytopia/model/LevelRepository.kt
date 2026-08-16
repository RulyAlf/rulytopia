package com.example.rulytopia.model

object LevelRepository {

    const val TOTAL_LEVELS = 100
    const val TOTAL_WORLDS = 5
    const val LEVELS_PER_WORLD = 20

    val worlds: List<WorldDef> = LevelGenerator.worlds

    val levels: List<LevelDef> by lazy {
        LevelGenerator.generateAllLevels()
    }

    fun getLevel(id: Int): LevelDef {
        val clampedId = id.coerceIn(1, TOTAL_LEVELS)
        return levels.getOrNull(clampedId - 1) ?: levels.first()
    }

    fun getWorld(worldId: Int): WorldDef {
        val clampedWorld = worldId.coerceIn(1, TOTAL_WORLDS)
        return worlds.find { it.id == clampedWorld } ?: worlds.first()
    }

    fun getLevelsForWorld(worldId: Int): List<LevelDef> {
        val range = getWorld(worldId).levelRange
        return levels.filter { it.id in range }
    }
}
