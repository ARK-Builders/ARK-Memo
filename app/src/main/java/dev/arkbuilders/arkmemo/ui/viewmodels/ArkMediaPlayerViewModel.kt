package dev.arkbuilders.arkmemo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arkmemo.media.ArkMediaPlayer
import dev.arkbuilders.arkmemo.utils.millisToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ArkMediaPlayerSideEffect {
    object StartPlaying : ArkMediaPlayerSideEffect()

    object PausePlaying : ArkMediaPlayerSideEffect()

    object ResumePlaying : ArkMediaPlayerSideEffect()

    object StopPlaying : ArkMediaPlayerSideEffect()
}

data class ArkMediaPlayerState(
    val progress: Float,
    val duration: String
)

@HiltViewModel
class ArkMediaPlayerViewModel @Inject constructor(
    private val arkMediaPlayer: ArkMediaPlayer
) : ViewModel() {

    private var currentPlayingVoiceNotePath: String = ""
    private val arkMediaPlayerSideEffect = MutableStateFlow<ArkMediaPlayerSideEffect?>(null)
    private val arkMediaPlayerState = MutableStateFlow<ArkMediaPlayerState?>(null)

    fun initPlayer(path: String) {
        currentPlayingVoiceNotePath = path
        arkMediaPlayer.init(
            path,
            onCompletion = {
                arkMediaPlayerSideEffect.value = ArkMediaPlayerSideEffect.StopPlaying
            },
            onPrepared = {
                arkMediaPlayerState.value = ArkMediaPlayerState(
                    progress = 0f,
                    duration = millisToString(arkMediaPlayer.duration().toLong())
                )
            }
        )
    }

    fun onPlayOrPauseClick(path: String) {
        if (currentPlayingVoiceNotePath != path) {
            currentPlayingVoiceNotePath = path
            arkMediaPlayer.init(
                path,
                onCompletion = {
                    arkMediaPlayerSideEffect.value = ArkMediaPlayerSideEffect.StopPlaying
                },
                onPrepared = {
                    arkMediaPlayerState.value = ArkMediaPlayerState(
                        progress = 0f,
                        duration = millisToString(arkMediaPlayer.duration().toLong())
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

    fun onSeekTo(position: Int) {
        val pos = (position.toFloat() / 100f) * arkMediaPlayer.duration()
        arkMediaPlayer.seekTo(pos.toInt())
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

    private fun startProgressMonitor() {
        viewModelScope.launch(Dispatchers.Default) {
            var progress: Float
            do {
                progress = (
                    arkMediaPlayer.currentPosition().toFloat() /
                        arkMediaPlayer.duration().toFloat()
                    ) * 100
                arkMediaPlayerState.value = ArkMediaPlayerState(
                    progress = progress,
                    duration = millisToString(arkMediaPlayer.duration().toLong())
                )
            } while (arkMediaPlayer.isPlaying())
        }
    }

    private fun onPlayClick() {
        arkMediaPlayer.play()
        startProgressMonitor()
        arkMediaPlayerSideEffect.value = ArkMediaPlayerSideEffect.StartPlaying
    }

    private fun onPauseClick() {
        arkMediaPlayer.pause()
        arkMediaPlayerSideEffect.value = ArkMediaPlayerSideEffect.PausePlaying
    }
}
