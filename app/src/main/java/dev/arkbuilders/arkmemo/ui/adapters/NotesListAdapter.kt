package dev.arkbuilders.arkmemo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.AdapterTextNoteBinding
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.models.VoiceNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.fragments.ArkMediaPlayerFragment
import dev.arkbuilders.arkmemo.ui.fragments.EditGraphicNotesFragment
import dev.arkbuilders.arkmemo.ui.fragments.EditTextNotesFragment
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerSideEffect
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerState
import dev.arkbuilders.arkmemo.ui.views.NotesCanvas
import dev.arkbuilders.arkmemo.utils.getAutoTitle
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.replaceFragment
import dev.arkbuilders.arkmemo.utils.visible

class NotesListAdapter(
    private val notes: List<Note>,
    private val onPlayPauseClick: (String) -> Unit,
    private val onThumbPrepare : (note: GraphicNote, holder: NotesCanvas) -> Unit
): RecyclerView.Adapter<NotesListAdapter.NoteViewHolder>() {

    private lateinit var activity: MainActivity
    private lateinit var fragmentManager: FragmentManager

    lateinit var observeItemSideEffect: () -> ArkMediaPlayerSideEffect
    lateinit var observeItemState: () -> ArkMediaPlayerState

    fun setActivity(activity: AppCompatActivity) {
        this.activity = activity as MainActivity
    }

    fun setFragmentManager(manager: FragmentManager) {
        fragmentManager = manager
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = AdapterTextNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.clipToOutline = true
        return NoteViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.title.text = note.getAutoTitle(activity)
//        holder.date.text = note.resource?.modified?.toString() ?:
//                activity.getString(R.string.ark_memo_just_now)
        if (note is TextNote) {
            holder.contentPreview.text = note.text
        }
        holder.layoutAudioView.root.isVisible = false
        if (note is VoiceNote) {
            holder.layoutAudioView.root.isVisible = true
            holder.btnPlayPause.setOnClickListener {
                onPlayPauseClick(note.path.toString())
                handleMediaPlayerSideEffect(observeItemSideEffect(), holder)
            }
        } else if (note is GraphicNote) {
            holder.canvasGraphicThumb.visible()
            onThumbPrepare(note, holder.canvasGraphicThumb)
        }

        if (note.pendingForDelete) {
            holder.tvDelete.visible()
        } else {
            holder.tvDelete.gone()
        }
    }

    override fun getItemCount() = notes.size

    private fun handleMediaPlayerSideEffect(
        effect: ArkMediaPlayerSideEffect,
        holder: NoteViewHolder
    ) {
        when (effect) {
            is ArkMediaPlayerSideEffect.StartPlaying -> {
                showPauseIcon(holder)
            }
            is ArkMediaPlayerSideEffect.PausePlaying -> {
                showPlayIcon(holder)
            }
            is ArkMediaPlayerSideEffect.StopPlaying -> {
                showPlayIcon(holder)
            }
            is ArkMediaPlayerSideEffect.ResumePlaying -> {
                showPauseIcon(holder)
            }
        }
    }

    private fun showPlayIcon(holder: NoteViewHolder) {
        val playIcon = ResourcesCompat.getDrawable(
            activity.resources,
            R.drawable.ic_play_circle,
            null
        )
        holder.btnPlayPause.setImageDrawable(playIcon)
    }

    private fun showPauseIcon(holder: NoteViewHolder) {
        val playIcon = ResourcesCompat.getDrawable(
            activity.resources,
            R.drawable.ic_pause_circle,
            null
        )
        holder.btnPlayPause.setImageDrawable(playIcon)
    }

    inner class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val binding by viewBinding {
            AdapterTextNoteBinding.bind(itemView)
        }

        val title = binding.tvTitle
        val contentPreview = binding.tvContentPreview
        val btnPlayPause = binding.layoutAudioView.ivPlayAudio
        val layoutAudioView = binding.layoutAudioView
        val canvasGraphicThumb = binding.canvasGraphicThumb
        val tvDelete = binding.tvDelete

        private val clickNoteToEditListener = View.OnClickListener {
            var tag = EditTextNotesFragment.TAG
            when (val selectedNote = notes[bindingAdapterPosition]) {
                is TextNote -> activity.fragment = EditTextNotesFragment.newInstance(selectedNote)
                is GraphicNote -> {
                    activity.fragment = EditGraphicNotesFragment.newInstance(selectedNote)
                    tag = EditGraphicNotesFragment.TAG
                }
                is VoiceNote -> {
                    activity.fragment = ArkMediaPlayerFragment.newInstance(selectedNote)
                    tag = ArkMediaPlayerFragment.TAG
                }
            }
            activity.replaceFragment(activity.fragment, tag)
        }

        init {
            binding.root.setOnClickListener(clickNoteToEditListener)
        }
    }
}