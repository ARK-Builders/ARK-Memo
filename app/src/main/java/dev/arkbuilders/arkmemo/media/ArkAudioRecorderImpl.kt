package dev.arkbuilders.arkmemo.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.arkbuilders.logging.ALog
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.createTempFile

class ArkAudioRecorderImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : ArkAudioRecorder {
        private val tag = "ArkAudioRecorderImpl"

        private var recorder: MediaRecorder? = null
        private val tempFile = createTempFile().toFile()

        override fun init() {
            recorder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    MediaRecorder()
                }
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(tempFile)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
            }
        }

        override fun start() {
            ALog.d(tag, "start")
            recorder?.start()
        }

        override fun pause() {
            ALog.d(tag, "pause")
            recorder?.pause()
        }

        override fun resume() {
            ALog.d(tag, "resume")
            recorder?.resume()
        }

        override fun reset() {
            ALog.d(tag, "reset")
            recorder?.reset()
        }

        override fun stop() {
            recorder?.let {
                try {
                    it.stop()
                } catch (e: RuntimeException) {
                    ALog.e(tag, "stop exception: " + e.message)
                }

                it.release()
            }
            recorder = null
        }

        override fun maxAmplitude(): Int {
            return try {
                recorder?.maxAmplitude ?: 0
            } catch (e: Exception) {
                ALog.e(tag, "maxAmplitude exception: $e")
                0
            }
        }

        override fun getRecording(): Path = tempFile.toPath()

        override suspend fun deleteTempFile(): Boolean {
            return try {
                ALog.d(tag, "deleteTempFile")
                tempFile.delete()
            } catch (e: Exception) {
                ALog.e(tag, "deleteTempFile exception: " + e.message)
                false
            }
        }
    }
