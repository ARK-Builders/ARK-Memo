package dev.arkbuilders.arkmemo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import dev.arkbuilders.arkmemo.utils.replaceFragment

class NotesListAdapter(private val notes: List<Note>):
    RecyclerView.Adapter<NotesListAdapter.NoteViewHolder>() {

    private lateinit var activity: MainActivity
    private lateinit var fragmentManager: FragmentManager

    fun setActivity(activity: AppCompatActivity) {
        this.activity = activity as MainActivity
    }

    fun setFragmentManager(manager: FragmentManager) {
        fragmentManager = manager
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.title.text = notes[position].title
        holder.date.text = notes[position].resource?.modified?.toString() ?:
                activity.getString(R.string.ark_memo_just_now)
    }

    override fun getItemCount() = notes.size

    inner class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val binding by viewBinding {
            NoteBinding.bind(itemView)
        }

        val title = binding.noteTitle
        val date = binding.noteDate

        private val clickNoteToEditListener = View.OnClickListener {
            var tag = EditTextNotesFragment.TAG
            when (val selectedNote = notes[bindingAdapterPosition]) {
                is TextNote -> activity.fragment = EditTextNotesFragment.newInstance(selectedNote)
                is GraphicNote -> {
                    activity.fragment = EditGraphicNotesFragment.newInstance(selectedNote)
                    tag = EditGraphicNotesFragment.TAG
                }
            }
            activity.replaceFragment(activity.fragment, tag)
        }

        private val deleteNoteClickListener = View.OnClickListener {
            NoteDeleteDialog()
                .setNoteToBeDeleted(notes[bindingAdapterPosition])
                .show(fragmentManager, NoteDeleteDialog.TAG)
        }

        init {
            binding.theNote.setOnClickListener(clickNoteToEditListener)
            binding.deleteNote.setOnClickListener(deleteNoteClickListener)
        }
    }
}