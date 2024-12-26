package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.models.VoiceNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerSideEffect
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerState
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerViewModel
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ArkMediaPlayerFragment : BaseEditNoteFragment() {
    private val activity by lazy {
        requireActivity() as MainActivity
    }

    private val arkMediaPlayerViewModel: ArkMediaPlayerViewModel by viewModels()

    private lateinit var note: VoiceNote

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        activity.initEditUI()
        initUI()
        observeViewModel()
    }

    override fun createNewNote(): Note {
        return note
    }

    override fun getCurrentNote(): Note {
        return note
    }

    override fun isContentChanged(): Boolean {
        return note.title != binding.edtTitle.text.toString()
    }

    override fun isContentEmpty(): Boolean {
        return false
    }

    override fun onViewRestoredWithNote(note: Note) {
    }

    private fun initUI() {
        binding.toolbar.ivRightActionIcon.setOnClickListener {
            showDeleteNoteDialog(note)
        }

        val defaultTitle =
            getString(
                R.string.ark_memo_voice_note,
                LocalDate.now().format(DateTimeFormatter.ISO_DATE),
            )
        binding.edtTitle.hint = defaultTitle
        binding.edtTitle.setText(note.title)

        if (File(note.path.toString()).exists()) {
            arkMediaPlayerViewModel.setPath(note.path.toString())
            binding.layoutAudioView.root.visible()
            binding.layoutAudioRecord.root.visible()
            binding.layoutAudioRecord.tvRecordGuide.text =
                getString(R.string.audio_record_guide_text_replace)
            arkMediaPlayerViewModel.getDurationString { duration ->
                binding.layoutAudioView.tvDuration.text = duration
            }
        } else {
            binding.layoutAudioView.root.gone()
            binding.layoutAudioRecord.root.gone()
        }

        binding.layoutAudioView.ivPlayAudio.setOnClickListener {
            val recordingPath = note.path.toString()
            arkMediaPlayerViewModel.initPlayer(recordingPath)
            arkMediaPlayerViewModel.onPlayOrPauseClick(recordingPath)
            binding.layoutAudioView.tvPlayingPosition.visible()
        }
    }

    private fun showState(state: ArkMediaPlayerState) {
        binding.layoutAudioView.tvPlayingPosition.text = state.currentPos.toString()
    }

    private fun handleSideEffect(effect: ArkMediaPlayerSideEffect) {
        when (effect) {
            ArkMediaPlayerSideEffect.StartPlaying -> {
                showPauseIcon()
            }
            ArkMediaPlayerSideEffect.StopPlaying -> {
                showPlayIcon()
                binding.layoutAudioView.animAudioPlaying.resetWave()
            }
            ArkMediaPlayerSideEffect.PausePlaying -> {
                showPlayIcon()
            }
            ArkMediaPlayerSideEffect.ResumePlaying -> {
                showPauseIcon()
            }
        }
    }

    private fun observeViewModel() {
        observePlayerState()
        observePlayerSideEffect()
    }

    private fun observePlayerState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                arkMediaPlayerViewModel.playerState.collectLatest { state ->
                    state ?: return@collectLatest
                    showState(state)
                }
            }
        }
    }

    private fun observePlayerSideEffect() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                arkMediaPlayerViewModel.playerSideEffect.collectLatest { sideEffect ->
                    sideEffect ?: return@collectLatest
                    handleSideEffect(sideEffect)
                }
            }
        }
    }

    private fun setNote(note: VoiceNote) {
        this.note = note
    }

    private fun showPlayIcon() {
        val playIcon =
            ResourcesCompat.getDrawable(
                activity.resources,
                R.drawable.ic_play_circle,
                null,
            )
        binding.layoutAudioView.ivPlayAudio.setImageDrawable(playIcon)
    }

    private fun showPauseIcon() {
        val playIcon =
            ResourcesCompat.getDrawable(
                activity.resources,
                R.drawable.ic_pause_circle,
                null,
            )
        binding.layoutAudioView.ivPlayAudio.setImageDrawable(playIcon)
    }

    companion object {
        const val TAG = "ark-media-player-fragment"

        fun newInstance(note: VoiceNote) =
            ArkMediaPlayerFragment().apply {
                setNote(note)
            }
    }
}
