package dev.arkbuilders.arkmemo.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.models.VoiceNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.dialogs.CommonActionDialog
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerSideEffect
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerState
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.RecorderSideEffect
import dev.arkbuilders.arkmemo.ui.viewmodels.RecorderState
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkRecorderViewModel
import dev.arkbuilders.arkmemo.ui.views.toast
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.millisToString
import dev.arkbuilders.arkmemo.utils.observeSaveResult
import dev.arkbuilders.arkmemo.utils.visible
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path

@AndroidEntryPoint
class ArkRecorderFragment: BaseEditNoteFragment() {

    private val activity by lazy { requireActivity() as MainActivity }

    private var shouldRecord = false
    private val audioRecordingPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            shouldRecord = isGranted
            if (!shouldRecord) activity.onBackPressedDispatcher.onBackPressed()
        }

    private val arkRecorderViewModel: ArkRecorderViewModel by viewModels()
    private val mediaPlayViewModel: ArkMediaPlayerViewModel by viewModels()

    private lateinit var ivRecord: ImageView
    private lateinit var ivPauseResume: ImageView
    private lateinit var tvDuration: TextView

    private var note: VoiceNote? = null

    private val defaultNoteTitle by lazy { getString(
        R.string.ark_memo_voice_note,
        LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    ) }

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
                        stateToUI = {
                            showState(it)
                        },
                        handleSideEffect = { handleSideEffect(it) }
                    )
                }
            }
            observeSaveResult(notesViewModel.getSaveNoteResultLiveData())
        } else audioRecordingPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initExistingNoteUI()
        observeViewModel()
        observeKeyboardVisibility()
    }

    private fun observeKeyboardVisibility() {
        val view = activity.window?.decorView ?: return
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val showingKeyboard = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (showingKeyboard) {
                binding.layoutAudioRecord.groupRecordingViews.gone()
                binding.layoutAudioRecord.groupSideRecordButtons.gone()
                binding.layoutAudioRecord.tvRecordGuide.gone()
            } else {
                binding.layoutAudioRecord.groupRecordingViews.visible()

                if (arkRecorderViewModel.isRecording()) {
                    binding.layoutAudioRecord.groupSideRecordButtons.visible()
                    binding.layoutAudioRecord.tvRecordGuide.gone()
                } else {
                    binding.layoutAudioRecord.groupSideRecordButtons.gone()
                    binding.layoutAudioRecord.tvRecordGuide.visible()
                }
            }
            insets
        }
    }

    private fun initUI() {
        var title = ""
        val etTitle = binding.edtTitle
        val etTitleWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                title = s?.toString() ?: ""
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.toolbar.ivRightActionIcon.setImageResource(R.drawable.ic_delete_note)

        binding.layoutAudioRecord.root.visible()
        ivRecord = binding.layoutAudioRecord.ivRecord
        ivPauseResume = binding.layoutAudioRecord.ivPauseResume
        tvDuration = binding.layoutAudioRecord.tvDuration

        ivPauseResume.isEnabled = false
        enableSaveText(false)

        etTitle.hint = defaultNoteTitle
        etTitle.addTextChangedListener(etTitleWatcher)

        ivRecord.setOnClickListener {
            if (!arkRecorderViewModel.isRecording()
                && (arkRecorderViewModel.isRecordExisting() ||
                        File(getCurrentRecordingPath()).length() > 0L)) {

                CommonActionDialog(title = R.string.dialog_replace_recording_title,
                    message = R.string.dialog_replace_recording_message,
                    positiveText = R.string.dialog_replace_recording_positive_text,
                    negativeText = R.string.discard,
                    onPositiveClick = {
                        binding.layoutAudioView.root.gone()
                        arkRecorderViewModel.onStartStopClick() },

                    onNegativeClicked = {  }
                ).show(parentFragmentManager, CommonActionDialog.TAG)
            } else {
                arkRecorderViewModel.onStartStopClick()
            }
        }

        ivPauseResume.setOnClickListener {
            arkRecorderViewModel.onPauseResumeClick()
        }

        binding.layoutAudioRecord.ivStartOver.setOnClickListener {
            arkRecorderViewModel.onStartOverClick()
        }

        binding.layoutAudioView.ivPlayAudio.setOnClickListener {
            val recordingPath = getCurrentRecordingPath()
            if (recordingPath.isEmpty()) {
                toast(requireContext(), getString(R.string.toast_invalid_recording))
            } else {
                if (!mediaPlayViewModel.isPlayerInitialized()) {
                    mediaPlayViewModel.initPlayer(recordingPath)
                }

                mediaPlayViewModel.onPlayOrPauseClick(recordingPath)
            }
        }

        binding.layoutAudioRecord.tvSaveRecording.setOnClickListener {
            saveNote()
        }

        binding.toolbar.ivRightActionIcon.setOnClickListener {
            val note = VoiceNote(
                title = title.ifEmpty { defaultNoteTitle },
                path = arkRecorderViewModel.getRecordingPath()
            )
            CommonActionDialog(title = R.string.delete_note,
                message = R.string.ark_memo_delete_warn,
                positiveText = R.string.action_delete,
                negativeText = R.string.ark_memo_cancel,
                isAlert = true,
                onPositiveClick = {
                notesViewModel.onDeleteConfirmed(note)
                toast(requireContext(), getString(R.string.note_deleted))
                activity.onBackPressedDispatcher.onBackPressed()
            }, onNegativeClicked = {
            }).show(parentFragmentManager, CommonActionDialog.TAG)
        }

        note ?: binding.toolbar.ivRightActionIcon.gone()
    }

    private fun handleSideEffect(effect: RecorderSideEffect) {
        when (effect) {
            RecorderSideEffect.StartRecording -> {
                val stopIcon = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_record_ongoing,
                    null
                )
                ivRecord.setImageDrawable(stopIcon)
                ivRecord.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(activity, R.color.warning_50)
                )
                ivPauseResume.isEnabled = true
                enableSaveText(false)
                binding.layoutAudioRecord.animRecording.playAnimation()
                binding.layoutAudioRecord.animRecording.visible()
                binding.layoutAudioRecord.animSoundWave.visible()
                binding.layoutAudioRecord.animSoundWave.playAnimation()
                binding.layoutAudioRecord.ivPauseResume.visible()
                binding.layoutAudioRecord.ivStartOver.visible()
                binding.layoutAudioView.root.gone()
                binding.layoutAudioRecord.tvRecordGuide.gone()
            }
            is RecorderSideEffect.StopRecording -> {
                val recordIcon = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_record_big,
                    null
                )
                ivRecord.setImageDrawable(recordIcon)
                ivRecord.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(activity, R.color.warning)
                )
                ivPauseResume.isEnabled = false
                enableSaveText(true)
                showPauseIcon()
                binding.layoutAudioRecord.animRecording.gone()
                binding.layoutAudioRecord.animSoundWave.gone()
                binding.layoutAudioRecord.ivPauseResume.gone()
                binding.layoutAudioRecord.ivStartOver.gone()
                binding.layoutAudioView.root.visible()
                binding.layoutAudioView.tvDuration.text = effect.duration
                binding.layoutAudioRecord.tvRecordGuide.visible()
                binding.layoutAudioRecord.tvRecordGuide.text =
                    getString(R.string.audio_record_guide_text_replace)
                binding.layoutAudioRecord.tvDuration.setText(R.string.ark_memo_duration_default)
            }
            RecorderSideEffect.PauseRecording -> {
                val resumeIcon = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_play,
                    null
                )
                ivPauseResume.setImageDrawable(resumeIcon)
                pauseOrResumeRecordingAnimation(false)
            }
            RecorderSideEffect.ResumeRecording -> {
                showPauseIcon()
                pauseOrResumeRecordingAnimation(true)
            }
        }
    }

    private fun handlePlaySideEffect(effect: ArkMediaPlayerSideEffect) {
        when (effect) {
            ArkMediaPlayerSideEffect.StartPlaying -> {
                binding.layoutAudioView.ivPlayAudio.setImageResource(R.drawable.ic_pause_circle)
                binding.layoutAudioView.animAudioPlaying.playAnimation()
            }
            ArkMediaPlayerSideEffect.StopPlaying -> {
                binding.layoutAudioView.ivPlayAudio.setImageResource(R.drawable.ic_play_circle)
                binding.layoutAudioView.animAudioPlaying.cancelAnimation()
            }
            ArkMediaPlayerSideEffect.PausePlaying -> {
                binding.layoutAudioView.ivPlayAudio.setImageResource(R.drawable.ic_play_circle)
                binding.layoutAudioView.animAudioPlaying.cancelAnimation()
            }
            ArkMediaPlayerSideEffect.ResumePlaying -> {
                binding.layoutAudioView.ivPlayAudio.setImageResource(R.drawable.ic_pause_circle)
                binding.layoutAudioView.animAudioPlaying.playAnimation()
            }
        }
    }

    private fun showState(state: RecorderState) {
        tvDuration.text = state.progress
    }

    private fun showPlayState(state: ArkMediaPlayerState) {
        binding.layoutAudioView.tvDuration.text = state.duration
    }

    private fun showPauseIcon() {
        val pauseIcon = ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_pause,
            null
        )
        ivPauseResume.setImageDrawable(pauseIcon)
    }

    override fun createNewNote(): Note {
        return VoiceNote(
            title = binding.edtTitle.text.toString().ifEmpty { defaultNoteTitle },
            path = Path(getCurrentRecordingPath())
        )
    }

    override fun getCurrentNote(): Note {
        return note ?: createNewNote()
    }

    override fun isContentChanged(): Boolean {
        return if (note != null) {
            val originalTitle = note?.title
            val originalRecordingSize = note?.path?.toFile()?.length() ?: 0
            val currentRecordingSize = arkRecorderViewModel.getRecordingPath().toFile().length()

            (!originalTitle.equals(binding.edtTitle.text.toString())
                    || (originalRecordingSize != currentRecordingSize && currentRecordingSize > 0))
        } else {
            (binding.edtTitle.text.toString().isNotEmpty()
                    || arkRecorderViewModel.isRecordExisting())
        }
    }

    override fun isContentEmpty(): Boolean {
        return (!arkRecorderViewModel.isRecordExisting()
                && ((note?.path?.toFile()?.length() ?: 0L) == 0L))
    }

    private fun saveNote() {
        notesViewModel.onSaveClick(createNewNote(), parentNote = note) { show ->
            activity.showProgressBar(show)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                mediaPlayViewModel.collect(
                    stateToUI = { showPlayState(it) },
                    handleSideEffect = { handlePlaySideEffect(it) }
                )
            }
        }
    }

    private fun pauseOrResumeRecordingAnimation(resume: Boolean) {
        if (resume) {
            binding.layoutAudioRecord.animSoundWave.resumeAnimation()
            binding.layoutAudioRecord.animRecording.resumeAnimation()
        } else {
            binding.layoutAudioRecord.animSoundWave.pauseAnimation()
            binding.layoutAudioRecord.animRecording.pauseAnimation()
        }
    }

    private fun enableSaveText(enabled: Boolean) {
        binding.layoutAudioRecord.tvSaveRecording.isEnabled = enabled
        if (enabled) {
            binding.layoutAudioRecord.tvSaveRecording.alpha = 1f
        } else {
            binding.layoutAudioRecord.tvSaveRecording.alpha = 0.4f
        }
    }

    private fun setNote(note: VoiceNote) {
        this.note = note
    }

    private fun initExistingNoteUI() {
        val notePath = note?.path.toString()
        if (File(notePath).exists() && (note?.path?.toFile()?.length() ?: 0L) > 0L) {
            mediaPlayViewModel.setPath(notePath)
            binding.layoutAudioView.root.visible()
            binding.layoutAudioRecord.tvRecordGuide.text =
                getString(R.string.audio_record_guide_text_replace)
            mediaPlayViewModel.getDurationMillis { duration ->
                binding.layoutAudioView.tvDuration.text = millisToString(duration)
            }

        } else {
            binding.layoutAudioView.root.gone()
        }
        binding.edtTitle.setText(note?.title)
        binding.edtTitle.addTextChangedListener {
            enableSaveText(isContentChanged() && !isContentEmpty())
        }
    }

    private fun getCurrentRecordingPath(): String {

        val tempRecordingPath = arkRecorderViewModel.getRecordingPath()
        return if (tempRecordingPath.toFile().length() > 0) {
            tempRecordingPath.toString()
        } else if ((note?.path?.toFile()?.length() ?: 0) > 0) {
            note?.path.toString()
        } else {
            tempRecordingPath.toString()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayViewModel.isPlaying()) {
            mediaPlayViewModel.onPlayOrPauseClick(getCurrentRecordingPath())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val view = activity.window?.decorView ?: return
        ViewCompat.setOnApplyWindowInsetsListener(view, null)
    }

    companion object {

        const val TAG = "voice-notes-fragment"

        fun newInstance(note: VoiceNote? = null) = ArkRecorderFragment().apply {
            note?.let { this.setNote(note) }
        }
    }
}