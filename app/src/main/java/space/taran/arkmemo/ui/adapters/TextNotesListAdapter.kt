package space.taran.arkmemo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import space.taran.arkmemo.R
import space.taran.arkmemo.databinding.TextNoteBinding
import space.taran.arkmemo.data.models.TextNote
import space.taran.arkmemo.ui.activities.MainActivity
import space.taran.arkmemo.ui.activities.replaceFragment
import space.taran.arkmemo.ui.dialogs.NoteDeleteDialogFragment
import space.taran.arkmemo.ui.fragments.EditTextNotesFragment
import space.taran.arkmemo.ui.fragments.TextNoteVersionsFragment

class TextNotesListAdapter(private val notes: List<TextNote>): RecyclerView.Adapter<TextNotesListAdapter.NoteViewHolder>() {

    private var activity: MainActivity? = null
    private var fragmentManager: FragmentManager? = null
    private var showVersionsTracker = false
    var showLatestNoteIcon: (TextNote) -> Boolean = {
        false
    }
    private var showVersionsFork = false

    fun setActivity(activity: AppCompatActivity){
        this.activity = activity as MainActivity
    }

    fun setFragmentManager(manager: FragmentManager){
        fragmentManager = manager
    }

    fun showVersionsTracker(show: Boolean) {
        showVersionsTracker = show
    }

    fun showVersionsFork(show: Boolean) {
        showVersionsFork = show
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.text_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.title.text = notes[position].content.title
        holder.date.text = notes[position].meta?.modified.toString()
        holder.ivLatestNote.isVisible = showLatestNoteIcon(notes[position])
    }

    override fun getItemCount() = notes.size

    inner class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val binding by viewBinding{
            TextNoteBinding.bind(itemView)
        }

        val title = binding.noteTitle
        val date = binding.noteDate
        val ivLatestNote = binding.ivLatestNote

        private val clickNoteToEditListener = View.OnClickListener {
            editNote()
        }

        private val forkNoteToEditListener = View.OnClickListener {
            editNote(true)
        }

        private val deleteNoteClickListener = View.OnClickListener {
            NoteDeleteDialogFragment()
                .setNoteToBeDeleted(notes[bindingAdapterPosition])
                .show(fragmentManager!!, NoteDeleteDialogFragment.TAG)
        }

        private val trackVersionsListener = View.OnClickListener {
            val note = notes[bindingAdapterPosition]
            activity?.fragment = TextNoteVersionsFragment.newInstance(
                note
            )
            activity?.replaceFragment(activity?.fragment!!, TextNoteVersionsFragment.TAG)
        }

        init {
            binding.theNote.setOnClickListener(clickNoteToEditListener)
            binding.btnDelete.setOnClickListener(deleteNoteClickListener)
            binding.btnTrackVersions.apply {
                isVisible = showVersionsTracker
                setOnClickListener(trackVersionsListener)
            }
            binding.btnForkVersions.apply {
                isVisible = showVersionsFork
                setOnClickListener(forkNoteToEditListener)
            }
        }

        private fun editNote(forked: Boolean = false) {
            val selectedNote = notes[bindingAdapterPosition]
            selectedNote.isForked = forked
            activity?.fragment = EditTextNotesFragment.newInstance(selectedNote)
            activity?.replaceFragment(activity?.fragment!!, EditTextNotesFragment.TAG)
        }
    }
}