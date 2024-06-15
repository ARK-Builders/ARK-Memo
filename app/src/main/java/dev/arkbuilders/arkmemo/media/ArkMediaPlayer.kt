package dev.arkbuilders.arkmemo.media

import android.media.MediaPlayer

interface ArkMediaPlayer:
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnSeekCompleteListener {

    fun init(path: String, onCompletion: () -> Unit, onPrepared: () -> Unit)

    fun play()

    fun stop()

    fun pause()

    fun seekTo(position: Int)

    fun duration(): Int

    fun currentPosition(): Int

    fun isPlaying(): Boolean

    fun isInitialized(): Boolean
}