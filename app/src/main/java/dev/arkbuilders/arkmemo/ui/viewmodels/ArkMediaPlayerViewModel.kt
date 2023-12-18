package dev.arkbuilders.arkmemo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arkmemo.media.ArkMediaPlayer
import dev.arkbuilders.arkmemo.utils.millisToString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timer

sealed class ArkMediaPlayerSideEffect {
    object StartPlaying: ArkMediaPlayerSideEffect()

    object PausePlaying: ArkMediaPlayerSideEffect()

    object ResumePlaying: ArkMediaPlayerSideEffect()

    object StopPlaying: ArkMediaPlayerSideEffect()
}

data class ArkMediaPlayerState(
    val progress: Float,
    val duration: String
)

@HiltViewModel
class ArkMediaPlayerViewModel @Inject constructor(
    private val arkMediaPlayer: ArkMediaPlayer
): ViewModel() {

    private var currentPlayingVoiceNotePath: String = ""
    private val arkMediaPlayerSideEffect = MutableStateFlow<ArkMediaPlayerSideEffect?>(null)
    private val arkMediaPlayerState = MutableStateFlow<ArkMediaPlayerState?>(null)

    private var timer: Timer? = null

    fun onPlayOrPauseClick(path: String) {
        if (currentPlayingVoiceNotePath != path) {
            currentPlayingVoiceNotePath = path
            arkMediaPlayer.init(path)
        }
        if (arkMediaPlayer.isPlaying()) {
            onPauseClick()
            return
        }
        onPlayClick()
    }

    fun onSeekTo(progress: Int) {
        val point = (progress / 100) * arkMediaPlayer.duration()
        arkMediaPlayer.seekTo(point)
    }

    fun collect(
        stateToUI: (ArkMediaPlayerState) -> Unit,
        handleSideEffect: (ArkMediaPlayerSideEffect) -> Unit
    ) {
        viewModelScope.launch {
            arkMediaPlayerState.collectLatest {
                it?.let {
                    stateToUI(it)
                }
            }
        }
        viewModelScope.launch {
            arkMediaPlayerSideEffect.collectLatest {
                it?.let {
                    handleSideEffect(it)
                }
            }
        }
    }

    private fun startTimer() {
        timer = timer(initialDelay = 0L, period = arkMediaPlayer.duration().toLong()) {
            arkMediaPlayerState.value = ArkMediaPlayerState(
                progress = arkMediaPlayer.currentPosition().toFloat() /
                        arkMediaPlayer.duration().toFloat(),
                duration = millisToString(arkMediaPlayer.duration().toLong())
            )
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun onPlayClick() {
        arkMediaPlayer.play()
        arkMediaPlayer.onCompletion = {
            stopTimer()
            arkMediaPlayerSideEffect.value = ArkMediaPlayerSideEffect.StopPlaying
        }
        startTimer()
        arkMediaPlayerSideEffect.value = ArkMediaPlayerSideEffect.StartPlaying
    }

    private fun onPauseClick() {
        arkMediaPlayer.pause()
        arkMediaPlayerSideEffect.value = ArkMediaPlayerSideEffect.PausePlaying
    }
}