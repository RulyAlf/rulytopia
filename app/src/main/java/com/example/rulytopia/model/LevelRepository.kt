package com.example.rulytopia.model

object LevelRepository {

    val levels: List<LevelDef> = listOf(
        // LEVEL 1: First Harvest (Apple tutorial, basic wooden tower, 2 monkeys)
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
                // Left column
                BlockDef(x = 620f, y = 435f, width = 24f, height = 90f, material = MaterialType.WOOD),
                // Right column
                BlockDef(x = 720f, y = 435f, width = 24f, height = 90f, material = MaterialType.WOOD),
                // Horizontal plank
                BlockDef(x = 670f, y = 380f, width = 140f, height = 20f, material = MaterialType.WOOD),
                // Second tier left
                BlockDef(x = 640f, y = 332.5f, width = 20f, height = 75f, material = MaterialType.WOOD),
                // Second tier right
                BlockDef(x = 700f, y = 332.5f, width = 20f, height = 75f, material = MaterialType.WOOD),
                // Roof plank
                BlockDef(x = 670f, y = 286f, width = 100f, height = 18f, material = MaterialType.WOOD)
            ),
            monkeys = listOf(
                MonkeyDef(x = 670f, y = 460f, type = MonkeyType.BASIC),
                MonkeyDef(x = 670f, y = 257f, type = MonkeyType.BASIC)
            )
        ),

        // LEVEL 2: Target Practice (Twin towers, trajectory planning)
        LevelDef(
            id = 2,
            title = "Target Practice",
            subtitle = "Twin Towers",
            description = "Aim high to knock the top beams and create a domino collapse!",
            fruitQueue = listOf(FruitType.APPLE, FruitType.APPLE, FruitType.APPLE),
            groundY = 480f,
            starThresholds = Triple(14000, 22000, 29000),
            blocks = listOf(
                // Tower 1
                BlockDef(x = 550f, y = 435f, width = 22f, height = 90f, material = MaterialType.WOOD),
                BlockDef(x = 630f, y = 435f, width = 22f, height = 90f, material = MaterialType.WOOD),
                BlockDef(x = 590f, y = 380f, width = 110f, height = 20f, material = MaterialType.WOOD),
                BlockDef(x = 590f, y = 330f, width = 22f, height = 80f, material = MaterialType.WOOD),
                BlockDef(x = 590f, y = 281f, width = 60f, height = 18f, material = MaterialType.WOOD),

                // Connecting bridge
                BlockDef(x = 685f, y = 380f, width = 80f, height = 16f, material = MaterialType.WOOD),

                // Tower 2
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

        // LEVEL 3: Banana Curve (Introduce Banana, boost mechanic)
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
                // Protective bunker with high front wall
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

        // LEVEL 4: Glasshouse Chaos (Introduce Glass & Orange)
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
                // Glass base structure
                BlockDef(x = 600f, y = 440f, width = 20f, height = 80f, material = MaterialType.GLASS),
                BlockDef(x = 680f, y = 440f, width = 20f, height = 80f, material = MaterialType.GLASS),
                BlockDef(x = 760f, y = 440f, width = 20f, height = 80f, material = MaterialType.GLASS),
                BlockDef(x = 680f, y = 391f, width = 190f, height = 18f, material = MaterialType.GLASS),

                // Upper wooden tier
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

        // LEVEL 5: Helmet Trouble (Introduce Armored Monkey)
        LevelDef(
            id = 5,
            title = "Helmet Trouble",
            subtitle = "Armor & Collapse",
            description = "Armored Monkeys resist frontal hits. Collapse heavy structures onto them to defeat them!",
            fruitQueue = listOf(FruitType.APPLE, FruitType.ORANGE, FruitType.BANANA),
            groundY = 480f,
            starThresholds = Triple(18000, 28000, 36000),
            blocks = listOf(
                // Heavy fortified pillars
                BlockDef(x = 580f, y = 432.5f, width = 28f, height = 95f, material = MaterialType.WOOD),
                BlockDef(x = 680f, y = 432.5f, width = 28f, height = 95f, material = MaterialType.WOOD),
                BlockDef(x = 630f, y = 373f, width = 135f, height = 24f, material = MaterialType.WOOD),

                // Glass mid-section
                BlockDef(x = 605f, y = 321f, width = 18f, height = 80f, material = MaterialType.GLASS),
                BlockDef(x = 655f, y = 321f, width = 18f, height = 80f, material = MaterialType.GLASS),
                BlockDef(x = 630f, y = 272f, width = 90f, height = 18f, material = MaterialType.WOOD),

                // Heavy stone cap
                BlockDef(x = 630f, y = 245.5f, width = 50f, height = 35f, material = MaterialType.STONE)
            ),
            monkeys = listOf(
                MonkeyDef(x = 630f, y = 458f, type = MonkeyType.ARMORED),
                MonkeyDef(x = 630f, y = 341f, type = MonkeyType.BASIC)
            )
        ),

        // LEVEL 6: Triple Cherry Shot (Introduce Cherry, precision split)
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
                // Triple tiered vertical tower
                // Bottom
                BlockDef(x = 560f, y = 435f, width = 18f, height = 90f, material = MaterialType.GLASS),
                BlockDef(x = 660f, y = 435f, width = 18f, height = 90f, material = MaterialType.GLASS),
                BlockDef(x = 610f, y = 381f, width = 130f, height = 18f, material = MaterialType.WOOD),

                // Mid
                BlockDef(x = 575f, y = 329.5f, width = 18f, height = 85f, material = MaterialType.GLASS),
                BlockDef(x = 645f, y = 329.5f, width = 18f, height = 85f, material = MaterialType.GLASS),
                BlockDef(x = 610f, y = 278f, width = 100f, height = 18f, material = MaterialType.WOOD),

                // Top
                BlockDef(x = 590f, y = 226.5f, width = 18f, height = 85f, material = MaterialType.GLASS),
                BlockDef(x = 630f, y = 226.5f, width = 18f, height = 85f, material = MaterialType.GLASS),
                BlockDef(x = 610f, y = 175f, width = 70f, height = 18f, material = MaterialType.WOOD),

                // Distant satellite perch
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

        // LEVEL 7: Shielded Outpost (Stone blocks + Shielded Monkey)
        LevelDef(
            id = 7,
            title = "Shielded Outpost",
            subtitle = "Fortress Defense",
            description = "Shielded monkeys block direct frontal attacks. Arc high above or explode from behind!",
            fruitQueue = listOf(FruitType.ORANGE, FruitType.BANANA, FruitType.CHERRY, FruitType.APPLE),
            groundY = 480f,
            starThresholds = Triple(22000, 32000, 42000),
            blocks = listOf(
                // Stone front bastion
                BlockDef(x = 540f, y = 420f, width = 32f, height = 120f, material = MaterialType.STONE),
                BlockDef(x = 540f, y = 348f, width = 50f, height = 24f, material = MaterialType.STONE),

                // Main courtyard
                BlockDef(x = 650f, y = 430f, width = 24f, height = 100f, material = MaterialType.WOOD),
                BlockDef(x = 740f, y = 430f, width = 24f, height = 100f, material = MaterialType.WOOD),
                BlockDef(x = 695f, y = 370f, width = 120f, height = 20f, material = MaterialType.WOOD),

                // Upper stone keep
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

        // LEVEL 8: Durian Smash (Introduce Durian + Heavy Monkey)
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
                // Massive heavy stone castle
                BlockDef(x = 560f, y = 430f, width = 36f, height = 100f, material = MaterialType.STONE),
                BlockDef(x = 670f, y = 430f, width = 36f, height = 100f, material = MaterialType.STONE),
                BlockDef(x = 780f, y = 430f, width = 36f, height = 100f, material = MaterialType.STONE),
                BlockDef(x = 670f, y = 367f, width = 270f, height = 26f, material = MaterialType.STONE),

                // Tier 2
                BlockDef(x = 610f, y = 314f, width = 30f, height = 80f, material = MaterialType.STONE),
                BlockDef(x = 730f, y = 314f, width = 30f, height = 80f, material = MaterialType.STONE),
                BlockDef(x = 670f, y = 262f, width = 160f, height = 24f, material = MaterialType.STONE),

                // Top wooden lookout
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

        // LEVEL 9: Fortress Infiltration (Multi-material: Wood, Glass, Stone, Metal)
        LevelDef(
            id = 9,
            title = "Fortress Infiltration",
            subtitle = "Multi-Stage Defense",
            description = "A heavy compound protected by metal beams, stone walls, and shielded defenders.",
            fruitQueue = listOf(FruitType.BANANA, FruitType.DURIAN, FruitType.ORANGE, FruitType.CHERRY),
            groundY = 480f,
            starThresholds = Triple(28000, 40000, 54000),
            blocks = listOf(
                // Forward metal barrier
                BlockDef(x = 490f, y = 425f, width = 24f, height = 110f, material = MaterialType.METAL),

                // Central multi-material bunker
                BlockDef(x = 590f, y = 430f, width = 30f, height = 100f, material = MaterialType.STONE),
                BlockDef(x = 680f, y = 430f, width = 20f, height = 100f, material = MaterialType.GLASS),
                BlockDef(x = 770f, y = 430f, width = 30f, height = 100f, material = MaterialType.STONE),
                BlockDef(x = 680f, y = 368f, width = 220f, height = 24f, material = MaterialType.WOOD),

                // Second tier
                BlockDef(x = 625f, y = 311f, width = 22f, height = 90f, material = MaterialType.METAL),
                BlockDef(x = 735f, y = 311f, width = 22f, height = 90f, material = MaterialType.WOOD),
                BlockDef(x = 680f, y = 255f, width = 140f, height = 22f, material = MaterialType.STONE),

                // Top glass crown
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

        // LEVEL 10: The Grand Banana Fortress (Grand Climax: All 5 fruits, Epic Citadel)
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
                // Outer Left Bastion
                BlockDef(x = 480f, y = 425f, width = 24f, height = 110f, material = MaterialType.WOOD),
                BlockDef(x = 530f, y = 425f, width = 18f, height = 110f, material = MaterialType.GLASS),
                BlockDef(x = 505f, y = 360f, width = 80f, height = 20f, material = MaterialType.WOOD),

                // Main Castle Lower Level (Metal + Stone)
                BlockDef(x = 600f, y = 425f, width = 32f, height = 110f, material = MaterialType.METAL),
                BlockDef(x = 700f, y = 425f, width = 32f, height = 110f, material = MaterialType.STONE),
                BlockDef(x = 800f, y = 425f, width = 32f, height = 110f, material = MaterialType.METAL),
                BlockDef(x = 700f, y = 357f, width = 240f, height = 26f, material = MaterialType.STONE),

                // Mid Level Castle (Stone + Glass windows)
                BlockDef(x = 635f, y = 294f, width = 24f, height = 100f, material = MaterialType.STONE),
                BlockDef(x = 700f, y = 294f, width = 18f, height = 100f, material = MaterialType.GLASS),
                BlockDef(x = 765f, y = 294f, width = 24f, height = 100f, material = MaterialType.STONE),
                BlockDef(x = 700f, y = 233f, width = 170f, height = 22f, material = MaterialType.WOOD),

                // High Throne Tower
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

    fun getLevel(id: Int): LevelDef {
        return levels.find { it.id == id } ?: levels.first()
    }
}

