package dev.arkbuilders.arkmemo.media

import android.media.AudioAttributes
import android.media.MediaPlayer
import javax.inject.Inject

class ArkMediaPlayerImpl @Inject constructor(): ArkMediaPlayer {

    private var player: MediaPlayer? = null

    private var onCompletionHandler: () -> Unit = {}
    private var onPreparedHandler: () -> Unit = {}

    override fun init(path: String, onCompletion: () -> Unit, onPrepared: () -> Unit) {
        onCompletionHandler = onCompletion
        onPreparedHandler = onPrepared
        player = MediaPlayer().apply {
            setOnCompletionListener(this@ArkMediaPlayerImpl)
            setOnPreparedListener(this@ArkMediaPlayerImpl)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
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

    override fun seekTo(position: Int) {
        player?.seekTo(position)
    }

    override fun duration(): Int = player?.duration!!

    override fun currentPosition(): Int = player?.currentPosition!!

    override fun isPlaying(): Boolean = player?.isPlaying!!

    override fun onCompletion(player: MediaPlayer?) {
        onCompletionHandler()
    }

    override fun onPrepared(player: MediaPlayer?) {
        onPreparedHandler()
    }

    override fun onSeekComplete(player: MediaPlayer?) {}
}