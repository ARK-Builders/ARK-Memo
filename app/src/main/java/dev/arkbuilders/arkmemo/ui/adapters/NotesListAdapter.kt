package dev.arkbuilders.arkmemo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.NoteBinding
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.dialogs.NoteDeleteDialog
import dev.arkbuilders.arkmemo.ui.fragments.EditGraphicNotesFragment
import dev.arkbuilders.arkmemo.ui.fragments.EditTextNotesFragment
import dev.arkbuilders.arkmemo.ui.fragments.VersionsFragment
import dev.arkbuilders.arkmemo.utils.replaceFragment

class NotesListAdapter(
    private val notes: List<Note>,
    private val isLatestNote: (Note) -> Boolean = { false }
):
    RecyclerView.Adapter<NotesListAdapter.NoteViewHolder>() {

    private lateinit var activity: MainActivity
    private lateinit var fragmentManager: FragmentManager
    private var showVersionsTracker = false
    private var showVersionFork = false

    fun setActivity(activity: AppCompatActivity) {
        this.activity = activity as MainActivity
    }

    fun setFragmentManager(manager: FragmentManager) {
        fragmentManager = manager
    }

    fun showVersionTracker(show: Boolean) {
        showVersionsTracker = show
    }

    fun showVersionFork(show: Boolean) {
        showVersionFork = show
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.title.text = note.title
        holder.date.text = note.resource?.modified?.toString() ?:
                activity.getString(R.string.ark_memo_just_now)
        holder.ivLatestNote.isVisible = isLatestNote(note)
    }

    override fun getItemCount() = notes.size

    inner class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val binding by viewBinding {
            NoteBinding.bind(itemView)
        }

        val title = binding.noteTitle
        val date = binding.noteDate
        val ivLatestNote = binding.ivLatestNote

        private val trackVersionsClickListener = View.OnClickListener {
            val note = notes[bindingAdapterPosition]
            activity.fragment = VersionsFragment.newInstance(note)
            activity.replaceFragment(activity.fragment, VersionsFragment.TAG)
        }

        private val forkVersionClickListener = View.OnClickListener {
            editNote(true)
        }

        private val noteClickListener = View.OnClickListener {
            editNote()
        }

        private val deleteNoteClickListener = View.OnClickListener {
            NoteDeleteDialog()
                .setNote(notes[bindingAdapterPosition])
                .show(fragmentManager, NoteDeleteDialog.TAG)
        }

        init {
            binding.theNote.setOnClickListener(noteClickListener)
            binding.btnDelete.setOnClickListener(deleteNoteClickListener)
            binding.btnForkVersion.apply {
                isVisible = showVersionFork
                setOnClickListener(forkVersionClickListener)
            }
            binding.btnTrackVersions.apply {
                isVisible = showVersionsTracker
                setOnClickListener(trackVersionsClickListener)
            }
        }

        private fun editNote(forked: Boolean = false) {
            var tag = EditTextNotesFragment.TAG
            val note = notes[bindingAdapterPosition]
            note.isForked = forked
            when (note) {
                is TextNote -> activity.fragment = EditTextNotesFragment.newInstance(note)
                is GraphicNote -> {
                    activity.fragment = EditGraphicNotesFragment.newInstance(note)
                    tag = EditGraphicNotesFragment.TAG
                }
            }
            activity.replaceFragment(activity.fragment, tag)
        }
    }
}