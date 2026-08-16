package com.example.rulytopia.model

data class BlockDef(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val material: MaterialType,
    val shape: BlockShape = BlockShape.RECTANGLE,
    val angle: Float = 0f
)

data class MonkeyDef(
    val x: Float,
    val y: Float,
    val type: MonkeyType = MonkeyType.BASIC
)

data class LevelDef(
    val id: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val fruitQueue: List<FruitType>,
    val unlockedNewFruit: FruitType? = null,
    val blocks: List<BlockDef>,
    val monkeys: List<MonkeyDef>,
    val starThresholds: Triple<Int, Int, Int>,
    val slingshotX: Float = 140f,
    val slingshotY: Float = 360f,
    val groundY: Float = 480f,
    val worldWidth: Float = 1100f,
    val worldHeight: Float = 560f
)
