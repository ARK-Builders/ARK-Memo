package dev.arkbuilders.arkmemo.media

import android.media.MediaPlayer

interface ArkMediaPlayer: MediaPlayer.OnCompletionListener {

    var onCompletion: () -> Unit

    fun init(path: String)

    fun play()

    fun stop()

    fun pause()

    fun seekTo(point: Int)

    fun duration(): Int

    fun currentPosition(): Int

    fun isPlaying(): Boolean
}