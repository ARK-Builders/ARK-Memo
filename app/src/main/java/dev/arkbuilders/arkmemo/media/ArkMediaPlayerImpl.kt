package dev.arkbuilders.arkmemo.media

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import javax.inject.Inject

class ArkMediaPlayerImpl @Inject constructor(): ArkMediaPlayer {

    private var player: MediaPlayer? = null

    private var onCompletionHandler: () -> Unit = {}
    private var onPreparedHandler: () -> Unit = {}

    private var maxAmplitude = 0

    override fun init(path: String, onCompletion: () -> Unit, onPrepared: () -> Unit) {
        if (player?.isPlaying == true) {
            player?.stop()
            onCompletionHandler()
        }

        onCompletionHandler = onCompletion
        onPreparedHandler = onPrepared

        player = MediaPlayer().apply {
            setOnCompletionListener(this@ArkMediaPlayerImpl)
            setOnPreparedListener(this@ArkMediaPlayerImpl)

            try {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(path)
                prepare()
            } catch (e: Exception) {
                Log.e("ArkMediaPlayerImpl", "init exception: ${e.message}" )
            }

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

    override fun duration(): Int = player?.duration ?: 0

    override fun currentPosition(): Int = player?.currentPosition ?: 0

    override fun isPlaying(): Boolean = player?.isPlaying ?: false

    override fun onCompletion(player: MediaPlayer?) {
        onCompletionHandler()
    }

    override fun onPrepared(player: MediaPlayer?) {
        onPreparedHandler()
    }

    override fun onSeekComplete(player: MediaPlayer?) {}

    override fun isInitialized(): Boolean {
        return player != null
    }

    override fun getAudioSessionId(): Int {
        return player?.audioSessionId ?: -1
    }

    override fun getMaxAmplitude(): Int {
        return maxAmplitude
    }

    override fun setMaxAmplitude(maxAmplitude: Int) {
        this.maxAmplitude = maxAmplitude
    }
}