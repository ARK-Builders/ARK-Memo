package dev.arkbuilders.arkmemo.ui.adapters

import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.AdapterTextNoteBinding
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.models.VoiceNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.fragments.ArkRecorderFragment
import dev.arkbuilders.arkmemo.ui.fragments.EditGraphicNotesFragment
import dev.arkbuilders.arkmemo.ui.fragments.EditTextNotesFragment
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerSideEffect
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerState
import dev.arkbuilders.arkmemo.utils.getAutoTitle
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.highlightWord
import dev.arkbuilders.arkmemo.utils.millisToString
import dev.arkbuilders.arkmemo.utils.replaceFragment
import dev.arkbuilders.arkmemo.utils.visible

class NotesListAdapter(
    private var notes: MutableList<Note>,
    private val onPlayPauseClick: (path: String, pos: Int?, stopCallback: ((pos: Int) -> Unit)?) -> Unit,
) : RecyclerView.Adapter<NotesListAdapter.NoteViewHolder>() {
    private lateinit var activity: MainActivity
    private var mActionMode = false
    private var checkedByItemClick = false

    lateinit var observeItemSideEffect: () -> ArkMediaPlayerSideEffect
    lateinit var observeItemState: () -> ArkMediaPlayerState

    private var isFromSearch: Boolean = false
    private var searchKeyWord: String = ""

    private val cornerRadius by lazy {
        activity.resources.getDimension(R.dimen.corner_radius_big)
    }

    var onItemLongPressed: ((pos: Int, note: Note) -> Unit)? = null
    var onItemClicked: (() -> Unit)? = null

    private val selectedNoteCount by lazy { MutableLiveData<Int>() }
    val observableSelectedNoteCount by lazy { selectedNoteCount }
    val selectedNotedForDelete = mutableListOf<Note>()

    fun setActivity(activity: AppCompatActivity) {
        this.activity = activity as MainActivity
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): NoteViewHolder {
        val binding = AdapterTextNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.clipToOutline = true
        return NoteViewHolder(binding.root)
    }

    override fun onBindViewHolder(
        holder: NoteViewHolder,
        position: Int,
    ) {
        val note = notes[holder.bindingAdapterPosition]
        holder.title.text = note.getAutoTitle(activity)

        if (isFromSearch) {
            holder.title.highlightWord(searchKeyWord)
        }

        if (note is TextNote) {
            holder.contentPreview.text = note.text
        } else {
            holder.contentPreview.text = ""
        }
        holder.layoutAudioView.root.gone()
        holder.ivGraphicThumb.gone()
        if (note is VoiceNote) {
            val isRecordingExist = note.path.toFile().length() > 0L
            if (isRecordingExist) {
                holder.layoutAudioView.root.visible()
                holder.layoutAudioView.tvDuration.text = note.duration
            } else {
                holder.layoutAudioView.root.gone()
            }

            holder.btnPlayPause.setOnClickListener {
                onPlayPauseClick(note.path.toString(), holder.bindingAdapterPosition) { stopPos ->
                    val realPos =
                        if (holder.bindingAdapterPosition >= 0) {
                            holder.bindingAdapterPosition
                        } else {
                            position
                        }
                    showPlaybackIdleState(holder)
                    (notes[realPos] as VoiceNote).isPlaying = false
                    holder.layoutAudioView.animAudioPlaying.resetWave()
                    holder.layoutAudioView.animAudioPlaying.invalidateWave(0)
                    holder.tvPlayingPosition.gone()
                    notifyItemChanged(realPos)
                }
                handleMediaPlayerSideEffect(observeItemSideEffect(), holder)
                note.isPlaying = !note.isPlaying
            }

            if (note.isPlaying) {
                showPlayingState(holder)
                holder.tvPlayingPosition.text = millisToString(note.currentPlayingPos * 1000L)
                holder.tvPlayingPosition.visible()
                holder.layoutAudioView.animAudioPlaying.invalidateWave(note.currentMaxAmplitude)
            } else {
                if (note.pendingForPlaybackReset) {
                    showPlaybackIdleState(holder)
                    holder.tvPlayingPosition.gone()
                    note.pendingForPlaybackReset = false
                } else if (note.waitToBeResumed) {
                    showPlaybackIdleState(holder, isPaused = true)
                    note.waitToBeResumed = false
                } else {
                    showPlaybackIdleState(holder)
                }
            }
        } else if (note is GraphicNote) {
            holder.ivGraphicThumb.background =
                BitmapDrawable(
                    holder.itemView.context.resources, note.thumb,
                )
            holder.ivGraphicThumb.visible()
            holder.ivGraphicThumb.shapeAppearanceModel =
                ShapeAppearanceModel.builder()
                    .setBottomLeftCornerSize(0f)
                    .setTopLeftCornerSize(0f)
                    .setTopRightCorner(CornerFamily.ROUNDED, cornerRadius)
                    .setBottomRightCorner(CornerFamily.ROUNDED, cornerRadius)
                    .build()
        }

        if (note.pendingForDelete) {
            holder.tvDelete.visible()
            if (note is GraphicNote) {
                holder.ivGraphicThumb.shapeAppearanceModel =
                    ShapeAppearanceModel.builder()
                        .setAllCorners(CornerFamily.ROUNDED, 0f).build()
            }
        } else {
            holder.tvDelete.gone()
        }

        holder.cbDelete.isChecked = note.selected
        if (mActionMode) {
            holder.cbDelete.visible()
        } else {
            holder.cbDelete.gone()
        }
    }

    override fun getItemCount() = notes.size

    private fun handleMediaPlayerSideEffect(
        effect: ArkMediaPlayerSideEffect,
        holder: NoteViewHolder,
    ) {
        when (effect) {
            is ArkMediaPlayerSideEffect.StartPlaying -> {
                showPlayingState(holder)
            }
            is ArkMediaPlayerSideEffect.PausePlaying -> {
                showPlaybackIdleState(holder, isPaused = true)
            }
            is ArkMediaPlayerSideEffect.StopPlaying -> {
                showPlaybackIdleState(holder)
                holder.tvPlayingPosition.gone()
            }
            is ArkMediaPlayerSideEffect.ResumePlaying -> {
                showPlayingState(holder)
            }
        }
    }

    private fun showPlaybackIdleState(
        holder: NoteViewHolder,
        isPaused: Boolean = false,
    ) {
        val playIcon =
            ResourcesCompat.getDrawable(
                activity.resources,
                R.drawable.ic_play_circle,
                null,
            )

        holder.btnPlayPause.setImageDrawable(playIcon)
        if (!isPaused) {
            holder.layoutAudioView.animAudioPlaying.resetWave()
            holder.layoutAudioView.animAudioPlaying.invalidateWave(0)
            holder.layoutAudioView.animAudioPlaying.background =
                ContextCompat.getDrawable(activity, R.drawable.audio_wave_thumb)
        }
    }

    private fun showPlayingState(holder: NoteViewHolder) {
        val playIcon =
            ResourcesCompat.getDrawable(
                activity.resources,
                R.drawable.ic_pause_circle,
                null,
            )
        holder.btnPlayPause.setImageDrawable(playIcon)
        holder.layoutAudioView.animAudioPlaying.background = null
    }

    fun updateData(
        newNotes: List<Note>,
        fromSearch: Boolean? = null,
        keyword: String? = null,
    ) {
        notes = newNotes.toMutableList()
        isFromSearch = fromSearch ?: false
        searchKeyWord = keyword ?: ""
        notifyDataSetChanged()
    }

    fun getNotes(): MutableList<Note> {
        return notes
    }

    fun removeNote(noteToRemove: Note) {
        notes.remove(noteToRemove)
        selectedNoteCount.postValue(notes.size)
    }

    fun setNotes(notes: List<Note>) {
        this.notes = notes.toMutableList()
    }

    fun toggleActionMode(pos: Int) {
        mActionMode = !mActionMode
        var selectedCount = 0
        notes.forEachIndexed { index, note ->
            note.selected = mActionMode && index == pos
            if (index == pos) {
                selectedCount++
            }
        }
        selectedNoteCount.postValue(selectedCount)
        notifyDataSetChanged()
    }

    fun toggleSelectAllItems(selected: Boolean) {
        notes.forEach { it.selected = selected }
        selectedNotedForDelete.clear()
        selectedNoteCount.postValue(
            if (selected) {
                selectedNotedForDelete.addAll(notes)
                notes.size
            } else {
                0
            },
        )
        notifyDataSetChanged()
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding by viewBinding {
            AdapterTextNoteBinding.bind(itemView)
        }

        val title = binding.tvTitle
        val contentPreview = binding.tvContentPreview
        val btnPlayPause = binding.layoutAudioView.ivPlayAudio
        val layoutAudioView = binding.layoutAudioView
        val tvPlayingPosition = binding.layoutAudioView.tvPlayingPosition
        val ivGraphicThumb = binding.ivGraphicsThumb
        val tvDelete = binding.tvDelete
        val cbDelete = binding.cbDelete

        var isSwiping: Boolean = false

        private val clickNoteToEditListener =
            View.OnClickListener {
                if (mActionMode) {
                    checkedByItemClick = true
                    binding.cbDelete.toggle()
                    return@OnClickListener
                }
                var tag = EditTextNotesFragment.TAG
                when (val selectedNote = notes[bindingAdapterPosition]) {
                    is TextNote -> activity.fragment = EditTextNotesFragment.newInstance(selectedNote)
                    is GraphicNote -> {
                        activity.fragment = EditGraphicNotesFragment.newInstance(selectedNote)
                        tag = EditGraphicNotesFragment.TAG
                    }
                    is VoiceNote -> {
                        activity.fragment = ArkRecorderFragment.newInstance(selectedNote)
                        tag = ArkRecorderFragment.TAG
                    }
                }
                onItemClicked?.invoke()
                activity.replaceFragment(activity.fragment, tag)
            }

        private val noteCheckedListener =
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed && !checkedByItemClick) return@OnCheckedChangeListener
                checkedByItemClick = false

                val selectedNote = notes[bindingAdapterPosition]
                selectedNote.selected = isChecked
                if (isChecked) {
                    selectedNoteCount.value?.let { count ->
                        selectedNoteCount.postValue(count + 1)
                    }
                    selectedNotedForDelete.add(selectedNote)
                } else {
                    selectedNoteCount.value?.let { count ->
                        selectedNoteCount.postValue(count - 1)
                    }
                    selectedNotedForDelete.remove(selectedNote)
                }

                buttonView.post {
                    notifyItemChanged(bindingAdapterPosition)
                }
            }

        init {
            binding.root.setOnClickListener(clickNoteToEditListener)
            binding.root.setOnLongClickListener {
                onItemLongPressed?.invoke(bindingAdapterPosition, notes[bindingAdapterPosition])
                true
            }
            binding.cbDelete.setOnCheckedChangeListener(noteCheckedListener)
            binding.layoutAudioView.root.setBackgroundResource(R.drawable.bg_audio_view_note_item)
        }
    }
}
