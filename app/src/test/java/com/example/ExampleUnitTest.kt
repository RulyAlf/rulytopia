package com.example

import com.example.rulytopia.audio.SoundManager
import com.example.rulytopia.model.BlockEntity
import com.example.rulytopia.model.BlockShape
import com.example.rulytopia.model.FruitEntity
import com.example.rulytopia.model.FruitType
import com.example.rulytopia.model.LevelGenerator
import com.example.rulytopia.model.LevelRepository
import com.example.rulytopia.model.MaterialType
import com.example.rulytopia.model.MonkeyEntity
import com.example.rulytopia.model.MonkeyType
import com.example.rulytopia.model.Vector2D
import com.example.rulytopia.physics.PhysicsEngine
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito

class ExampleUnitTest {

    @Test
    fun testAll100LevelsLoadSuccessfully() {
        assertEquals(100, LevelRepository.TOTAL_LEVELS)
        for (i in 1..100) {
            val level = LevelRepository.getLevel(i)
            assertNotNull("Level $i should not be null", level)
            assertEquals(i, level.id)
            assertTrue("Level $i must have fruits", level.fruitQueue.isNotEmpty())
            assertTrue("Level $i must have monkeys", level.monkeys.isNotEmpty())
            assertTrue("Level $i must have blocks", level.blocks.isNotEmpty())
            assertTrue("Level $i worldWidth should be >= 1650", level.worldWidth >= 1650f)
            assertEquals("Level $i slingshot should be placed at X=180", 180f, level.slingshotX, 0.1f)
            
            // Verify structure spacing: all blocks should be placed at X >= 950 (well separated from slingshot)
            for (block in level.blocks) {
                assertTrue("Block in level $i should be far from slingshot (x=${block.x})", block.x >= 950f)
            }
        }
    }

    @Test
    fun testWorldThemesAndProgression() {
        val worlds = LevelRepository.getWorlds()
        assertEquals(5, worlds.size)
        assertEquals(1..20, worlds[0].levelRange)
        assertEquals(21..40, worlds[1].levelRange)
        assertEquals(41..60, worlds[2].levelRange)
        assertEquals(61..80, worlds[3].levelRange)
        assertEquals(81..100, worlds[4].levelRange)
    }

    @Test
    fun testTrajectoryParabolaPrediction() {
        val dummySoundManager = Mockito.mock(SoundManager::class.java)
        val engine = PhysicsEngine(
            soundManager = dummySoundManager,
            onScoreAdded = { _, _, _, _ -> },
            onMonkeyDefeated = { _ -> },
            onBlockBroken = { _ -> }
        )
        val level1 = LevelRepository.getLevel(1)
        engine.loadLevel(level1)

        val trajectory = engine.calculateTrajectory(
            startPos = Vector2D(180f, 380f),
            initialVelocity = Vector2D(800f, -400f),
            fruitType = FruitType.APPLE
        )
        assertTrue("Trajectory should produce points", trajectory.size > 5)
        assertTrue("Trajectory starts at slingshot", trajectory.first().x == 180f)
        assertTrue("Trajectory moves forward toward structure", trajectory.last().x > 180f)
    }

    @Test
    fun testRestingBlocksStayStableUntilImpacted() {
        val dummySoundManager = Mockito.mock(SoundManager::class.java)
        val engine = PhysicsEngine(
            soundManager = dummySoundManager,
            onScoreAdded = { _, _, _, _ -> },
            onMonkeyDefeated = { _ -> },
            onBlockBroken = { _ -> }
        )
        val level = LevelRepository.getLevel(1)
        engine.loadLevel(level)

        // Run several physics frames without any projectile
        for (i in 0 until 60) {
            engine.step(0.016f)
        }

        // Verify all blocks remain intact and at rest
        for (block in engine.blocks) {
            assertTrue("Block should remain resting prior to impact", block.isResting)
            assertFalse("Block should not be broken without impact", block.isBroken)
        }
        for (monkey in engine.monkeys) {
            assertTrue("Monkey should remain resting prior to impact", monkey.isResting)
            assertFalse("Monkey should not be defeated without impact", monkey.isDefeated)
        }
    }
}

