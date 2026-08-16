package com.example.rulytopia.model

/**
 * LevelGenerator creates 100 well-balanced, solvable, diverse levels across 5 themed worlds.
 * Ensures physically stable block coordinates, proper ammunition balance, and clear strategic solutions.
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

        // 1..10: Handcrafted Classic Levels (Preserved with high precision)
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
            groundY = groundY
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
                3 -> listOf(FruitType.CHERRY, FruitType.ORANGE, FruitType.CHERRY)
                else -> listOf(FruitType.APPLE, FruitType.CHERRY, FruitType.BANANA, FruitType.ORANGE, FruitType.APPLE)
            }
            4 -> when (pattern) {
                0 -> listOf(FruitType.DURIAN, FruitType.ORANGE, FruitType.CHERRY, FruitType.BANANA)
                1 -> listOf(FruitType.BANANA, FruitType.DURIAN, FruitType.ORANGE, FruitType.APPLE)
                2 -> listOf(FruitType.CHERRY, FruitType.DURIAN, FruitType.ORANGE, FruitType.BANANA, FruitType.APPLE)
                3 -> listOf(FruitType.DURIAN, FruitType.CHERRY, FruitType.BANANA, FruitType.ORANGE)
                else -> listOf(FruitType.ORANGE, FruitType.DURIAN, FruitType.APPLE, FruitType.CHERRY)
            }
            5 -> when (pattern) {
                0 -> listOf(FruitType.DURIAN, FruitType.ORANGE, FruitType.CHERRY, FruitType.BANANA, FruitType.APPLE)
                1 -> listOf(FruitType.APPLE, FruitType.BANANA, FruitType.ORANGE, FruitType.CHERRY, FruitType.DURIAN)
                2 -> listOf(FruitType.BANANA, FruitType.CHERRY, FruitType.DURIAN, FruitType.ORANGE, FruitType.APPLE)
                3 -> listOf(FruitType.CHERRY, FruitType.DURIAN, FruitType.ORANGE, FruitType.BANANA, FruitType.DURIAN)
                else -> listOf(FruitType.DURIAN, FruitType.BANANA, FruitType.ORANGE, FruitType.CHERRY, FruitType.APPLE)
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
            3 -> if (levelInWorld % 3 == 0) MaterialType.STONE else if (levelInWorld % 2 == 0) MaterialType.WOOD else MaterialType.GLASS
            4 -> if (levelInWorld % 3 == 0) MaterialType.METAL else if (levelInWorld % 2 == 0) MaterialType.STONE else MaterialType.WOOD
            5 -> when (levelInWorld % 4) {
                0 -> MaterialType.METAL
                1 -> MaterialType.STONE
                2 -> MaterialType.WOOD
                else -> MaterialType.GLASS
            }
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

        val archetype = (id % 6)
        when (archetype) {
            0 -> buildTowerArchetype(blocks, monkeys, primaryMat, secondaryMat, worldId, groundY, id)
            1 -> buildBunkerArchetype(blocks, monkeys, primaryMat, secondaryMat, worldId, groundY, id)
            2 -> buildBridgeArchetype(blocks, monkeys, primaryMat, secondaryMat, worldId, groundY, id)
            3 -> buildTwinTowersArchetype(blocks, monkeys, primaryMat, secondaryMat, worldId, groundY, id)
            4 -> buildPyramidArchetype(blocks, monkeys, primaryMat, secondaryMat, worldId, groundY, id)
            else -> buildFortressArchetype(blocks, monkeys, primaryMat, secondaryMat, worldId, groundY, id)
        }

        return Pair(blocks, monkeys)
    }

    // --- ARCHETYPE BUILDERS (Mathematically Stably Stacked) ---

    private fun buildTowerArchetype(
        blocks: MutableList<BlockDef>,
        monkeys: MutableList<MonkeyDef>,
        mat1: MaterialType,
        mat2: MaterialType,
        worldId: Int,
        groundY: Float,
        id: Int
    ) {
        val centerX = 680f + ((id % 3) * 30f)
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
        id: Int
    ) {
        val centerX = 660f + ((id % 4) * 25f)
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
        id: Int
    ) {
        val leftX = 540f
        val rightX = 780f
        val pillarW = 26f
        val pillarH = 110f
        val pillarY = groundY - pillarH / 2f
        val bridgeY = groundY - pillarH - 10f

        blocks.add(BlockDef(leftX, pillarY, pillarW, pillarH, mat1))
        blocks.add(BlockDef(rightX, pillarY, pillarW, pillarH, mat1))
        // Long span bridge
        blocks.add(BlockDef((leftX + rightX) / 2f, bridgeY, 280f, 20f, mat2))

        // Center mid-tier column & monkey
        val midX = (leftX + rightX) / 2f
        monkeys.add(MonkeyDef(midX, bridgeY - 22f, getMonkeyForWorld(worldId, 0)))
        monkeys.add(MonkeyDef(leftX, bridgeY - 22f, getMonkeyForWorld(worldId, 1)))
        monkeys.add(MonkeyDef(rightX, bridgeY - 22f, getMonkeyForWorld(worldId, 2)))

        // Ground under-bridge monkey
        monkeys.add(MonkeyDef(midX, groundY - 20f, getMonkeyForWorld(worldId, 0)))

        // Top bridge canopy
        val canopyY = bridgeY - 60f
        blocks.add(BlockDef(midX, canopyY + 20f, 20f, 60f, mat1))
        blocks.add(BlockDef(midX, canopyY - 15f, 90f, 16f, mat2))
    }

    private fun buildTwinTowersArchetype(
        blocks: MutableList<BlockDef>,
        monkeys: MutableList<MonkeyDef>,
        mat1: MaterialType,
        mat2: MaterialType,
        worldId: Int,
        groundY: Float,
        id: Int
    ) {
        val t1X = 580f
        val t2X = 760f
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
        blocks.add(BlockDef((t1X + t2X) / 2f, plankY, 90f, 14f, mat2))
        monkeys.add(MonkeyDef((t1X + t2X) / 2f, plankY - 18f, getMonkeyForWorld(worldId, 2)))

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
        id: Int
    ) {
        val centerX = 680f
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
        id: Int
    ) {
        val centerX = 680f
        val colH = 95f
        val colW = 28f
        val slabH = 22f

        // Outer fortress bastions
        blocks.add(BlockDef(centerX - 120f, groundY - colH / 2f, colW, colH, mat1))
        blocks.add(BlockDef(centerX - 40f, groundY - colH / 2f, 20f, colH, mat2))
        blocks.add(BlockDef(centerX + 40f, groundY - colH / 2f, 20f, colH, mat2))
        blocks.add(BlockDef(centerX + 120f, groundY - colH / 2f, colW, colH, mat1))

        val slabY = groundY - colH - slabH / 2f
        blocks.add(BlockDef(centerX, slabY, 290f, slabH, mat1))

        monkeys.add(MonkeyDef(centerX - 80f, groundY - 22f, getMonkeyForWorld(worldId, 0)))
        monkeys.add(MonkeyDef(centerX, groundY - 22f, getMonkeyForWorld(worldId, 3)))
        monkeys.add(MonkeyDef(centerX + 80f, groundY - 22f, getMonkeyForWorld(worldId, 1)))

        // Tier 2 fortified crown
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

    // --- CLASSIC HANDCRAFTED LEVELS 1..10 (Exact coordinates preserved) ---

    private fun getClassicLevels(): List<LevelDef> {
        return listOf(
            // LEVEL 1: First Harvest
            LevelDef(
                id = 1,
                title = "First Harvest",
                subtitle = "Welcome to Rulytopia",
                description = "Pull back the slingshot and release the Apple to topple the wooden tower and defeat the monkeys!",
                fruitQueue = listOf(FruitType.APPLE, FruitType.APPLE, FruitType.APPLE),
                unlockedNewFruit = FruitType.APPLE,
                groundY = 480f,
                starThresholds = Triple(10000, 18000, 24000),
                blocks = listOf(
                    BlockDef(x = 620f, y = 435f, width = 24f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 720f, y = 435f, width = 24f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 670f, y = 380f, width = 140f, height = 20f, material = MaterialType.WOOD),
                    BlockDef(x = 640f, y = 332.5f, width = 20f, height = 75f, material = MaterialType.WOOD),
                    BlockDef(x = 700f, y = 332.5f, width = 20f, height = 75f, material = MaterialType.WOOD),
                    BlockDef(x = 670f, y = 286f, width = 100f, height = 18f, material = MaterialType.WOOD)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 670f, y = 460f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 670f, y = 257f, type = MonkeyType.BASIC)
                )
            ),

            // LEVEL 2: Target Practice
            LevelDef(
                id = 2,
                title = "Target Practice",
                subtitle = "Twin Towers",
                description = "Aim high to knock the top beams and create a domino collapse!",
                fruitQueue = listOf(FruitType.APPLE, FruitType.APPLE, FruitType.APPLE),
                groundY = 480f,
                starThresholds = Triple(14000, 22000, 29000),
                blocks = listOf(
                    BlockDef(x = 550f, y = 435f, width = 22f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 630f, y = 435f, width = 22f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 590f, y = 380f, width = 110f, height = 20f, material = MaterialType.WOOD),
                    BlockDef(x = 590f, y = 330f, width = 22f, height = 80f, material = MaterialType.WOOD),
                    BlockDef(x = 590f, y = 281f, width = 60f, height = 18f, material = MaterialType.WOOD),
                    BlockDef(x = 685f, y = 380f, width = 80f, height = 16f, material = MaterialType.WOOD),
                    BlockDef(x = 740f, y = 435f, width = 22f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 820f, y = 435f, width = 22f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 780f, y = 380f, width = 110f, height = 20f, material = MaterialType.WOOD),
                    BlockDef(x = 780f, y = 330f, width = 22f, height = 80f, material = MaterialType.WOOD),
                    BlockDef(x = 780f, y = 281f, width = 60f, height = 18f, material = MaterialType.WOOD)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 590f, y = 460f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 685f, y = 352f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 780f, y = 460f, type = MonkeyType.BASIC)
                )
            ),

            // LEVEL 3: Banana Curve
            LevelDef(
                id = 3,
                title = "Banana Curve",
                subtitle = "Speed & Agility",
                description = "Tap the screen while the Banana is in the air to trigger a speed boost!",
                fruitQueue = listOf(FruitType.BANANA, FruitType.APPLE, FruitType.BANANA),
                unlockedNewFruit = FruitType.BANANA,
                groundY = 480f,
                starThresholds = Triple(15000, 24000, 31000),
                blocks = listOf(
                    BlockDef(x = 520f, y = 400f, width = 26f, height = 160f, material = MaterialType.WOOD),
                    BlockDef(x = 660f, y = 440f, width = 22f, height = 80f, material = MaterialType.WOOD),
                    BlockDef(x = 750f, y = 440f, width = 22f, height = 80f, material = MaterialType.WOOD),
                    BlockDef(x = 705f, y = 390f, width = 120f, height = 20f, material = MaterialType.WOOD),
                    BlockDef(x = 680f, y = 340f, width = 20f, height = 80f, material = MaterialType.WOOD),
                    BlockDef(x = 730f, y = 340f, width = 20f, height = 80f, material = MaterialType.WOOD),
                    BlockDef(x = 705f, y = 291f, width = 85f, height = 18f, material = MaterialType.WOOD)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 705f, y = 460f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 705f, y = 262f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 590f, y = 460f, type = MonkeyType.BASIC)
                )
            ),

            // LEVEL 4: Glasshouse Chaos
            LevelDef(
                id = 4,
                title = "Glasshouse Chaos",
                subtitle = "Explosive Citrus",
                description = "Glass shatters easily! Tap the screen to unleash Orange's Citrus Burst shockwave!",
                fruitQueue = listOf(FruitType.ORANGE, FruitType.BANANA, FruitType.APPLE),
                unlockedNewFruit = FruitType.ORANGE,
                groundY = 480f,
                starThresholds = Triple(16000, 25000, 34000),
                blocks = listOf(
                    BlockDef(x = 600f, y = 440f, width = 20f, height = 80f, material = MaterialType.GLASS),
                    BlockDef(x = 680f, y = 440f, width = 20f, height = 80f, material = MaterialType.GLASS),
                    BlockDef(x = 760f, y = 440f, width = 20f, height = 80f, material = MaterialType.GLASS),
                    BlockDef(x = 680f, y = 391f, width = 190f, height = 18f, material = MaterialType.GLASS),
                    BlockDef(x = 635f, y = 342f, width = 22f, height = 80f, material = MaterialType.WOOD),
                    BlockDef(x = 725f, y = 342f, width = 22f, height = 80f, material = MaterialType.WOOD),
                    BlockDef(x = 680f, y = 292f, width = 120f, height = 20f, material = MaterialType.WOOD),
                    BlockDef(x = 680f, y = 249.5f, width = 20f, height = 65f, material = MaterialType.GLASS),
                    BlockDef(x = 680f, y = 209f, width = 60f, height = 16f, material = MaterialType.WOOD)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 640f, y = 460f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 720f, y = 460f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 680f, y = 362f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 680f, y = 181f, type = MonkeyType.BASIC)
                )
            ),

            // LEVEL 5: Helmet Trouble
            LevelDef(
                id = 5,
                title = "Helmet Trouble",
                subtitle = "Armor & Collapse",
                description = "Armored Monkeys resist frontal hits. Collapse heavy structures onto them to defeat them!",
                fruitQueue = listOf(FruitType.APPLE, FruitType.ORANGE, FruitType.BANANA),
                groundY = 480f,
                starThresholds = Triple(18000, 28000, 36000),
                blocks = listOf(
                    BlockDef(x = 580f, y = 432.5f, width = 28f, height = 95f, material = MaterialType.WOOD),
                    BlockDef(x = 680f, y = 432.5f, width = 28f, height = 95f, material = MaterialType.WOOD),
                    BlockDef(x = 630f, y = 373f, width = 135f, height = 24f, material = MaterialType.WOOD),
                    BlockDef(x = 605f, y = 321f, width = 18f, height = 80f, material = MaterialType.GLASS),
                    BlockDef(x = 655f, y = 321f, width = 18f, height = 80f, material = MaterialType.GLASS),
                    BlockDef(x = 630f, y = 272f, width = 90f, height = 18f, material = MaterialType.WOOD),
                    BlockDef(x = 630f, y = 245.5f, width = 50f, height = 35f, material = MaterialType.STONE)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 630f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 630f, y = 341f, type = MonkeyType.BASIC)
                )
            ),

            // LEVEL 6: Triple Cherry Shot
            LevelDef(
                id = 6,
                title = "Triple Cherry Shot",
                subtitle = "Cluster Precision",
                description = "Tap the screen to split Cherry into three high-speed projectiles covering multiple heights!",
                fruitQueue = listOf(FruitType.CHERRY, FruitType.CHERRY, FruitType.APPLE),
                unlockedNewFruit = FruitType.CHERRY,
                groundY = 480f,
                starThresholds = Triple(20000, 30000, 40000),
                blocks = listOf(
                    BlockDef(x = 560f, y = 435f, width = 18f, height = 90f, material = MaterialType.GLASS),
                    BlockDef(x = 660f, y = 435f, width = 18f, height = 90f, material = MaterialType.GLASS),
                    BlockDef(x = 610f, y = 381f, width = 130f, height = 18f, material = MaterialType.WOOD),
                    BlockDef(x = 575f, y = 329.5f, width = 18f, height = 85f, material = MaterialType.GLASS),
                    BlockDef(x = 645f, y = 329.5f, width = 18f, height = 85f, material = MaterialType.GLASS),
                    BlockDef(x = 610f, y = 278f, width = 100f, height = 18f, material = MaterialType.WOOD),
                    BlockDef(x = 590f, y = 226.5f, width = 18f, height = 85f, material = MaterialType.GLASS),
                    BlockDef(x = 630f, y = 226.5f, width = 18f, height = 85f, material = MaterialType.GLASS),
                    BlockDef(x = 610f, y = 175f, width = 70f, height = 18f, material = MaterialType.WOOD),
                    BlockDef(x = 800f, y = 425f, width = 22f, height = 110f, material = MaterialType.WOOD),
                    BlockDef(x = 800f, y = 361f, width = 60f, height = 18f, material = MaterialType.WOOD)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 610f, y = 462f, type = MonkeyType.NIMBLE),
                    MonkeyDef(x = 610f, y = 354f, type = MonkeyType.NIMBLE),
                    MonkeyDef(x = 610f, y = 249f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 800f, y = 334f, type = MonkeyType.NIMBLE)
                )
            ),

            // LEVEL 7: Shielded Outpost
            LevelDef(
                id = 7,
                title = "Shielded Outpost",
                subtitle = "Fortress Defense",
                description = "Shielded monkeys block direct frontal attacks. Arc high above or explode from behind!",
                fruitQueue = listOf(FruitType.ORANGE, FruitType.BANANA, FruitType.CHERRY, FruitType.APPLE),
                groundY = 480f,
                starThresholds = Triple(22000, 32000, 42000),
                blocks = listOf(
                    BlockDef(x = 540f, y = 420f, width = 32f, height = 120f, material = MaterialType.STONE),
                    BlockDef(x = 540f, y = 348f, width = 50f, height = 24f, material = MaterialType.STONE),
                    BlockDef(x = 650f, y = 430f, width = 24f, height = 100f, material = MaterialType.WOOD),
                    BlockDef(x = 740f, y = 430f, width = 24f, height = 100f, material = MaterialType.WOOD),
                    BlockDef(x = 695f, y = 370f, width = 120f, height = 20f, material = MaterialType.WOOD),
                    BlockDef(x = 670f, y = 320f, width = 25f, height = 80f, material = MaterialType.STONE),
                    BlockDef(x = 720f, y = 320f, width = 25f, height = 80f, material = MaterialType.STONE),
                    BlockDef(x = 695f, y = 270f, width = 90f, height = 20f, material = MaterialType.STONE)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 600f, y = 458f, type = MonkeyType.SHIELDED),
                    MonkeyDef(x = 695f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 695f, y = 240f, type = MonkeyType.BASIC)
                )
            ),

            // LEVEL 8: Durian Smash
            LevelDef(
                id = 8,
                title = "Durian Smash",
                subtitle = "King of Fruits",
                description = "Meet the Durian! Heavy mass with a ground-shaking shockwave that crushes stone fortresses.",
                fruitQueue = listOf(FruitType.DURIAN, FruitType.APPLE, FruitType.ORANGE),
                unlockedNewFruit = FruitType.DURIAN,
                groundY = 480f,
                starThresholds = Triple(25000, 36000, 48000),
                blocks = listOf(
                    BlockDef(x = 560f, y = 430f, width = 36f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 670f, y = 430f, width = 36f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 780f, y = 430f, width = 36f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 670f, y = 367f, width = 270f, height = 26f, material = MaterialType.STONE),
                    BlockDef(x = 610f, y = 314f, width = 30f, height = 80f, material = MaterialType.STONE),
                    BlockDef(x = 730f, y = 314f, width = 30f, height = 80f, material = MaterialType.STONE),
                    BlockDef(x = 670f, y = 262f, width = 160f, height = 24f, material = MaterialType.STONE),
                    BlockDef(x = 670f, y = 215f, width = 20f, height = 70f, material = MaterialType.WOOD),
                    BlockDef(x = 670f, y = 172f, width = 60f, height = 16f, material = MaterialType.WOOD)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 670f, y = 454f, type = MonkeyType.HEAVY),
                    MonkeyDef(x = 615f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 725f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 670f, y = 146f, type = MonkeyType.NIMBLE)
                )
            ),

            // LEVEL 9: Fortress Infiltration
            LevelDef(
                id = 9,
                title = "Fortress Infiltration",
                subtitle = "Multi-Stage Defense",
                description = "A heavy compound protected by metal beams, stone walls, and shielded defenders.",
                fruitQueue = listOf(FruitType.BANANA, FruitType.DURIAN, FruitType.ORANGE, FruitType.CHERRY),
                groundY = 480f,
                starThresholds = Triple(28000, 40000, 54000),
                blocks = listOf(
                    BlockDef(x = 490f, y = 425f, width = 24f, height = 110f, material = MaterialType.METAL),
                    BlockDef(x = 590f, y = 430f, width = 30f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 680f, y = 430f, width = 20f, height = 100f, material = MaterialType.GLASS),
                    BlockDef(x = 770f, y = 430f, width = 30f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 680f, y = 368f, width = 220f, height = 24f, material = MaterialType.WOOD),
                    BlockDef(x = 625f, y = 311f, width = 22f, height = 90f, material = MaterialType.METAL),
                    BlockDef(x = 735f, y = 311f, width = 22f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 680f, y = 255f, width = 140f, height = 22f, material = MaterialType.STONE),
                    BlockDef(x = 680f, y = 209f, width = 20f, height = 70f, material = MaterialType.GLASS),
                    BlockDef(x = 680f, y = 166f, width = 60f, height = 16f, material = MaterialType.WOOD)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 540f, y = 458f, type = MonkeyType.SHIELDED),
                    MonkeyDef(x = 635f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 725f, y = 460f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 680f, y = 330f, type = MonkeyType.HEAVY),
                    MonkeyDef(x = 680f, y = 140f, type = MonkeyType.NIMBLE)
                )
            ),

            // LEVEL 10: The Grand Banana Fortress
            LevelDef(
                id = 10,
                title = "The Grand Banana Fortress",
                subtitle = "The Final Showdown",
                description = "The ultimate fortress! Utilize all 5 fruits strategically to defeat the King Monkey and his army!",
                fruitQueue = listOf(
                    FruitType.APPLE,
                    FruitType.BANANA,
                    FruitType.ORANGE,
                    FruitType.CHERRY,
                    FruitType.DURIAN
                ),
                groundY = 480f,
                starThresholds = Triple(35000, 52000, 68000),
                blocks = listOf(
                    BlockDef(x = 480f, y = 425f, width = 24f, height = 110f, material = MaterialType.WOOD),
                    BlockDef(x = 530f, y = 425f, width = 18f, height = 110f, material = MaterialType.GLASS),
                    BlockDef(x = 505f, y = 360f, width = 80f, height = 20f, material = MaterialType.WOOD),
                    BlockDef(x = 600f, y = 425f, width = 32f, height = 110f, material = MaterialType.METAL),
                    BlockDef(x = 700f, y = 425f, width = 32f, height = 110f, material = MaterialType.STONE),
                    BlockDef(x = 800f, y = 425f, width = 32f, height = 110f, material = MaterialType.METAL),
                    BlockDef(x = 700f, y = 357f, width = 240f, height = 26f, material = MaterialType.STONE),
                    BlockDef(x = 635f, y = 294f, width = 24f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 700f, y = 294f, width = 18f, height = 100f, material = MaterialType.GLASS),
                    BlockDef(x = 765f, y = 294f, width = 24f, height = 100f, material = MaterialType.STONE),
                    BlockDef(x = 700f, y = 233f, width = 170f, height = 22f, material = MaterialType.WOOD),
                    BlockDef(x = 670f, y = 177f, width = 20f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 730f, y = 177f, width = 20f, height = 90f, material = MaterialType.WOOD),
                    BlockDef(x = 700f, y = 122f, width = 90f, height = 20f, material = MaterialType.STONE),
                    BlockDef(x = 700f, y = 87f, width = 24f, height = 50f, material = MaterialType.METAL)
                ),
                monkeys = listOf(
                    MonkeyDef(x = 505f, y = 332f, type = MonkeyType.NIMBLE),
                    MonkeyDef(x = 555f, y = 458f, type = MonkeyType.SHIELDED),
                    MonkeyDef(x = 650f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 750f, y = 458f, type = MonkeyType.ARMORED),
                    MonkeyDef(x = 700f, y = 324f, type = MonkeyType.BASIC),
                    MonkeyDef(x = 700f, y = 196f, type = MonkeyType.HEAVY)
                )
            )
        )
    }
}
