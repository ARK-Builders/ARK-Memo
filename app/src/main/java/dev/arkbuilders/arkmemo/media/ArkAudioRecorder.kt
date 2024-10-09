package dev.arkbuilders.arkmemo.media

import java.nio.file.Path

interface ArkAudioRecorder {

    fun init()

    fun start()

    fun pause()

    fun stop()

    fun resume()

    fun reset()

    fun maxAmplitude(): Int

    fun getRecording(): Path

    suspend fun deleteTempFile(): Boolean
}
