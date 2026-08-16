package com.example.rulytopia.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rulytopia.audio.SoundManager
import com.example.rulytopia.data.GamePreferences
import com.example.rulytopia.model.*
import com.example.rulytopia.physics.PhysicsEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

enum class GameScreen {
    MAIN_MENU,
    LEVEL_SELECT,
    PLAYING
}

enum class PlayState {
    AIMING,
    AIRBORNE,
    SETTLING,
    LEVEL_COMPLETE,
    LEVEL_FAILED,
    PAUSED
}

data class GameUiState(
    val currentScreen: GameScreen = GameScreen.MAIN_MENU,
    val playState: PlayState = PlayState.AIMING,
    val currentLevelId: Int = 1,
    val score: Int = 0,
    val remainingFruits: List<FruitType> = emptyList(),
    val currentFruit: FruitType? = null,
    val starsEarned: Int = 0,
    val isSlingshotDragging: Boolean = false,
    val slingshotPullPos: Vector2D = Vector2D(140f, 360f),
    val slingshotAnchor: Vector2D = Vector2D(140f, 360f),
    val trajectoryPoints: List<Vector2D> = emptyList(),
    val activeFruitAbilityAvailable: Boolean = false,
    val scorePopups: List<ScorePopup> = emptyList(),
    val cameraOffsetX: Float = 0f,
    val cameraOffsetY: Float = 0f,
    val cameraShakeOffsetX: Float = 0f,
    val cameraShakeOffsetY: Float = 0f,
    val cameraZoom: Float = 1.0f,
    val showTutorialDialog: FruitType? = null,
    val showSettingsDialog: Boolean = false,
    val isSoundEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val totalStars: Int = 0
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = GamePreferences(application.applicationContext)
    val soundManager = SoundManager(application.applicationContext)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var currentLevelDef: LevelDef = LevelRepository.getLevel(1)
    private var gameLoopJob: Job? = null
    private var settlingTimer: Float = 0f
    private var shakeTrauma: Float = 0f

    val physicsEngine: PhysicsEngine = PhysicsEngine(
        soundManager = soundManager,
        onScoreAdded = { points, position, text, isCrit ->
            addScore(points, position, text, isCrit)
        },
        onMonkeyDefeated = { _ ->
            checkLevelProgress()
        },
        onBlockBroken = { _ ->
            // Handled in score callback
        }
    )

    private val maxPullRadius = 78f
    private val launchSpeedMultiplier = 14.5f

    init {
        // Connect Screen Shake
        physicsEngine.onScreenShake = { intensity ->
            shakeTrauma = (shakeTrauma + (intensity / 15f)).coerceIn(0f, 1f)
        }

        // Sync preferences with sound manager
        soundManager.isSoundEnabled = preferences.isSoundEnabled
        soundManager.isMusicEnabled = preferences.isMusicEnabled
        soundManager.isVibrationEnabled = preferences.isVibrationEnabled

        _uiState.update {
            it.copy(
                isSoundEnabled = preferences.isSoundEnabled,
                isMusicEnabled = preferences.isMusicEnabled,
                isVibrationEnabled = preferences.isVibrationEnabled,
                totalStars = preferences.getTotalStars()
            )
        }
    }

    // --- NAVIGATION ---

    fun navigateTo(screen: GameScreen) {
        soundManager.playButtonClick()
        _uiState.update { it.copy(currentScreen = screen, totalStars = preferences.getTotalStars()) }
        if (screen != GameScreen.PLAYING) {
            stopGameLoop()
        }
    }

    fun startLevel(levelId: Int) {
        soundManager.playButtonClick()
        currentLevelDef = LevelRepository.getLevel(levelId)

        physicsEngine.loadLevel(currentLevelDef)

        val fruitQueue = currentLevelDef.fruitQueue.toMutableList()
        val firstFruit = if (fruitQueue.isNotEmpty()) fruitQueue.removeAt(0) else FruitType.APPLE

        val anchor = Vector2D(currentLevelDef.slingshotX, currentLevelDef.slingshotY)

        _uiState.update {
            it.copy(
                currentScreen = GameScreen.PLAYING,
                playState = PlayState.AIMING,
                currentLevelId = levelId,
                score = 0,
                remainingFruits = fruitQueue,
                currentFruit = firstFruit,
                starsEarned = 0,
                isSlingshotDragging = false,
                slingshotAnchor = anchor,
                slingshotPullPos = anchor.copy(),
                trajectoryPoints = emptyList(),
                activeFruitAbilityAvailable = false,
                scorePopups = emptyList(),
                cameraOffsetX = 0f,
                cameraOffsetY = 0f,
                cameraZoom = 1.0f,
                showTutorialDialog = null
            )
        }

        // Check if level unlocks a new fruit that the player hasn't seen tutorial for
        if (currentLevelDef.unlockedNewFruit != null && !preferences.hasSeenFruitTutorial(currentLevelDef.unlockedNewFruit!!)) {
            _uiState.update { it.copy(showTutorialDialog = currentLevelDef.unlockedNewFruit) }
        }

        startGameLoop()
    }

    fun restartLevel() {
        soundManager.playButtonClick()
        startLevel(_uiState.value.currentLevelId)
    }

    fun nextLevel() {
        soundManager.playButtonClick()
        val nextId = (_uiState.value.currentLevelId + 1).coerceAtMost(10)
        startLevel(nextId)
    }

    fun dismissTutorial() {
        val fruit = _uiState.value.showTutorialDialog
        if (fruit != null) {
            preferences.setSeenFruitTutorial(fruit)
        }
        _uiState.update { it.copy(showTutorialDialog = null) }
    }

    // --- SLINGSHOT TOUCH CONTROLS ---

    fun onSlingshotTouchStart(touchPos: Vector2D) {
        if (_uiState.value.playState != PlayState.AIMING) return
        val anchor = _uiState.value.slingshotAnchor

        // Check if touch is reasonably close to slingshot area
        if (touchPos.distanceTo(anchor) < 160f) {
            _uiState.update {
                it.copy(
                    isSlingshotDragging = true,
                    slingshotPullPos = clampPullPosition(anchor, touchPos)
                )
            }
            updateTrajectory()
        }
    }

    fun onSlingshotTouchMove(touchPos: Vector2D) {
        if (!_uiState.value.isSlingshotDragging) return
        val anchor = _uiState.value.slingshotAnchor
        val clampedPos = clampPullPosition(anchor, touchPos)

        val pullDist = clampedPos.distanceTo(anchor)
        soundManager.playSlingshotStretch(pullDist / maxPullRadius)

        _uiState.update {
            it.copy(
                slingshotPullPos = clampedPos
            )
        }
        updateTrajectory()
    }

    fun onSlingshotTouchRelease() {
        if (!_uiState.value.isSlingshotDragging) return
        val anchor = _uiState.value.slingshotAnchor
        val pullPos = _uiState.value.slingshotPullPos
        val pullVector = anchor - pullPos
        val pullDist = pullVector.length()

        if (pullDist > 18f) {
            // Launch the fruit!
            launchFruit(pullVector)
        } else {
            // Cancel pull
            _uiState.update {
                it.copy(
                    isSlingshotDragging = false,
                    slingshotPullPos = anchor.copy(),
                    trajectoryPoints = emptyList()
                )
            }
        }
    }

    private fun clampPullPosition(anchor: Vector2D, touchPos: Vector2D): Vector2D {
        // Can only pull backwards (to the left of anchor) or downwards
        val delta = touchPos - anchor
        // Restrict drag distance
        val dist = delta.length()
        return if (dist > maxPullRadius) {
            anchor + (delta.normalized() * maxPullRadius)
        } else {
            touchPos
        }
    }

    private fun updateTrajectory() {
        val anchor = _uiState.value.slingshotAnchor
        val pullPos = _uiState.value.slingshotPullPos
        val fruit = _uiState.value.currentFruit ?: return

        val pullVector = anchor - pullPos
        val launchVelocity = pullVector * (launchSpeedMultiplier * fruit.launchSpeedMult)

        val points = physicsEngine.calculateTrajectory(
            startPos = anchor,
            initialVelocity = launchVelocity,
            fruitType = fruit
        )

        _uiState.update { it.copy(trajectoryPoints = points) }
    }

    private fun launchFruit(pullVector: Vector2D) {
        val currentFruitType = _uiState.value.currentFruit ?: return
        val anchor = _uiState.value.slingshotAnchor
        val launchVelocity = pullVector * (launchSpeedMultiplier * currentFruitType.launchSpeedMult)

        val fruitEntity = FruitEntity(
            id = System.currentTimeMillis(),
            type = currentFruitType,
            position = anchor.copy(),
            velocity = launchVelocity,
            isLaunched = true,
            hasUsedAbility = false
        )

        physicsEngine.fruits.clear()
        physicsEngine.fruits.add(fruitEntity)
        physicsEngine.hasFirstShotOccurred = true

        soundManager.playLaunch()

        settlingTimer = 0f
        _uiState.update {
            it.copy(
                isSlingshotDragging = false,
                slingshotPullPos = anchor.copy(),
                trajectoryPoints = emptyList(),
                playState = PlayState.AIRBORNE,
                activeFruitAbilityAvailable = true
            )
        }
    }

    // --- SPECIAL ABILITY ACTIVATION ---

    fun onScreenTapped() {
        val state = _uiState.value.playState
        if (state != PlayState.AIRBORNE) return

        val activeFruit = physicsEngine.fruits.firstOrNull { it.isLaunched && !it.isDead && !it.hasUsedAbility }
        if (activeFruit != null) {
            when (activeFruit.type) {
                FruitType.BANANA -> {
                    physicsEngine.triggerBananaBoost(activeFruit)
                    _uiState.update { it.copy(activeFruitAbilityAvailable = false) }
                }
                FruitType.ORANGE -> {
                    physicsEngine.triggerOrangeBurst(activeFruit)
                    _uiState.update { it.copy(activeFruitAbilityAvailable = false) }
                }
                FruitType.CHERRY -> {
                    val splitCherries = physicsEngine.triggerCherrySplit(activeFruit)
                    physicsEngine.fruits.addAll(splitCherries)
                    _uiState.update { it.copy(activeFruitAbilityAvailable = false) }
                }
                FruitType.DURIAN -> {
                    physicsEngine.triggerDurianSmash(activeFruit)
                    _uiState.update { it.copy(activeFruitAbilityAvailable = false) }
                }
                FruitType.APPLE -> {
                    // Apple has passive heavy impact
                }
            }
        }
    }

    // --- GAME LOOP & STATE ---

    fun onGameFrame(dt: Float) {
        if (_uiState.value.playState != PlayState.PAUSED &&
            _uiState.value.playState != PlayState.LEVEL_COMPLETE &&
            _uiState.value.playState != PlayState.LEVEL_FAILED
        ) {
            updateGame(dt)
        }
    }

    private fun startGameLoop() {
        stopGameLoop()
        // Primary game loop is driven at VSync 60fps via withFrameNanos in GamePlayScreen
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun updateGame(dt: Float) {
        physicsEngine.step(dt)

        // Update score popups
        updatePopups(dt)

        // Update camera position
        updateCamera(dt)

        // Check flight settling or next fruit loading
        if (_uiState.value.playState == PlayState.AIRBORNE || _uiState.value.playState == PlayState.SETTLING) {
            val allFruitsResting = physicsEngine.fruits.all { it.isResting || it.isDead || it.position.x > currentLevelDef.worldWidth + 50f }

            if (allFruitsResting) {
                settlingTimer += dt
                if (settlingTimer > 1.4f) {
                    settlingTimer = 0f
                    onFlightFinished()
                }
            }
        }
    }

    private fun onFlightFinished() {
        // Check if all monkeys are defeated
        val aliveMonkeys = physicsEngine.monkeys.filter { !it.isDefeated }
        if (aliveMonkeys.isEmpty()) {
            handleLevelVictory()
            return
        }

        // Check if we have more fruits in queue
        val remaining = _uiState.value.remainingFruits.toMutableList()
        if (remaining.isNotEmpty()) {
            val nextFruit = remaining.removeAt(0)
            _uiState.update {
                it.copy(
                    playState = PlayState.AIMING,
                    currentFruit = nextFruit,
                    remainingFruits = remaining,
                    activeFruitAbilityAvailable = false
                )
            }
        } else {
            // No more fruits and monkeys alive -> Level Failed!
            handleLevelDefeat()
        }
    }

    private fun handleLevelVictory() {
        // Calculate bonus for unused fruits (+10,000 pts each)
        val unusedCount = _uiState.value.remainingFruits.size + (if (_uiState.value.playState == PlayState.AIMING) 1 else 0)
        val fruitBonus = unusedCount * 10000
        val finalScore = _uiState.value.score + fruitBonus

        val thresholds = currentLevelDef.starThresholds
        val stars = when {
            finalScore >= thresholds.third -> 3
            finalScore >= thresholds.second -> 2
            finalScore >= thresholds.first -> 1
            else -> 1 // Completed gets at least 1 star
        }

        // Save progress locally
        preferences.setLevelStars(currentLevelDef.id, stars)
        preferences.setLevelHighScore(currentLevelDef.id, finalScore)
        preferences.unlockLevel(currentLevelDef.id + 1)

        soundManager.playVictory()
        physicsEngine.spawnConfettiVictory()

        _uiState.update {
            it.copy(
                playState = PlayState.LEVEL_COMPLETE,
                score = finalScore,
                starsEarned = stars,
                totalStars = preferences.getTotalStars()
            )
        }
    }

    private fun handleLevelDefeat() {
        soundManager.playDefeat()
        _uiState.update {
            it.copy(
                playState = PlayState.LEVEL_FAILED
            )
        }
    }

    private fun checkLevelProgress() {
        val aliveMonkeys = physicsEngine.monkeys.filter { !it.isDefeated }
        if (aliveMonkeys.isEmpty()) {
            // Trigger victory sequence
            viewModelScope.launch {
                delay(600)
                handleLevelVictory()
            }
        }
    }

    private fun addScore(points: Int, position: Vector2D, text: String, isCrit: Boolean) {
        val newScore = _uiState.value.score + points
        val popup = ScorePopup(
            position = position.copy() + Vector2D(0f, -20f),
            text = text,
            color = if (isCrit) Color(0xFFFFEB3B) else Color.White,
            isCritical = isCrit
        )

        _uiState.update {
            it.copy(
                score = newScore,
                scorePopups = it.scorePopups + popup
            )
        }
    }

    private fun updatePopups(dt: Float) {
        val updated = mutableListOf<ScorePopup>()
        for (p in _uiState.value.scorePopups) {
            p.lifeTime -= dt
            p.position += Vector2D(0f, -40f * dt)
            if (p.lifeTime > 0f) {
                updated.add(p)
            }
        }
        if (updated.size != _uiState.value.scorePopups.size) {
            _uiState.update { it.copy(scorePopups = updated) }
        }
    }

    private fun updateCamera(dt: Float) {
        val activeFruit = physicsEngine.fruits.firstOrNull { it.isLaunched && !it.isDead && !it.isResting }
        val targetOffsetX: Float

        if (activeFruit != null && _uiState.value.playState == PlayState.AIRBORNE) {
            // Track fruit with leading offset
            targetOffsetX = (activeFruit.position.x - 300f).coerceIn(0f, (currentLevelDef.worldWidth - 550f).coerceAtLeast(0f))
        } else {
            // Default framing: centered on slingshot & structure
            targetOffsetX = 0f
        }

        // Smooth camera lerp
        val currentOffsetX = _uiState.value.cameraOffsetX
        val newOffsetX = currentOffsetX + (targetOffsetX - currentOffsetX) * (dt * 4.5f).coerceIn(0f, 1f)

        // Screen Shake calculation using non-linear trauma decay
        var shakeX = 0f
        var shakeY = 0f
        if (shakeTrauma > 0.001f) {
            val shakeAmount = shakeTrauma * shakeTrauma * 16f
            shakeX = (kotlin.random.Random.nextFloat() - 0.5f) * 2f * shakeAmount
            shakeY = (kotlin.random.Random.nextFloat() - 0.5f) * 2f * shakeAmount
            shakeTrauma = (shakeTrauma - dt * 2.2f).coerceAtLeast(0f)
        }

        _uiState.update {
            it.copy(
                cameraOffsetX = newOffsetX,
                cameraShakeOffsetX = shakeX,
                cameraShakeOffsetY = shakeY
            )
        }
    }

    // --- PAUSE & SETTINGS ---

    fun pauseGame() {
        soundManager.playButtonClick()
        _uiState.update { it.copy(playState = PlayState.PAUSED) }
    }

    fun resumeGame() {
        soundManager.playButtonClick()
        _uiState.update { it.copy(playState = PlayState.AIMING) }
    }

    fun toggleSettingsDialog(show: Boolean) {
        soundManager.playButtonClick()
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun toggleSound(enabled: Boolean) {
        soundManager.isSoundEnabled = enabled
        preferences.isSoundEnabled = enabled
        _uiState.update { it.copy(isSoundEnabled = enabled) }
    }

    fun toggleMusic(enabled: Boolean) {
        soundManager.setMusicActive(enabled)
        preferences.isMusicEnabled = enabled
        _uiState.update { it.copy(isMusicEnabled = enabled) }
    }

    fun toggleVibration(enabled: Boolean) {
        soundManager.isVibrationEnabled = enabled
        preferences.isVibrationEnabled = enabled
        _uiState.update { it.copy(isVibrationEnabled = enabled) }
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
        soundManager.release()
    }
}
