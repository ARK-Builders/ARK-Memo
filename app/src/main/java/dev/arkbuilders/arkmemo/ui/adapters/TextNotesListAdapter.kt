package dev.arkbuilders.arkmemo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.TextNoteBinding
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.activities.replaceFragment
import dev.arkbuilders.arkmemo.ui.dialogs.NoteDeleteDialogFragment
import dev.arkbuilders.arkmemo.ui.fragments.EditTextNotesFragment

class TextNotesListAdapter(private val notes: List<TextNote>): RecyclerView.Adapter<TextNotesListAdapter.NoteViewHolder>() {

    private var activity: MainActivity? = null
    private var fragmentManager: FragmentManager? = null

    fun setActivity(activity: AppCompatActivity){
        this.activity = activity as MainActivity
    }

    fun setFragmentManager(manager: FragmentManager){
        fragmentManager = manager
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.text_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.title.text = notes[position].content.title
        holder.date.text = notes[position].meta?.modified?.toString() ?: "Just now"
    }

    override fun getItemCount() = notes.size

    inner class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val binding by viewBinding{
            TextNoteBinding.bind(itemView)
        }

        val title = binding.noteTitle
        val date = binding.noteDate

        private val clickNoteToEditListener = View.OnClickListener {
            val selectedNote = notes[bindingAdapterPosition]
            activity?.fragment = EditTextNotesFragment.newInstance(selectedNote)
            activity?.replaceFragment(activity?.fragment!!, EditTextNotesFragment.TAG)
        }

        private val deleteNoteClickListener = View.OnClickListener{
            NoteDeleteDialogFragment()
                .setNoteToBeDeleted(notes[bindingAdapterPosition])
                .show(fragmentManager!!, NoteDeleteDialogFragment.TAG)
        }

        init {
            binding.theNote.setOnClickListener(clickNoteToEditListener)
            binding.deleteNote.setOnClickListener(deleteNoteClickListener)
        }

    }
}