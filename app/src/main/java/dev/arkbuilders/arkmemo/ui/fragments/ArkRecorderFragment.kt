package dev.arkbuilders.arkmemo.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentEditNotesBinding
import dev.arkbuilders.arkmemo.models.VoiceNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkRecorderViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.RecorderSideEffect
import dev.arkbuilders.arkmemo.ui.viewmodels.RecorderState
import dev.arkbuilders.arkmemo.ui.views.WaveView
import dev.arkbuilders.arkmemo.utils.observeSaveResult
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ArkRecorderFragment : Fragment(R.layout.fragment_edit_notes) {
    private val activity by lazy { requireActivity() as MainActivity }
    private val binding by viewBinding(FragmentEditNotesBinding::bind)

    private var shouldRecord = false
    private val audioRecordingPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            shouldRecord = isGranted
            if (!shouldRecord) activity.onBackPressedDispatcher.onBackPressed()
        }

    private val notesViewModel: NotesViewModel by activityViewModels()
    private val arkRecorderViewModel: ArkRecorderViewModel by viewModels()

    private lateinit var ivRecord: ImageView
    private lateinit var ivPauseResume: ImageView
    private lateinit var tvDuration: TextView
    private lateinit var btnSave: ExtendedFloatingActionButton
    private lateinit var waveView: WaveView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shouldRecord = context?.let {
            it.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        } ?: false
        if (shouldRecord) {
            notesViewModel.init {}
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    arkRecorderViewModel.collect(
                        stateToUI = { showState(it) },
                        handleSideEffect = { handleSideEffect(it) },
                    )
                }
            }
            observeSaveResult(notesViewModel.getSaveNoteResultLiveData())
        } else {
            audioRecordingPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        val defaultTitle =
            getString(
                R.string.ark_memo_voice_note,
                LocalDate.now().format(DateTimeFormatter.ISO_DATE),
            )
        var title = ""
        val etTitle = binding.noteTitle
        val etTitleWatcher =
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    title = s?.toString() ?: ""
                }

                override fun afterTextChanged(s: Editable?) {}
            }

        binding.recorderViewBinding.recorderView.isVisible = true
        btnSave = binding.btnSave
        ivRecord = binding.recorderViewBinding.ivRecord
        ivPauseResume = binding.recorderViewBinding.ivPauseResume
        tvDuration = binding.recorderViewBinding.tvDuration
        waveView = binding.recorderViewBinding.waveView

        ivPauseResume.isEnabled = false
        btnSave.isEnabled = false

        etTitle.hint = defaultTitle
        etTitle.addTextChangedListener(etTitleWatcher)

        ivRecord.setOnClickListener {
            arkRecorderViewModel.onStartStopClick()
        }

        ivPauseResume.setOnClickListener {
            arkRecorderViewModel.onPauseResumeClick()
        }

        btnSave.setOnClickListener {
            val note =
                VoiceNote(
                    title = title.ifEmpty { defaultTitle },
                    path = arkRecorderViewModel.getRecordingPath(),
                )
            notesViewModel.onSaveClick(note) { show ->
                activity.showProgressBar(show)
            }
        }
    }

    private fun handleSideEffect(effect: RecorderSideEffect) {
        when (effect) {
            RecorderSideEffect.StartRecording -> {
                val stopIcon =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_stop,
                        null,
                    )
                waveView.resetWave()
                ivRecord.setImageDrawable(stopIcon)
                ivPauseResume.isEnabled = true
                btnSave.isEnabled = false
            }
            RecorderSideEffect.StopRecording -> {
                val recordIcon =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_record,
                        null,
                    )
                ivRecord.setImageDrawable(recordIcon)
                ivPauseResume.isEnabled = false
                btnSave.isEnabled = true
                showPauseIcon()
            }
            RecorderSideEffect.PauseRecording -> {
                val resumeIcon =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_play,
                        null,
                    )
                ivPauseResume.setImageDrawable(resumeIcon)
            }
            RecorderSideEffect.ResumeRecording -> {
                showPauseIcon()
            }
        }
    }

    private fun showState(state: RecorderState) {
        tvDuration.text = state.progress
        waveView.invalidateWave(state.maxAmplitude)
    }

    private fun showPauseIcon() {
        val pauseIcon =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_pause,
                null,
            )
        ivPauseResume.setImageDrawable(pauseIcon)
    }

    companion object {
        const val TAG = "voice-notes-fragment"

        fun newInstance() = ArkRecorderFragment()
    }
}
