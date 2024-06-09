package dev.arkbuilders.arkmemo.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.createTempFile

class ArkAudioRecorderImpl @Inject constructor(
    @ApplicationContext private val context: Context
): ArkAudioRecorder {

    private val TAG = "ArkAudioRecorderImpl"

    private var recorder: MediaRecorder? = null
    private val tempFile = createTempFile().toFile()

    override fun init() {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(context)
        else MediaRecorder()
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(tempFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
        }
    }

    override fun start() {
        recorder?.start()
    }

    override fun pause() {
        recorder?.pause()
    }

    override fun resume() {
        recorder?.resume()
    }

    override fun reset() {
        recorder?.reset()
    }

    override fun stop() {
        recorder?.let {
            try {
                it.stop()
            } catch (e: RuntimeException) {
                Log.e(TAG, "stop exception: " + e.message)
            }

            it.release()
        }
        recorder = null
    }

    override fun maxAmplitude(): Int {
        try {
            return recorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            return 0
        }
    }

    override fun getRecording(): Path = tempFile.toPath()
}