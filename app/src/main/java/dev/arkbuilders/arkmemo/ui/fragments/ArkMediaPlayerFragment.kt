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
import dev.arkbuilders.arkmemo.utils.millisToString
import dev.arkbuilders.arkmemo.utils.visible
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ArkMediaPlayerFragment: BaseEditNoteFragment() {

    private val activity by lazy {
        requireActivity() as MainActivity
    }

    private val arkMediaPlayerViewModel: ArkMediaPlayerViewModel by viewModels()

    private lateinit var note: VoiceNote

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

    private fun initUI() {

        binding.toolbar.ivRightActionIcon.setOnClickListener {
            showDeleteNoteDialog(note)
        }

        val defaultTitle = getString(
            R.string.ark_memo_voice_note,
            LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        )
        binding.edtTitle.hint = defaultTitle
        binding.edtTitle.setText(note.title)

        if (File(note.path.toString()).exists()) {
            arkMediaPlayerViewModel.setPath(note.path.toString())
            binding.layoutAudioView.root.visible()
            arkMediaPlayerViewModel.getDurationMillis { duration ->
                binding.layoutAudioView.tvDuration.text = millisToString(duration)
            }

        } else {
            binding.layoutAudioView.root.gone()
        }

        binding.layoutAudioView.ivPlayAudio.setOnClickListener {
            val recordingPath = note.path.toString()
            arkMediaPlayerViewModel.initPlayer(recordingPath)
            arkMediaPlayerViewModel.onPlayOrPauseClick(recordingPath)
        }

        binding.layoutAudioRecord.root.gone()

    }

    private fun showState(state: ArkMediaPlayerState) {
        binding.layoutAudioView.tvDuration.text = state.duration
    }

    private fun handleSideEffect(effect: ArkMediaPlayerSideEffect) {
        when (effect) {
            ArkMediaPlayerSideEffect.StartPlaying -> {
                showPauseIcon()
                binding.layoutAudioView.animAudioPlaying.playAnimation()
            }
            ArkMediaPlayerSideEffect.StopPlaying -> {
                showPlayIcon()
                binding.layoutAudioView.animAudioPlaying.cancelAnimation()
            }
            ArkMediaPlayerSideEffect.PausePlaying -> {
                showPlayIcon()
                binding.layoutAudioView.animAudioPlaying.cancelAnimation()
            }
            ArkMediaPlayerSideEffect.ResumePlaying -> {
                showPauseIcon()
                binding.layoutAudioView.animAudioPlaying.playAnimation()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                arkMediaPlayerViewModel.collect(
                    stateToUI = { showState(it) },
                    handleSideEffect = { handleSideEffect(it) }
                )
            }
        }
    }

    private fun setNote(note: VoiceNote) {
        this.note = note
    }

    private fun showPlayIcon() {
        val playIcon = ResourcesCompat.getDrawable(
            activity.resources,
            R.drawable.ic_play_circle,
            null
        )
        binding.layoutAudioView.ivPlayAudio.setImageDrawable(playIcon)
    }

    private fun showPauseIcon() {
        val playIcon = ResourcesCompat.getDrawable(
            activity.resources,
            R.drawable.ic_pause_circle,
            null
        )
        binding.layoutAudioView.ivPlayAudio.setImageDrawable(playIcon)
    }

    companion object {

        const val TAG = "ark-media-player-fragment"

        fun newInstance(note: VoiceNote) = ArkMediaPlayerFragment().apply {
            setNote(note)
        }
    }
}