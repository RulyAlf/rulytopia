package com.example.rulytopia.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.rulytopia.model.MaterialType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural Audio & Haptics Engine for Rulytopia.
 * Generates rich, snappy, arcade-style sound effects and cozy background music using synthesized PCM audio.
 */
class SoundManager(private val context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null

    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
    var isVibrationEnabled: Boolean = true

    private val sampleRate = 22050
    private val audioTracks = ConcurrentLinkedQueue<AudioTrack>()

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        // Start background music loop
        startMusic()
    }

    fun startMusic() {
        if (musicJob != null && musicJob?.isActive == true) return
        musicJob = coroutineScope.launch {
            // Cozy tropical marimba/island synth progression in C Pentatonic: C4, D4, E4, G4, A4, C5
            val chords = listOf(
                listOf(261.63f, 329.63f, 392.00f), // C Maj
                listOf(220.00f, 261.63f, 329.63f), // Am
                listOf(174.61f, 220.00f, 261.63f), // F Maj
                listOf(196.00f, 246.94f, 293.66f)  // G Maj
            )
            var step = 0
            while (isActive) {
                if (isMusicEnabled) {
                    val chord = chords[(step / 4) % chords.size]
                    val noteFreq = chord[step % chord.size] * (if (step % 2 == 0) 1f else 1.5f)
                    playMarimbaTone(noteFreq, durationMs = 180, volume = 0.12f)
                }
                delay(220)
                step++
            }
        }
    }

    fun stopMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    fun setMusicActive(enabled: Boolean) {
        isMusicEnabled = enabled
        if (enabled && (musicJob == null || musicJob?.isActive == false)) {
            startMusic()
        }
    }

    // --- SOUND EFFECTS ---

    fun playSlingshotStretch(intensity: Float) {
        if (!isSoundEnabled) return
        coroutineScope.launch {
            val freq = 120f + (intensity.coerceIn(0f, 1f) * 160f)
            playPcmTone(
                durationMs = 60,
                startFreq = freq,
                endFreq = freq + 40f,
                waveType = WaveType.TRIANGLE,
                volume = 0.25f
            )
        }
    }

    fun playLaunch() {
        if (!isSoundEnabled) return
        vibrate(30)
        coroutineScope.launch {
            // Whoosh sound: frequency sweep up + filtered noise
            playPcmTone(
                durationMs = 140,
                startFreq = 180f,
                endFreq = 620f,
                waveType = WaveType.SINE,
                volume = 0.45f
            )
        }
    }

    fun playImpact(material: MaterialType, impulse: Float) {
        if (!isSoundEnabled) return
        val strength = (impulse / 30f).coerceIn(0.2f, 1.0f)
        if (strength > 0.5f) {
            vibrate((strength * 40).toLong())
        }

        coroutineScope.launch {
            when (material) {
                MaterialType.WOOD -> {
                    // Wood thud + snap
                    playPcmTone(100, 220f, 110f, WaveType.TRIANGLE, 0.4f * strength)
                    playNoiseBurst(50, 0.3f * strength)
                }
                MaterialType.GLASS -> {
                    // Glass tink
                    playPcmTone(90, 1400f, 900f, WaveType.SINE, 0.45f * strength)
                }
                MaterialType.STONE -> {
                    // Stone heavy thud
                    playPcmTone(120, 140f, 60f, WaveType.SQUARE, 0.4f * strength)
                    playNoiseBurst(70, 0.35f * strength)
                }
                MaterialType.METAL -> {
                    // Metallic clink
                    playPcmTone(110, 880f, 660f, WaveType.SINE, 0.5f * strength)
                    playPcmTone(90, 1760f, 1320f, WaveType.SINE, 0.25f * strength)
                }
            }
        }
    }

    fun playBreak(material: MaterialType) {
        if (!isSoundEnabled) return
        vibrate(50)
        coroutineScope.launch {
            when (material) {
                MaterialType.WOOD -> {
                    playPcmTone(120, 280f, 80f, WaveType.TRIANGLE, 0.5f)
                    playNoiseBurst(120, 0.5f)
                }
                MaterialType.GLASS -> {
                    playPcmTone(160, 2200f, 1100f, WaveType.SINE, 0.6f)
                    playNoiseBurst(140, 0.4f)
                }
                MaterialType.STONE -> {
                    playPcmTone(180, 110f, 40f, WaveType.SQUARE, 0.6f)
                    playNoiseBurst(160, 0.6f)
                }
                MaterialType.METAL -> {
                    playPcmTone(200, 650f, 320f, WaveType.SQUARE, 0.55f)
                    playNoiseBurst(120, 0.5f)
                }
            }
        }
    }

    fun playMonkeyReaction() {
        if (!isSoundEnabled) return
        coroutineScope.launch {
            // Cartoon monkey "eek!" chirp (quick up-sweep)
            playPcmTone(70, 480f, 960f, WaveType.SINE, 0.4f)
            delay(50)
            playPcmTone(90, 720f, 1280f, WaveType.SINE, 0.35f)
        }
    }

    fun playMonkeyDefeat() {
        if (!isSoundEnabled) return
        vibrate(75)
        coroutineScope.launch {
            // Humorous defeat "poof!" + descending whistle
            playPcmTone(80, 880f, 220f, WaveType.SINE, 0.5f)
            playNoiseBurst(100, 0.45f)
            delay(60)
            playPcmTone(120, 440f, 150f, WaveType.TRIANGLE, 0.4f)
        }
    }

    fun playAbilityBanana() {
        if (!isSoundEnabled) return
        vibrate(40)
        coroutineScope.launch {
            // Rocket swoosh
            playPcmTone(140, 300f, 850f, WaveType.TRIANGLE, 0.55f)
            playPcmTone(100, 600f, 1200f, WaveType.SINE, 0.35f)
        }
    }

    fun playAbilityOrange() {
        if (!isSoundEnabled) return
        vibrate(80)
        coroutineScope.launch {
            // Explosive boom
            playPcmTone(220, 180f, 45f, WaveType.SQUARE, 0.7f)
            playNoiseBurst(200, 0.65f)
        }
    }

    fun playAbilityCherry() {
        if (!isSoundEnabled) return
        vibrate(35)
        coroutineScope.launch {
            // Triple pop
            playPcmTone(40, 650f, 1100f, WaveType.SINE, 0.4f)
            delay(35)
            playPcmTone(40, 800f, 1300f, WaveType.SINE, 0.4f)
            delay(35)
            playPcmTone(40, 950f, 1500f, WaveType.SINE, 0.4f)
        }
    }

    fun playAbilityDurian() {
        if (!isSoundEnabled) return
        vibrate(100)
        coroutineScope.launch {
            // Heavy earthquake seismic rumble
            playPcmTone(300, 90f, 30f, WaveType.SQUARE, 0.8f)
            playNoiseBurst(260, 0.7f)
        }
    }

    fun playStar(starIndex: Int) {
        if (!isSoundEnabled) return
        vibrate(40)
        coroutineScope.launch {
            val freqs = listOf(523.25f, 659.25f, 783.99f) // C5, E5, G5
            val freq = freqs.getOrElse(starIndex) { 880f }
            playPcmTone(180, freq, freq + 50f, WaveType.SINE, 0.6f)
            delay(50)
            playPcmTone(150, freq * 1.5f, freq * 1.5f, WaveType.SINE, 0.3f)
        }
    }

    fun playVictory() {
        if (!isSoundEnabled) return
        vibrate(120)
        coroutineScope.launch {
            val notes = listOf(392f, 523f, 659f, 784f) // G4, C5, E5, G5
            for (note in notes) {
                playPcmTone(120, note, note, WaveType.TRIANGLE, 0.5f)
                delay(100)
            }
        }
    }

    fun playDefeat() {
        if (!isSoundEnabled) return
        coroutineScope.launch {
            val notes = listOf(330f, 311f, 293f, 261f) // E4, Eb4, D4, C4
            for (note in notes) {
                playPcmTone(150, note, note - 10f, WaveType.SQUARE, 0.4f)
                delay(130)
            }
        }
    }

    fun playButtonClick() {
        if (!isSoundEnabled) return
        vibrate(15)
        coroutineScope.launch {
            playPcmTone(35, 600f, 400f, WaveType.SINE, 0.3f)
        }
    }

    // --- HAPTICS ---

    private fun vibrate(durationMs: Long) {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        durationMs.coerceAtLeast(10),
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Ignore if vibration unavailable
        }
    }

    // --- PCM SYNTHESIZER CORE ---

    private enum class WaveType {
        SINE, TRIANGLE, SQUARE
    }

    private fun playPcmTone(
        durationMs: Int,
        startFreq: Float,
        endFreq: Float,
        waveType: WaveType,
        volume: Float
    ) {
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt().coerceAtLeast(64)
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = startFreq + (endFreq - startFreq) * t
            val phaseInc = 2.0 * PI * freq / sampleRate
            phase += phaseInc

            val rawSample: Float = when (waveType) {
                WaveType.SINE -> sin(phase).toFloat()
                WaveType.TRIANGLE -> (2.0 * kotlin.math.abs(2.0 * (phase / (2.0 * PI) - kotlin.math.floor(phase / (2.0 * PI) + 0.5)) ) - 1.0).toFloat()
                WaveType.SQUARE -> if (sin(phase) >= 0) 0.8f else -0.8f
            }

            // Exponential decay envelope
            val envelope = (1f - t) * (1f - exp(-5f * (1f - t)))
            val sample = (rawSample * envelope * volume * Short.MAX_VALUE).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            )
            buffer[i] = sample.toShort()
        }

        playBuffer(buffer)
    }

    private fun playNoiseBurst(durationMs: Int, volume: Float) {
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt().coerceAtLeast(64)
        val buffer = ShortArray(numSamples)
        val random = Random(System.currentTimeMillis())

        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val envelope = (1f - t) * (1f - t)
            val noise = (random.nextFloat() * 2f - 1f)
            val sample = (noise * envelope * volume * Short.MAX_VALUE).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            )
            buffer[i] = sample.toShort()
        }

        playBuffer(buffer)
    }

    private fun playMarimbaTone(freq: Float, durationMs: Int, volume: Float) {
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt().coerceAtLeast(64)
        val buffer = ShortArray(numSamples)
        var phase1 = 0.0
        var phase2 = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            phase1 += 2.0 * PI * freq / sampleRate
            phase2 += 2.0 * PI * (freq * 2.756f) / sampleRate // Marimba overtone

            val fundamental = sin(phase1).toFloat()
            val overtone = sin(phase2).toFloat() * 0.25f
            val envelope = exp(-7.0 * t).toFloat()

            val sample = ((fundamental + overtone) * envelope * volume * Short.MAX_VALUE).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            )
            buffer[i] = sample.toShort()
        }

        playBuffer(buffer)
    }

    private fun playBuffer(buffer: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()

            // Release after playing
            coroutineScope.launch {
                delay((buffer.size * 1000L / sampleRate) + 50)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Silently ignore audio track init issues
        }
    }

    fun release() {
        stopMusic()
    }
}
