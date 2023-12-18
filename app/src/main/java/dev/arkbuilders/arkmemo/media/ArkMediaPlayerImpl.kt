package dev.arkbuilders.arkmemo.media

import android.media.MediaPlayer
import javax.inject.Inject

class ArkMediaPlayerImpl @Inject constructor(): ArkMediaPlayer {

    private var player: MediaPlayer? = null

    override var onCompletion: () -> Unit = {}

    override fun init(path: String) {
        player = MediaPlayer().apply {
            setDataSource(path)
            prepare()
        }
    }

    override fun play() {
        player?.start()
    }

    override fun stop() {
        player?.let {
            it.stop()
            it.release()
        }
        player = null
    }

    override fun pause() {
        player?.pause()
    }

    override fun seekTo(point: Int) {
        player?.seekTo(point)
    }

    override fun duration(): Int = player?.duration!!

    override fun currentPosition(): Int = player?.currentPosition!!

    override fun isPlaying(): Boolean = player?.isPlaying!!

    override fun onCompletion(p0: MediaPlayer?) {
        onCompletion()
    }
}