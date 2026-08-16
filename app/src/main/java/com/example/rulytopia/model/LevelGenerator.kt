package com.example.rulytopia.model

/**
 * LevelGenerator creates 100 well-balanced, solvable, diverse levels across 5 themed worlds.
 * Ensures physically stable block coordinates, proper ammunition balance, and clear strategic solutions
 * with wide horizontal flight spacing between the slingshot (X=180) and enemy structures (X=1050..1450).
 */
object LevelGenerator {

    val worlds: List<WorldDef> = listOf(
        WorldDef(
            id = 1,
            name = "Forest Canopy",
            subtitle = "Fundamentals & Trajectory",
            description = "Master slingshot trajectory and structural weak points using Apple and Banana heroes.",
            levelRange = 1..20,
            themeColorHex = 0xFF2E7D32,
            accentColorHex = 0xFF81C784
        ),
        WorldDef(
            id = 2,
            name = "Deep Jungle",
            subtitle = "Glass & Explosive Citrus",
            description = "Shatter fragile glass barriers and trigger Orange's explosive shockwaves.",
            levelRange = 21..40,
            themeColorHex = 0xFF00695C,
            accentColorHex = 0xFF4DB6AC
        ),
        WorldDef(
            id = 3,
            name = "Ancient Ruins",
            subtitle = "Stone & Cluster Split",
            description = "Breach heavy stone masonry and deploy Cherry's triple split against nimble & armored guards.",
            levelRange = 41..60,
            themeColorHex = 0xFFE65100,
            accentColorHex = 0xFFFFB74D
        ),
        WorldDef(
            id = 4,
            name = "Monkey Temple",
            subtitle = "Metal & Fortified Shields",
            description = "Outsmart shielded defenders and heavy chieftains guarding multi-material temples.",
            levelRange = 61..80,
            themeColorHex = 0xFF4527A0,
            accentColorHex = 0xFF9575CD
        ),
        WorldDef(
            id = 5,
            name = "Grand Fortress",
            subtitle = "The Royal Stronghold",
            description = "The ultimate challenge! Combine all 5 fruit abilities to breach the Emperor's grand citadel.",
            levelRange = 81..100,
            themeColorHex = 0xFFB71C1C,
            accentColorHex = 0xFFEF5350
        )
    )

    fun generateAllLevels(): List<LevelDef> {
        val levelList = mutableListOf<LevelDef>()

        // 1..10: Handcrafted Classic Levels with expanded long-range spacing
        levelList.addAll(getClassicLevels())

        // 11..100: Procedural-Template Scaled Levels across the 5 worlds
        for (id in 11..100) {
            levelList.add(generateLevel(id))
        }

        return levelList
    }

    private fun generateLevel(id: Int): LevelDef {
        val worldId = ((id - 1) / 20) + 1
        val levelInWorld = ((id - 1) % 20) + 1 // 1..20
        val groundY = 480f

        val worldWidth = when (worldId) {
            1 -> 1650f
            2 -> 1750f
            3 -> 1850f
            4 -> 1900f
            else -> 2000f
        }

        val title = getLevelTitle(worldId, levelInWorld, id)
        val subtitle = getLevelSubtitle(worldId, levelInWorld, id)
        val description = getLevelDescription(worldId, levelInWorld, id)

        val fruitQueue = buildFruitQueue(worldId, levelInWorld, id)
        val (blocks, monkeys) = buildStructureAndMonkeys(worldId, levelInWorld, id, groundY)

        val star1 = 12000 + id * 600
        val star2 = 18000 + id * 900
        val star3 = 25000 + id * 1200

        return LevelDef(
            id = id,
            title = title,
            subtitle = subtitle,
            description = description,
            fruitQueue = fruitQueue,
            unlockedNewFruit = null,
            blocks = blocks,
            monkeys = monkeys,
            starThresholds = Triple(star1, star2, star3),
            slingshotX = 180f,
            slingshotY = 365f,
            groundY = groundY,
            worldWidth = worldWidth,
            worldHeight = 600f
        )
    }

    private fun getLevelTitle(worldId: Int, levelInWorld: Int, id: Int): String {
        return when (id) {
            100 -> "The Grand Emperor's Citadel"
            99 -> "Royal Vanguard Sanctum"
            98 -> "Obsidian Bastion"
            95 -> "King's Catacomb Vault"
            90 -> "Imperial High Keep"
            85 -> "Citadel Gatehouse"
            80 -> "Temple of the Monkey King"
            75 -> "Ironclad Vault"
            70 -> "Sanctum of the Elders"
            65 -> "Metal Spire Ridge"
            60 -> "Ancient Colossus"
            55 -> "Sunken Crypts"
            50 -> "Megalith Watchpost"
            45 -> "Ruins of Antiquity"
            40 -> "Emerald Pavilion"
            35 -> "Jungle Ziggurat"
            30 -> "Glasshouse Citadel"
            25 -> "Canopy Suspension"
            20 -> "Forest Overlook"
            15 -> "Timber Palisade"
            else -> when (worldId) {
                1 -> "Forest Stage $levelInWorld"
                2 -> "Jungle Outpost $levelInWorld"
                3 -> "Ruins Bastion $levelInWorld"
                4 -> "Temple Chamber $levelInWorld"
                5 -> "Fortress Tier $levelInWorld"
                else -> "Level $id"
            }
        }
    }

    private fun getLevelSubtitle(worldId: Int, levelInWorld: Int, id: Int): String {
        return when (worldId) {
            1 -> when (levelInWorld % 4) {
                0 -> "Domino Cascade"
                1 -> "Wooden Bastion"
                2 -> "Precision Angle"
                else -> "Twin Timber Spires"
            }
            2 -> when (levelInWorld % 4) {
                0 -> "Glass Resonance"
                1 -> "Citrus Detonation"
                2 -> "Multi-Level Perch"
                else -> "Canopy Bridges"
            }
            3 -> when (levelInWorld % 4) {
                0 -> "Triple Split Attack"
                1 -> "Heavy Stone Fort"
                2 -> "Armored Guard Defense"
                else -> "Ziggurat Collapse"
            }
            4 -> when (levelInWorld % 4) {
                0 -> "Iron Beam Fortress"
                1 -> "Shield Infiltration"
                2 -> "Heavy Chieftain Bunker"
                else -> "Multi-Material Stronghold"
            }
            5 -> when (levelInWorld % 4) {
                0 -> "Royal Citadel Core"
                1 -> "Multi-Tier Siege"
                2 -> "Impermeable Vault"
                else -> "Cataclysmic Collapse"
            }
            else -> "Tactical Strike"
        }
    }

    private fun getLevelDescription(worldId: Int, levelInWorld: Int, id: Int): String {
        return when (worldId) {
            1 -> "Aim for the base columns to trigger a cascading domino collapse!"
            2 -> "Shatter the fragile glass framework or explode Orange near structural joints."
            3 -> "Stone is sturdy — split Cherry at high altitude or target timber support beams."
            4 -> "Shielded enemies block frontal fire. Arc over the top or collapse the ceiling!"
            5 -> "The ultimate stronghold! Coordinate all fruit abilities to demolish the royal fortress."
            else -> "Deploy your fruit heroes strategically to clear all monkey targets."
        }
    }

    private fun buildFruitQueue(worldId: Int, levelInWorld: Int, id: Int): List<FruitType> {
        val pattern = (id % 5)
        return when (worldId) {
            1 -> when (pattern) {
                0 -> listOf(FruitType.APPLE, FruitType.BANANA, FruitType.APPLE, FruitType.BANANA)
                1 -> listOf(FruitType.BANANA, FruitType.APPLE, FruitType.APPLE)
                2 -> listOf(FruitType.APPLE, FruitType.APPLE, FruitType.BANANA, FruitType.APPLE)
                3 -> listOf(FruitType.BANANA, FruitType.BANANA, FruitType.APPLE)
                else -> listOf(FruitType.APPLE, FruitType.BANANA, FruitType.APPLE)
            }
            2 -> when (pattern) {
                0 -> listOf(FruitType.ORANGE, FruitType.BANANA, FruitType.APPLE, FruitType.ORANGE)
                1 -> listOf(FruitType.ORANGE, FruitType.APPLE, FruitType.BANANA)
                2 -> listOf(FruitType.BANANA, FruitType.ORANGE, FruitType.APPLE, FruitType.APPLE)
                3 -> listOf(FruitType.ORANGE, FruitType.BANANA, FruitType.ORANGE)
                else -> listOf(FruitType.APPLE, FruitType.ORANGE, FruitType.BANANA, FruitType.APPLE)
            }
            3 -> when (pattern) {
                0 -> listOf(FruitType.CHERRY, FruitType.ORANGE, FruitType.BANANA, FruitType.APPLE)
                1 -> listOf(FruitType.CHERRY, FruitType.APPLE, FruitType.CHERRY, FruitType.BANANA)
                2 -> listOf(FruitType.BANANA, FruitType.CHERRY, FruitType.ORANGE, FruitType.APPLE)
                3 -> listOf(FruitType.CHERRY, FruitType.CHERRY, FruitType.ORANGE)
                else -> listOf(FruitType.APPLE, FruitType.CHERRY, FruitType.BANANA, FruitType.CHERRY)
            }
            4 -> when (pattern) {
                0 -> listOf(FruitType.DURIAN, FruitType.CHERRY, FruitType.ORANGE, FruitType.BANANA)
                1 -> listOf(FruitType.DURIAN, FruitType.APPLE, FruitType.DURIAN, FruitType.BANANA)
                2 -> listOf(FruitType.ORANGE, FruitType.DURIAN, FruitType.CHERRY, FruitType.APPLE)
                3 -> listOf(FruitType.DURIAN, FruitType.BANANA, FruitType.ORANGE)
                else -> listOf(FruitType.APPLE, FruitType.DURIAN, FruitType.CHERRY, FruitType.ORANGE)
            }
            5 -> when (pattern) {
                0 -> listOf(FruitType.DURIAN, FruitType.ORANGE, FruitType.CHERRY, FruitType.BANANA, FruitType.APPLE)
                1 -> listOf(FruitType.CHERRY, FruitType.DURIAN, FruitType.ORANGE, FruitType.BANANA)
                2 -> listOf(FruitType.DURIAN, FruitType.BANANA, FruitType.ORANGE, FruitType.CHERRY)
                3 -> listOf(FruitType.ORANGE, FruitType.DURIAN, FruitType.APPLE, FruitType.BANANA)
                else -> listOf(FruitType.DURIAN, FruitType.CHERRY, FruitType.ORANGE, FruitType.BANANA)
            }
            else -> listOf(FruitType.APPLE, FruitType.BANANA, FruitType.ORANGE)
        }
    }

    private fun buildStructureAndMonkeys(
        worldId: Int,
        levelInWorld: Int,
        id: Int,
        groundY: Float
    ): Pair<List<BlockDef>, List<MonkeyDef>> {
        val blocks = mutableListOf<BlockDef>()
        val monkeys = mutableListOf<MonkeyDef>()

        val primaryMat = when (worldId) {
            1 -> MaterialType.WOOD
            2 -> if (levelInWorld % 2 == 0) MaterialType.GLASS else MaterialType.WOOD
            3 -> if (levelInWorld % 2 == 0) MaterialType.STONE else MaterialType.WOOD
            4 -> if (levelInWorld % 2 == 0) MaterialType.METAL else MaterialType.STONE
            5 -> if (levelInWorld % 3 == 0) MaterialType.METAL else if (levelInWorld % 3 == 1) MaterialType.STONE else MaterialType.WOOD
            else -> MaterialType.WOOD
        }

        val secondaryMat = when (worldId) {
            1 -> MaterialType.WOOD
            2 -> MaterialType.GLASS
            3 -> MaterialType.STONE
            4 -> MaterialType.METAL
            5 -> MaterialType.STONE
            else -> MaterialType.WOOD
        }

        val baseCenterX = when (worldId) {
            1 -> 1060f + ((id % 4) * 30f)
            2 -> 1120f + ((id % 4) * 35f)
            3 -> 1180f + ((id % 4) * 40f)
            4 -> 1240f + ((id % 4) * 40f)
            else -> 1300f + ((id % 4) * 45f)
        }

        val archetype = (id % 6)
        when (archetype) {
            0 -> buildTowerArchetype(blocks, monkeys, primaryMat, secondaryMat, worldId, groundY, baseCenterX)
            1 -> buildBunkerArchetype(blocks, monkeys, primaryMat, secondaryMat, worldId, groundY, baseCenterX)
            2 -> buildBridgeArchetype(blocks, monkeys, primaryMat, secondaryMat, worldId, groundY, baseCenterX)
            3 -> buildTwinTowersArchetype(blocks, monkeys, primaryMat, secondaryMat, worldId, groundY, baseCenterX)
            4 -> buildPyramidArchetype(blocks, monkeys, primaryMat, secondaryMat, worldId, groundY, baseCenterX)
            else -> buildFortressArchetype(blocks, monkeys, primaryMat, secondaryMat, worldId, groundY, baseCenterX)
        }

        return Pair(blocks, monkeys)
    }

    // --- ARCHETYPE BUILDERS (Mathematically Stably Stacked, Long-Range Spaced) ---

    private fun buildTowerArchetype(
        blocks: MutableList<BlockDef>,
        monkeys: MutableList<MonkeyDef>,
        mat1: MaterialType,
        mat2: MaterialType,
        worldId: Int,
        groundY: Float,
        centerX: Float
    ) {
        val colWidth = 22f
        val colHeight = 85f
        val colSpan = 80f
        val plankWidth = 120f
        val plankHeight = 18f

        // Tier 1 (Base)
        val t1LeftX = centerX - colSpan / 2f
        val t1RightX = centerX + colSpan / 2f
        val t1ColY = groundY - colHeight / 2f
        val t1PlankY = groundY - colHeight - plankHeight / 2f

        blocks.add(BlockDef(t1LeftX, t1ColY, colWidth, colHeight, mat1))
        blocks.add(BlockDef(t1RightX, t1ColY, colWidth, colHeight, mat1))
        blocks.add(BlockDef(centerX, t1PlankY, plankWidth, plankHeight, mat1))

        monkeys.add(MonkeyDef(centerX, groundY - 20f, getMonkeyForWorld(worldId, 0)))

        // Tier 2
        val t2BaseY = groundY - colHeight - plankHeight
        val t2ColHeight = 75f
        val t2ColY = t2BaseY - t2ColHeight / 2f
        val t2PlankY = t2BaseY - t2ColHeight - plankHeight / 2f

        blocks.add(BlockDef(centerX - 30f, t2ColY, 20f, t2ColHeight, mat2))
        blocks.add(BlockDef(centerX + 30f, t2ColY, 20f, t2ColHeight, mat2))
        blocks.add(BlockDef(centerX, t2PlankY, 90f, plankHeight, mat1))

        monkeys.add(MonkeyDef(centerX, t2BaseY - 20f, getMonkeyForWorld(worldId, 1)))

        // Tier 3 (Roof Perch)
        val t3BaseY = t2BaseY - t2ColHeight - plankHeight
        val t3ColHeight = 65f
        val t3ColY = t3BaseY - t3ColHeight / 2f
        val t3RoofY = t3BaseY - t3ColHeight - 16f / 2f

        blocks.add(BlockDef(centerX, t3ColY, 20f, t3ColHeight, mat2))
        blocks.add(BlockDef(centerX, t3RoofY, 55f, 16f, mat1))

        monkeys.add(MonkeyDef(centerX, t3RoofY - 18f, getMonkeyForWorld(worldId, 2)))
    }

    private fun buildBunkerArchetype(
        blocks: MutableList<BlockDef>,
        monkeys: MutableList<MonkeyDef>,
        mat1: MaterialType,
        mat2: MaterialType,
        worldId: Int,
        groundY: Float,
        centerX: Float
    ) {
        val wallW = 28f
        val wallH = 100f
        val span = 120f
        val roofW = 160f
        val roofH = 22f

        val leftWallX = centerX - span / 2f
        val rightWallX = centerX + span / 2f
        val wallY = groundY - wallH / 2f
        val roofY = groundY - wallH - roofH / 2f

        blocks.add(BlockDef(leftWallX, wallY, wallW, wallH, mat1))
        blocks.add(BlockDef(rightWallX, wallY, wallW, wallH, mat1))
        blocks.add(BlockDef(centerX, roofY, roofW, roofH, mat2))

        // Interior monkey guarded by walls
        monkeys.add(MonkeyDef(centerX, groundY - 22f, getMonkeyForWorld(worldId, 3)))

        // Roof tower
        val topH = 70f
        val topY = groundY - wallH - roofH - topH / 2f
        val topRoofY = groundY - wallH - roofH - topH - 9f

        blocks.add(BlockDef(centerX, topY, 24f, topH, mat1))
        blocks.add(BlockDef(centerX, topRoofY, 70f, 18f, mat2))

        monkeys.add(MonkeyDef(centerX, topRoofY - 20f, getMonkeyForWorld(worldId, 1)))

        // Satellite guard
        monkeys.add(MonkeyDef(leftWallX - 45f, groundY - 20f, getMonkeyForWorld(worldId, 0)))
    }

    private fun buildBridgeArchetype(
        blocks: MutableList<BlockDef>,
        monkeys: MutableList<MonkeyDef>,
        mat1: MaterialType,
        mat2: MaterialType,
        worldId: Int,
        groundY: Float,
        centerX: Float
    ) {
        val leftX = centerX - 120f
        val rightX = centerX + 120f
        val pillarW = 26f
        val pillarH = 110f
        val pillarY = groundY - pillarH / 2f
        val bridgeY = groundY - pillarH - 10f

        blocks.add(BlockDef(leftX, pillarY, pillarW, pillarH, mat1))
        blocks.add(BlockDef(rightX, pillarY, pillarW, pillarH, mat1))
        // Long span bridge
        blocks.add(BlockDef(centerX, bridgeY, 280f, 20f, mat2))

        // Center mid-tier column & monkey
        monkeys.add(MonkeyDef(centerX, bridgeY - 22f, getMonkeyForWorld(worldId, 0)))
        monkeys.add(MonkeyDef(leftX, bridgeY - 22f, getMonkeyForWorld(worldId, 1)))
        monkeys.add(MonkeyDef(rightX, bridgeY - 22f, getMonkeyForWorld(worldId, 2)))

        // Ground under-bridge monkey
        monkeys.add(MonkeyDef(centerX, groundY - 20f, getMonkeyForWorld(worldId, 0)))

        // Top bridge canopy
        val canopyY = bridgeY - 60f
        blocks.add(BlockDef(centerX, canopyY + 20f, 20f, 60f, mat1))
        blocks.add(BlockDef(centerX, canopyY - 15f, 90f, 16f, mat2))
    }

    private fun buildTwinTowersArchetype(
        blocks: MutableList<BlockDef>,
        monkeys: MutableList<MonkeyDef>,
        mat1: MaterialType,
        mat2: MaterialType,
        worldId: Int,
        groundY: Float,
        centerX: Float
    ) {
        val t1X = centerX - 95f
        val t2X = centerX + 95f
        val h = 90f
        val w = 22f
        val y = groundY - h / 2f
        val plankY = groundY - h - 9f

        // Tower 1
        blocks.add(BlockDef(t1X - 35f, y, w, h, mat1))
        blocks.add(BlockDef(t1X + 35f, y, w, h, mat1))
        blocks.add(BlockDef(t1X, plankY, 95f, 18f, mat1))
        monkeys.add(MonkeyDef(t1X, groundY - 20f, getMonkeyForWorld(worldId, 0)))
        monkeys.add(MonkeyDef(t1X, plankY - 20f, getMonkeyForWorld(worldId, 1)))

        // Connecting catwalk
        blocks.add(BlockDef(centerX, plankY, 90f, 14f, mat2))
        monkeys.add(MonkeyDef(centerX, plankY - 18f, getMonkeyForWorld(worldId, 2)))

        // Tower 2
        blocks.add(BlockDef(t2X - 35f, y, w, h, mat2))
        blocks.add(BlockDef(t2X + 35f, y, w, h, mat2))
        blocks.add(BlockDef(t2X, plankY, 95f, 18f, mat2))
        monkeys.add(MonkeyDef(t2X, groundY - 20f, getMonkeyForWorld(worldId, 0)))
        monkeys.add(MonkeyDef(t2X, plankY - 20f, getMonkeyForWorld(worldId, 3)))
    }

    private fun buildPyramidArchetype(
        blocks: MutableList<BlockDef>,
        monkeys: MutableList<MonkeyDef>,
        mat1: MaterialType,
        mat2: MaterialType,
        worldId: Int,
        groundY: Float,
        centerX: Float
    ) {
        val colH = 75f
        val colW = 22f
        val plankH = 18f

        // Level 1: 3 columns, 1 large slab
        val l1Y = groundY - colH / 2f
        blocks.add(BlockDef(centerX - 80f, l1Y, colW, colH, mat1))
        blocks.add(BlockDef(centerX, l1Y, colW, colH, mat1))
        blocks.add(BlockDef(centerX + 80f, l1Y, colW, colH, mat1))

        val slab1Y = groundY - colH - plankH / 2f
        blocks.add(BlockDef(centerX, slab1Y, 210f, plankH, mat1))

        monkeys.add(MonkeyDef(centerX - 40f, groundY - 20f, getMonkeyForWorld(worldId, 0)))
        monkeys.add(MonkeyDef(centerX + 40f, groundY - 20f, getMonkeyForWorld(worldId, 1)))

        // Level 2: 2 columns, medium slab
        val l2BaseY = groundY - colH - plankH
        val l2Y = l2BaseY - colH / 2f
        blocks.add(BlockDef(centerX - 40f, l2Y, colW, colH, mat2))
        blocks.add(BlockDef(centerX + 40f, l2Y, colW, colH, mat2))

        val slab2Y = l2BaseY - colH - plankH / 2f
        blocks.add(BlockDef(centerX, slab2Y, 130f, plankH, mat2))

        monkeys.add(MonkeyDef(centerX, l2BaseY - 20f, getMonkeyForWorld(worldId, 2)))

        // Level 3: Apex column & boss perch
        val l3BaseY = l2BaseY - colH - plankH
        val l3Y = l3BaseY - 50f / 2f
        val apexY = l3BaseY - 50f - 8f
        blocks.add(BlockDef(centerX, l3Y, 24f, 50f, mat1))
        blocks.add(BlockDef(centerX, apexY, 60f, 16f, mat1))

        monkeys.add(MonkeyDef(centerX, apexY - 18f, getMonkeyForWorld(worldId, 3)))
    }

    private fun buildFortressArchetype(
        blocks: MutableList<BlockDef>,
        monkeys: MutableList<MonkeyDef>,
        mat1: MaterialType,
        mat2: MaterialType,
        worldId: Int,
        groundY: Float,
        centerX: Float
    ) {
        val colH = 95f
        val colW = 28f
        val slabH = 22f

        // Outer fortress bastions
        blocks.add(BlockDef(centerX - 120f, groundY - colH / 2f, colW, colH, mat1))
        blocks.add(BlockDef(centerX - 40f, groundY - colH / 2f, 20f, colH, mat2))
        blocks.add(BlockDef(centerX + 40f, groundY - colH / 2f, 20f, colH, mat2))
        blocks.add(BlockDef(centerX + 120f, groundY - colH / 2f, colW, colH, mat1))

        val slab1Y = groundY - colH - slabH / 2f
        blocks.add(BlockDef(centerX, slab1Y, 280f, slabH, mat1))

        monkeys.add(MonkeyDef(centerX - 80f, groundY - 22f, getMonkeyForWorld(worldId, 0)))
        monkeys.add(MonkeyDef(centerX + 80f, groundY - 22f, getMonkeyForWorld(worldId, 1)))
        monkeys.add(MonkeyDef(centerX, groundY - 22f, getMonkeyForWorld(worldId, 3)))

        // Tier 2 Fortified Keep
        val t2BaseY = groundY - colH - slabH
        val t2ColH = 80f
        blocks.add(BlockDef(centerX - 60f, t2BaseY - t2ColH / 2f, 24f, t2ColH, mat2))
        blocks.add(BlockDef(centerX + 60f, t2BaseY - t2ColH / 2f, 24f, t2ColH, mat2))

        val t2SlabY = t2BaseY - t2ColH - 10f
        blocks.add(BlockDef(centerX, t2SlabY, 160f, 20f, mat1))

        monkeys.add(MonkeyDef(centerX, t2BaseY - 22f, getMonkeyForWorld(worldId, 2)))

        // Imperial Watchtower Throne
        val t3BaseY = t2SlabY - 10f
        val t3H = 65f
        blocks.add(BlockDef(centerX, t3BaseY - t3H / 2f, 24f, t3H, mat1))
        blocks.add(BlockDef(centerX, t3BaseY - t3H - 9f, 65f, 18f, mat2))

        monkeys.add(MonkeyDef(centerX, t3BaseY - t3H - 26f, getMonkeyForWorld(worldId, 4)))
    }

    private fun getMonkeyForWorld(worldId: Int, index: Int): MonkeyType {
        return when (worldId) {
            1 -> MonkeyType.BASIC
            2 -> if (index % 3 == 0) MonkeyType.NIMBLE else MonkeyType.BASIC
            3 -> when (index % 3) {
                0 -> MonkeyType.ARMORED
                1 -> MonkeyType.NIMBLE
                else -> MonkeyType.BASIC
            }
            4 -> when (index % 4) {
                0 -> MonkeyType.SHIELDED
                1 -> MonkeyType.HEAVY
                2 -> MonkeyType.ARMORED
                else -> MonkeyType.NIMBLE
            }
            5 -> when (index % 5) {
                0 -> MonkeyType.HEAVY
                1 -> MonkeyType.SHIELDED
                2 -> MonkeyType.ARMORED
                3 -> MonkeyType.NIMBLE
                else -> MonkeyType.BASIC
            }
            else -> MonkeyType.BASIC
        }
    }

    // --- CLASSIC HANDCRAFTED LEVELS 1..10 (Expanded with Proper Long-Range Spacing) ---

    private fun getClassicLevels(): List<LevelDef> {
        return listOf(
            // LEVEL 1: First Harvest (Apple tutorial, basic wooden tower, 2 monkeys)
            LevelDef(
                id = 1,
                title = "First Harvest",
                subtitle = "Welcome to Rulytopia",
                description = "Pull back the slingshot and release the Apple to topple the wooden tower and defeat the monkeys!",
                fruitQueue = listOf(FruitType.APPLE, FruitType.APPLE, FruitType.APPLE),
                unlockedNewFruit = FruitType.APPLE,
                slingshotX = 180f,
                slingshotY = 365f,
                groundY = 480f,
                worldWidth = 1650f,
                starThresholds = Triple(10000, 18000, 24000),
                blocks = listOf(
                    // Left column
                    BlockDef(x = 1040f, y = 435f, width = 24f, height = 90f, material = MaterialType.WOOD),
                    // Right column
                    BlockDef(x = 1140f, y = 435f, width = 24f, height = 90f, material = MaterialType.WOOD),
                    // Horizontal plank
                    BlockDef(x = 1090f, y = 380f, width = 140f, height = 20f, material = MaterialType.WOOD),
                    // Second tier left
                    BlockDef(x = 1060f, y = 332.5f, width = 20f, height = 75f, material = MaterialType.WOOD),
                    // Second tier right
                    BlockDef(x = 1120f, y = 332.5f, width = 20f, height = 75f, material = MaterialType.WOOD),
                    // Roof plank
                    BlockDef(x = 1090f, y = 286f, width = 100f, height = 18f, material = MaterialType.WOOD)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 1090f, y = 460f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 1090f, y = 257f, type = MonkeyType.BASIC)
                )
            ),

            // LEVEL 2: Target Practice (Twin towers, trajectory planning)
            LevelDef(
                id = 2,
                title = "Target Practice",
                subtitle = "Twin Towers",
                description = "Aim high to knock the top beams and create a domino collapse!",
                fruitQueue = listOf(FruitType.APPLE, FruitType.APPLE, FruitType.APPLE),
                slingshotX = 180f,
                slingshotY = 365f,
                groundY = 480f,
                worldWidth = 1750f,
                starThresholds = Triple(14000, 22000, 29000),
                blocks = listOf(
                    // Tower 1
                    BlockDef(x = 970f, y = 435f, width = 22f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 1050f, y = 435f, width = 22f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 1010f, y = 380f, width = 110f, height = 20f, material = MaterialType.WOOD),
                    BlockDef(x = 1010f, y = 330f, width = 22f, height = 80f, material = MaterialType.WOOD),
                    BlockDef(x = 1010f, y = 281f, width = 60f, height = 18f, material = MaterialType.WOOD),

                    // Connecting bridge
                    BlockDef(x = 1105f, y = 380f, width = 80f, height = 16f, material = MaterialType.WOOD),

                    // Tower 2
                    BlockDef(x = 1160f, y = 435f, width = 22f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 1240f, y = 435f, width = 22f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 1200f, y = 380f, width = 110f, height = 20f, material = MaterialType.WOOD),
                    BlockDef(x = 1200f, y = 330f, width = 22f, height = 80f, material = MaterialType.WOOD),
                    BlockDef(x = 1200f, y = 281f, width = 60f, height = 18f, material = MaterialType.WOOD)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 1010f, y = 460f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 1105f, y = 352f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 1200f, y = 460f, type = MonkeyType.BASIC)
                )
            ),

            // LEVEL 3: Banana Curve (Introduction to Banana Turbo Boost)
            LevelDef(
                id = 3,
                title = "Banana Curve",
                subtitle = "Speed & Agility",
                description = "Tap the screen while the Banana is in the air to trigger a speed boost!",
                fruitQueue = listOf(FruitType.BANANA, FruitType.APPLE, FruitType.BANANA),
                unlockedNewFruit = FruitType.BANANA,
                slingshotX = 180f,
                slingshotY = 365f,
                groundY = 480f,
                worldWidth = 1750f,
                starThresholds = Triple(12000, 20000, 26000),
                blocks = listOf(
                    // Tall fortified bunker
                    BlockDef(x = 1040f, y = 425f, width = 24f, height = 110f, material = MaterialType.WOOD),
                    BlockDef(x = 1140f, y = 425f, width = 24f, height = 110f, material = MaterialType.WOOD),
                    BlockDef(x = 1090f, y = 360f, width = 130f, height = 20f, material = MaterialType.WOOD),
                    // High watchtower
                    BlockDef(x = 1090f, y = 295f, width = 20f, height = 110f, material = MaterialType.WOOD),
                    BlockDef(x = 1090f, y = 230f, width = 80f, height = 18f, material = MaterialType.WOOD)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 1090f, y = 460f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 1090f, y = 202f, type = MonkeyType.NIMBLE)
                )
            ),

            // LEVEL 4: Glass House (Introduction to Glass & Orange Fruit)
            LevelDef(
                id = 4,
                title = "Glass House",
                subtitle = "Fragile Foundations",
                description = "Glass shatters easily! Use Orange's blast ability near the base to bring the whole structure down.",
                fruitQueue = listOf(FruitType.ORANGE, FruitType.BANANA, FruitType.APPLE),
                unlockedNewFruit = FruitType.ORANGE,
                slingshotX = 180f,
                slingshotY = 365f,
                groundY = 480f,
                worldWidth = 1800f,
                starThresholds = Triple(15000, 24000, 32000),
                blocks = listOf(
                    // Fragile Glass Ground Base
                    BlockDef(x = 1020f, y = 435f, width = 18f, height = 90f, material = MaterialType.GLASS),
                    BlockDef(x = 1100f, y = 435f, width = 18f, height = 90f, material = MaterialType.GLASS),
                    BlockDef(x = 1180f, y = 435f, width = 18f, height = 90f, material = MaterialType.GLASS),
                    BlockDef(x = 1100f, y = 380f, width = 200f, height = 20f, material = MaterialType.WOOD),

                    // Mid Level
                    BlockDef(x = 1050f, y = 330f, width = 18f, height = 80f, material = MaterialType.GLASS),
                    BlockDef(x = 1150f, y = 330f, width = 18f, height = 80f, material = MaterialType.GLASS),
                    BlockDef(x = 1100f, y = 280f, width = 140f, height = 20f, material = MaterialType.WOOD),

                    // Top
                    BlockDef(x = 1100f, y = 235f, width = 18f, height = 70f, material = MaterialType.WOOD),
                    BlockDef(x = 1100f, y = 191f, width = 70f, height = 18f, material = MaterialType.WOOD)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 1060f, y = 460f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 1140f, y = 460f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 1100f, y = 352f, type = MonkeyType.NIMBLE),
                    MonkeyDef(x = 1100f, y = 163f, type = MonkeyType.BASIC)
                )
            ),

            // LEVEL 5: Stone Fortress (Introduction to Stone & Cherry)
            LevelDef(
                id = 5,
                title = "Stone Fortress",
                subtitle = "Heavy Armor",
                description = "Stone is heavy and durable. Tap while Cherry is airborne to split into 3 high-impact projectiles!",
                fruitQueue = listOf(FruitType.CHERRY, FruitType.ORANGE, FruitType.BANANA),
                unlockedNewFruit = FruitType.CHERRY,
                slingshotX = 180f,
                slingshotY = 365f,
                groundY = 480f,
                worldWidth = 1800f,
                starThresholds = Triple(16000, 26000, 35000),
                blocks = listOf(
                    // Heavy Stone Lower Tier
                    BlockDef(x = 1020f, y = 430f, width = 28f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 1100f, y = 430f, width = 28f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 1180f, y = 430f, width = 28f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 1100f, y = 370f, width = 200f, height = 22f, material = MaterialType.STONE),

                    // Wood Upper Structure
                    BlockDef(x = 1055f, y = 315f, width = 20f, height = 85f, material = MaterialType.WOOD),
                    BlockDef(x = 1145f, y = 315f, width = 20f, height = 85f, material = MaterialType.WOOD),
                    BlockDef(x = 1100f, y = 262f, width = 120f, height = 20f, material = MaterialType.WOOD),

                    // High Stone Peak
                    BlockDef(x = 1100f, y = 217f, width = 22f, height = 70f, material = MaterialType.STONE),
                    BlockDef(x = 1100f, y = 173f, width = 60f, height = 18f, material = MaterialType.STONE)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 1060f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 1140f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 1100f, y = 342f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 1100f, y = 145f, type = MonkeyType.NIMBLE)
                )
            ),

            // LEVEL 6: Triple Threat (Cluster Target Practice)
            LevelDef(
                id = 6,
                title = "Triple Threat",
                subtitle = "Multiple Targets",
                description = "Deploy the Cherry split at high altitude to shower all 3 enemy posts simultaneously!",
                fruitQueue = listOf(FruitType.CHERRY, FruitType.CHERRY, FruitType.APPLE),
                slingshotX = 180f,
                slingshotY = 365f,
                groundY = 480f,
                worldWidth = 1850f,
                starThresholds = Triple(18000, 28000, 38000),
                blocks = listOf(
                    // Tower A
                    BlockDef(x = 940f, y = 435f, width = 20f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 990f, y = 435f, width = 20f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 965f, y = 380f, width = 75f, height = 18f, material = MaterialType.WOOD),

                    // Tower B (Middle, Stone + Glass)
                    BlockDef(x = 1070f, y = 435f, width = 22f, height = 90f, material = MaterialType.STONE),
                    BlockDef(x = 1130f, y = 435f, width = 18f, height = 90f, material = MaterialType.GLASS),
                    BlockDef(x = 1100f, y = 380f, width = 90f, height = 20f, material = MaterialType.WOOD),
                    BlockDef(x = 1100f, y = 325f, width = 20f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 1100f, y = 271f, width = 60f, height = 18f, material = MaterialType.STONE),

                    // Tower C
                    BlockDef(x = 1210f, y = 435f, width = 20f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 1260f, y = 435f, width = 20f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 1235f, y = 380f, width = 75f, height = 18f, material = MaterialType.WOOD)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 965f, y = 352f, type = MonkeyType.NIMBLE),
                    MonkeyDef(x = 1100f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 1100f, y = 243f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 1235f, y = 352f, type = MonkeyType.NIMBLE)
                )
            ),

            // LEVEL 7: Metal & Might (Introduction to Metal & Durian)
            LevelDef(
                id = 7,
                title = "Metal & Might",
                subtitle = "The Heavyweight",
                description = "Metal is nearly indestructible. Use Durian's Seismic Quake to smash straight through and obliterate the bunker!",
                fruitQueue = listOf(FruitType.DURIAN, FruitType.ORANGE, FruitType.APPLE),
                unlockedNewFruit = FruitType.DURIAN,
                slingshotX = 180f,
                slingshotY = 365f,
                groundY = 480f,
                worldWidth = 1850f,
                starThresholds = Triple(20000, 30000, 42000),
                blocks = listOf(
                    // Heavy Metal Pillbox
                    BlockDef(x = 1040f, y = 425f, width = 30f, height = 110f, material = MaterialType.METAL),
                    BlockDef(x = 1160f, y = 425f, width = 30f, height = 110f, material = MaterialType.METAL),
                    BlockDef(x = 1100f, y = 360f, width = 160f, height = 24f, material = MaterialType.METAL),

                    // Wood Upper Floor
                    BlockDef(x = 1070f, y = 305f, width = 20f, height = 85f, material = MaterialType.WOOD),
                    BlockDef(x = 1130f, y = 305f, width = 20f, height = 85f, material = MaterialType.WOOD),
                    BlockDef(x = 1100f, y = 252f, width = 100f, height = 20f, material = MaterialType.WOOD)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 1100f, y = 455f, type = MonkeyType.SHIELDED),
                    MonkeyDef(x = 1100f, y = 332f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 1100f, y = 224f, type = MonkeyType.HEAVY)
                )
            ),

            // LEVEL 8: Shield Wall (Dealing with Frontal Shields)
            LevelDef(
                id = 8,
                title = "Shield Wall",
                subtitle = "Defenders of Rulytopia",
                description = "Shielded Monkeys block all frontal damage. Arc high over their shields or collapse the roof on top of them!",
                fruitQueue = listOf(FruitType.BANANA, FruitType.ORANGE, FruitType.DURIAN),
                slingshotX = 180f,
                slingshotY = 365f,
                groundY = 480f,
                worldWidth = 1900f,
                starThresholds = Triple(22000, 34000, 46000),
                blocks = listOf(
                    // Shield Wall Fortification
                    BlockDef(x = 980f, y = 430f, width = 26f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 1080f, y = 430f, width = 26f, height = 100f, material = MaterialType.WOOD),
                    BlockDef(x = 1180f, y = 430f, width = 26f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 1080f, y = 370f, width = 230f, height = 22f, material = MaterialType.WOOD),

                    // Top Tier
                    BlockDef(x = 1040f, y = 315f, width = 22f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 1120f, y = 315f, width = 22f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 1080f, y = 260f, width = 120f, height = 20f, material = MaterialType.STONE)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 1030f, y = 455f, type = MonkeyType.SHIELDED),
                    MonkeyDef(x = 1130f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 1080f, y = 342f, type = MonkeyType.NIMBLE),
                    MonkeyDef(x = 1080f, y = 232f, type = MonkeyType.HEAVY)
                )
            ),

            // LEVEL 9: Monkey Chieftain (Boss Level - High HP Chieftain)
            LevelDef(
                id = 9,
                title = "Monkey Chieftain",
                subtitle = "The Heavy Chieftain",
                description = "The Chieftain has massive HP! Coordinate explosive and heavy fruit strikes to defeat his royal guard.",
                fruitQueue = listOf(FruitType.DURIAN, FruitType.CHERRY, FruitType.ORANGE, FruitType.BANANA),
                slingshotX = 180f,
                slingshotY = 365f,
                groundY = 480f,
                worldWidth = 1950f,
                starThresholds = Triple(25000, 38000, 52000),
                blocks = listOf(
                    // Complex Multi-Chamber Ziggurat
                    BlockDef(x = 1000f, y = 430f, width = 28f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 1100f, y = 430f, width = 20f, height = 100f, material = MaterialType.WOOD),
                    BlockDef(x = 1200f, y = 430f, width = 28f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 1100f, y = 370f, width = 240f, height = 22f, material = MaterialType.STONE),

                    // Mid Level Chambers
                    BlockDef(x = 1040f, y = 310f, width = 22f, height = 100f, material = MaterialType.WOOD),
                    BlockDef(x = 1160f, y = 310f, width = 22f, height = 100f, material = MaterialType.WOOD),
                    BlockDef(x = 1100f, y = 250f, width = 160f, height = 20f, material = MaterialType.WOOD),

                    // Top Throne Room
                    BlockDef(x = 1100f, y = 195f, width = 26f, height = 90f, material = MaterialType.METAL),
                    BlockDef(x = 1100f, y = 141f, width = 80f, height = 18f, material = MaterialType.STONE)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 1050f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 1150f, y = 455f, type = MonkeyType.SHIELDED),
                    MonkeyDef(x = 1100f, y = 342f, type = MonkeyType.NIMBLE),
                    MonkeyDef(x = 1100f, y = 113f, type = MonkeyType.HEAVY)
                )
            ),

            // LEVEL 10: Grand Castle Siege (World 1 Grand Finale)
            LevelDef(
                id = 10,
                title = "Grand Castle Siege",
                subtitle = "The Final Bastion",
                description = "Breach the Emperor's fortress using all five fruit abilities in sequence!",
                fruitQueue = listOf(FruitType.APPLE, FruitType.BANANA, FruitType.ORANGE, FruitType.CHERRY, FruitType.DURIAN),
                slingshotX = 180f,
                slingshotY = 365f,
                groundY = 480f,
                worldWidth = 2000f,
                starThresholds = Triple(30000, 48000, 65000),
                blocks = listOf(
                    // Outer Left Watchtower
                    BlockDef(x = 925f, y = 425f, width = 24f, height = 110f, material = MaterialType.STONE),
                    BlockDef(x = 925f, y = 360f, width = 60f, height = 20f, material = MaterialType.WOOD),

                    // Main Castle Lower Level (Metal + Stone)
                    BlockDef(x = 1020f, y = 425f, width = 32f, height = 110f, material = MaterialType.METAL),
                    BlockDef(x = 1120f, y = 425f, width = 32f, height = 110f, material = MaterialType.STONE),
                    BlockDef(x = 1220f, y = 425f, width = 32f, height = 110f, material = MaterialType.METAL),
                    BlockDef(x = 1120f, y = 357f, width = 240f, height = 26f, material = MaterialType.STONE),

                    // Mid Level Castle (Stone + Glass windows)
                    BlockDef(x = 1055f, y = 294f, width = 24f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 1120f, y = 294f, width = 18f, height = 100f, material = MaterialType.GLASS),
                    BlockDef(x = 1185f, y = 294f, width = 24f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 1120f, y = 233f, width = 170f, height = 22f, material = MaterialType.WOOD),

                    // High Throne Tower
                    BlockDef(x = 1090f, y = 177f, width = 20f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 1150f, y = 177f, width = 20f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 1120f, y = 122f, width = 90f, height = 20f, material = MaterialType.STONE),
                    BlockDef(x = 1120f, y = 87f, width = 24f, height = 50f, material = MaterialType.METAL)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 925f, y = 332f, type = MonkeyType.NIMBLE),
                    MonkeyDef(x = 975f, y = 458f, type = MonkeyType.SHIELDED),
                    MonkeyDef(x = 1070f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 1170f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 1120f, y = 324f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 1120f, y = 196f, type = MonkeyType.HEAVY)
                )
            )
        )
    }
}
