package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentEditNotesBinding
import dev.arkbuilders.arkmemo.models.VoiceNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerSideEffect
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerState
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ArkMediaPlayerFragment: Fragment(R.layout.fragment_edit_notes) {

    private val activity by lazy {
        requireActivity() as MainActivity
    }

    private val binding by viewBinding(FragmentEditNotesBinding::bind)
    private val arkMediaPlayerViewModel: ArkMediaPlayerViewModel by viewModels()

    private lateinit var seekBar: SeekBar
    private lateinit var ivPlayPause: ImageView
    private lateinit var tvDuration: TextView
    private lateinit var etTitle: EditText
    private lateinit var btnSave: Button

    private lateinit var note: VoiceNote

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arkMediaPlayerViewModel.initPlayer(note.path.toString())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.initEditUI()
        initUI()
        observeViewModel()
    }

    private fun initUI() {
        val defaultTitle = getString(
            R.string.ark_memo_voice_note,
            LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        )
        var title = note.title
        val textWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                title = s?.toString() ?: defaultTitle
            }

            override fun afterTextChanged(p0: Editable?) {}

        }
        val seekBarChangeListener = object: OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, isFromUser: Boolean) {
                if (isFromUser) arkMediaPlayerViewModel.onSeekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        }
        binding.mediaPlayerViewBinding.mediaPlayerView.isVisible = true
        seekBar = binding.mediaPlayerViewBinding.seekBar
        ivPlayPause = binding.mediaPlayerViewBinding.ivPlayPause
        tvDuration = binding.mediaPlayerViewBinding.tvDuration
        etTitle = binding.noteTitle
        btnSave = binding.btnSave

        etTitle.hint = defaultTitle
        etTitle.setText(title)
        etTitle.addTextChangedListener(textWatcher)
        ivPlayPause.setOnClickListener {
            arkMediaPlayerViewModel.onPlayOrPauseClick(note.path.toString())
        }
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener)
    }

    private fun showState(state: ArkMediaPlayerState) {
        seekBar.progress = state.progress.toInt()
        tvDuration.text = state.duration
    }

    private fun handleSideEffect(effect: ArkMediaPlayerSideEffect) {
        when (effect) {
            ArkMediaPlayerSideEffect.StartPlaying -> {
                showPauseIcon()
            }
            ArkMediaPlayerSideEffect.StopPlaying -> {
                showPlayIcon()
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
            R.drawable.ic_play,
            null
        )
        ivPlayPause.setImageDrawable(playIcon)
    }

    private fun showPauseIcon() {
        val playIcon = ResourcesCompat.getDrawable(
            activity.resources,
            R.drawable.ic_pause,
            null
        )
        ivPlayPause.setImageDrawable(playIcon)
    }

    companion object {

        const val TAG = "ark-media-player-fragment"

        fun newInstance(note: VoiceNote) = ArkMediaPlayerFragment().apply {
            setNote(note)
        }
    }
}