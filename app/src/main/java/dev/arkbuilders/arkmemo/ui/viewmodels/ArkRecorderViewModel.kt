package dev.arkbuilders.arkmemo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arkmemo.media.ArkAudioRecorder
import dev.arkbuilders.arkmemo.utils.millisToString
import dev.arkbuilders.arkmemo.utils.tenthSecondsToString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Path
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
import kotlin.concurrent.timer

sealed class RecorderSideEffect {
    data object StartRecording: RecorderSideEffect()

    data class StopRecording(val duration: String) : RecorderSideEffect()

    data object PauseRecording: RecorderSideEffect()

    data object ResumeRecording: RecorderSideEffect()
}

data class RecorderState(
    val maxAmplitude: Int,
    val progress: String
)

@HiltViewModel
class ArkRecorderViewModel @Inject constructor(
    private val arkAudioRecorder: ArkAudioRecorder
): ViewModel() {

    private val recorderSideEffect = MutableStateFlow<RecorderSideEffect?>(null)
    private val recorderState = MutableStateFlow<RecorderState?>(null)
    private val isRecording = MutableStateFlow(false)
    private val isPaused = MutableStateFlow(false)

    // Duration is in milliseconds
    private var duration = 0L
    private val RECORD_SAMPLING_INTERVAL = 100L //millisecond

    private var timer: Timer? = null
    private var recordTimerTask: TimerTask? = null

    fun onStartStopClick() {
        if (isRecording.value) {
            onStopRecordingClick()
        } else {
            onStartRecordingClick()
        }
    }

    fun onPauseResumeClick() {
        if (isPaused.value) {
            onResumeRecordingClick()
        } else {
            onPauseRecordingClick()
        }
    }

    fun onStartOverClick() {
        onStartOverRecordingClick()
    }

    fun collect(
        stateToUI: (RecorderState) -> Unit,
        handleSideEffect:(RecorderSideEffect) -> Unit
    ) {
        viewModelScope.launch {
            recorderState.collect {
                it?.let {
                    stateToUI(it)
                }
            }
        }
        viewModelScope.launch {
            recorderSideEffect.collectLatest {
                it?.let {
                    handleSideEffect(it)
                }
            }
        }
    }

    fun getRecordingPath(): Path {
        return arkAudioRecorder.getRecording()
    }

    fun isRecordExisting(): Boolean {
        val recordFile = File(getRecordingPath().toUri())
        return !isRecording.value && recordFile.exists() && recordFile.length() > 0
    }

    private fun onStartRecordingClick() {
        viewModelScope.launch {
            arkAudioRecorder.init()
            arkAudioRecorder.start()
            isRecording.value = true
            startTimer()
            recorderSideEffect.value = RecorderSideEffect.StartRecording
        }
    }

    private fun onStopRecordingClick() {
        viewModelScope.launch {
            arkAudioRecorder.stop()
            isRecording.value = false
            if (isPaused.value) isPaused.value = false
            val lastDuration = duration
            duration = 0
            stopTimer()
            recorderSideEffect.value = RecorderSideEffect.StopRecording(
                duration = millisToString(lastDuration * RECORD_SAMPLING_INTERVAL)
            )
        }
    }

    private fun onStartOverRecordingClick() {
        viewModelScope.launch {
            arkAudioRecorder.stop()
            duration = 0
            stopTimer()

            arkAudioRecorder.init()
            arkAudioRecorder.start()
            startTimer()
        }
    }

    private fun onPauseRecordingClick() {
        viewModelScope.launch {
            if (isRecording.value) {
                isPaused.value = true
                arkAudioRecorder.pause()
                stopTimer()
                recorderSideEffect.value = RecorderSideEffect.PauseRecording
            }
        }
    }

    private fun onResumeRecordingClick() {
        viewModelScope.launch {
            if (isRecording.value) {
                arkAudioRecorder.resume()
                isPaused.value = false
                startTimer(isResumed = true)
                recorderSideEffect.value = RecorderSideEffect.ResumeRecording
            }
        }
    }

    private fun startTimer(isResumed: Boolean = false) {
        viewModelScope.launch {
            if (isResumed) {
                recordTimerTask?.cancel()
            }
            timer = timer(initialDelay = 0L, period = RECORD_SAMPLING_INTERVAL) {
                recordTimerTask = this
                if (isRecording.value && !isPaused.value) {
                    duration += 1
                    recorderState.value = RecorderState(
                        arkAudioRecorder.maxAmplitude(),
                        tenthSecondsToString(duration)
                    )
                }
            }
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        recordTimerTask?.cancel()
        timer = null
    }

    fun isRecording(): Boolean {
        return isRecording.value
    }
}
