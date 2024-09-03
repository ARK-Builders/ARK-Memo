package dev.arkbuilders.arkmemo.ui.viewmodels

import android.media.audiofx.Visualizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arkmemo.media.ArkMediaPlayer
import dev.arkbuilders.arkmemo.ui.views.WaveView
import dev.arkbuilders.arkmemo.utils.launchPeriodicAsync
import dev.arkbuilders.arkmemo.utils.extractDuration
import dev.arkbuilders.arkmemo.utils.millisToString
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

sealed class ArkMediaPlayerSideEffect {
    data object StartPlaying: ArkMediaPlayerSideEffect()

    data object PausePlaying: ArkMediaPlayerSideEffect()

    data object ResumePlaying: ArkMediaPlayerSideEffect()

    data object StopPlaying: ArkMediaPlayerSideEffect()
}

data class ArkMediaPlayerState(
    val currentPos: Int,
    val duration: String,
    val maxAmplitude: Int
)

@HiltViewModel
class ArkMediaPlayerViewModel @Inject constructor(
    private val arkMediaPlayer: ArkMediaPlayer
): ViewModel() {

    private var currentPlayingVoiceNotePath: String = ""
    private val arkMediaPlayerSideEffect = MutableStateFlow<ArkMediaPlayerSideEffect?>(null)
    private val arkMediaPlayerState = MutableStateFlow<ArkMediaPlayerState?>(null)
    val playerState = arkMediaPlayerState as StateFlow<ArkMediaPlayerState?>
    val playerSideEffect = arkMediaPlayerSideEffect as StateFlow<ArkMediaPlayerSideEffect?>

    private var progressJob: Deferred<*>? = null
    private var visualizer: Visualizer? = null

    fun initPlayer(path: String) {
        currentPlayingVoiceNotePath = path
        arkMediaPlayer.init(
            path,
            onCompletion = {
                arkMediaPlayerSideEffect.value = ArkMediaPlayerSideEffect.StopPlaying
                finishPlaybackProgressUpdate()
            },
            onPrepared = {
                arkMediaPlayerState.value = ArkMediaPlayerState(
                    currentPos = 0,
                    duration = millisToString(arkMediaPlayer.duration().toLong()),
                    maxAmplitude = arkMediaPlayer.getMaxAmplitude()
                )
                startProgressMonitor()
            }
        )
    }

    private fun setupVisualizer() {
        if (visualizer != null) {
            visualizer?.setEnabled(true)
            return
        }
        // Attach a Visualizer to the MediaPlayer
        //Inspired from this thread: https://stackoverflow.com/a/30384717
        visualizer = Visualizer(arkMediaPlayer.getAudioSessionId()).apply {
            captureSize = Visualizer.getCaptureSizeRange()[1] // Use the max capture size
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(
                    visualizer: Visualizer?,
                    waveform: ByteArray?,
                    samplingRate: Int
                ) {
                    // Process waveform data here
                    val intensity = ((waveform?.getOrNull(0) ?: 0) + 128f) / 256
                    arkMediaPlayer.setMaxAmplitude((intensity * WaveView.MAX_AMPLITUDE).toInt())
                }

                override fun onFftDataCapture(
                    visualizer: Visualizer?,
                    fft: ByteArray?,
                    samplingRate: Int
                ) {
                    // Optionally, process FFT data here
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false)

            enabled = true
        }
    }

    fun setPath(path: String) {
        currentPlayingVoiceNotePath = path
    }

    fun onPlayOrPauseClick(path: String, pos: Int? = null, onStop: ((pos: Int) -> Unit)? = null) {
        if (currentPlayingVoiceNotePath != path) {
            currentPlayingVoiceNotePath = path
            arkMediaPlayer.init(
                path,
                onCompletion = {
                    arkMediaPlayerSideEffect.value = ArkMediaPlayerSideEffect.StopPlaying
                    onStop?.invoke(pos ?: 0)
                    finishPlaybackProgressUpdate()
                },
                onPrepared = {
                    arkMediaPlayerState.value = ArkMediaPlayerState(
                        currentPos = 0,
                        duration = millisToString(arkMediaPlayer.duration().toLong()),
                        maxAmplitude = arkMediaPlayer.getMaxAmplitude()
                    )
                }
            )
        }
        if (arkMediaPlayer.isPlaying()) {
            onPauseClick()
            return
        }
        onPlayClick()
    }

    private fun startProgressMonitor() {
        if (progressJob?.isActive == true) return
        val duration = millisToString(arkMediaPlayer.duration().toLong())

        progressJob = viewModelScope.launchPeriodicAsync(repeatMillis = 100L, repeatCondition = isPlaying()) {
            val curPosInMillis = arkMediaPlayer.currentPosition()
            val curPos = curPosInMillis / 1000

            arkMediaPlayerState.value = ArkMediaPlayerState(
                currentPos = curPos,
                duration = duration,
                maxAmplitude = arkMediaPlayer.getMaxAmplitude()
            )
        }
    }

    private fun onPlayClick() {
        arkMediaPlayer.play()
        startProgressMonitor()
        setupVisualizer()
        arkMediaPlayerSideEffect.value = ArkMediaPlayerSideEffect.StartPlaying
    }

    private fun onPauseClick() {
        arkMediaPlayer.pause()
        arkMediaPlayerSideEffect.value = ArkMediaPlayerSideEffect.PausePlaying
        visualizer?.setEnabled(false)
        progressJob?.cancel()
    }

    fun getDurationString(onSuccess: (duration: String) -> Unit) {
        if (currentPlayingVoiceNotePath.isEmpty()
            || File(currentPlayingVoiceNotePath).length() == 0L) return

        viewModelScope.launch(Dispatchers.IO) {
            val durationString = extractDuration(currentPlayingVoiceNotePath)
            withContext(Dispatchers.Main) {
                onSuccess.invoke(durationString)
            }
        }
    }

    fun isPlayerInitialized(): Boolean{
        return arkMediaPlayer.isInitialized()
    }

    fun isPlaying(): Boolean {
        return arkMediaPlayer.isPlaying()
    }

    private fun finishPlaybackProgressUpdate() {
        visualizer?.setEnabled(false)
        visualizer?.release()
        visualizer = null
        progressJob?.cancel()
    }
}