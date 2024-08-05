package dev.arkbuilders.arkmemo.ui.viewmodels

import android.media.MediaMetadataRetriever
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arkmemo.media.ArkMediaPlayer
import dev.arkbuilders.arkmemo.utils.millisToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

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
            val duration = millisToString(arkMediaPlayer.duration().toLong())
            do {
                progress = (arkMediaPlayer.currentPosition().toFloat() /
                        arkMediaPlayer.duration().toFloat()) * 100
                arkMediaPlayerState.value = ArkMediaPlayerState(
                    progress = progress,
                    duration = duration
                )
            } while(arkMediaPlayer.isPlaying())
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

    fun getDurationMillis(onSuccess: (duration: Long) -> Unit) {
        if (currentPlayingVoiceNotePath.isEmpty()
            || File(currentPlayingVoiceNotePath).length() == 0L) return

        viewModelScope.launch(Dispatchers.IO) {
            val metadataRetriever = MediaMetadataRetriever()
            metadataRetriever.setDataSource(currentPlayingVoiceNotePath)
            val duration = metadataRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLong() ?: 0L
            withContext(Dispatchers.Main) {
                onSuccess.invoke(duration)
            }
        }
    }

    fun isPlayerInitialized(): Boolean{
        return arkMediaPlayer.isInitialized()
    }

    fun isPlaying(): Boolean {
        return arkMediaPlayer.isPlaying()
    }
}