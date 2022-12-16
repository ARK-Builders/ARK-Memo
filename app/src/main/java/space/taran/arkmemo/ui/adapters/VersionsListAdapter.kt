package space.taran.arkmemo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import space.taran.arkmemo.R
import space.taran.arkmemo.databinding.TextNoteBinding
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.models.Version
import space.taran.arkmemo.ui.activities.MainActivity
import space.taran.arkmemo.ui.activities.replaceFragment
import space.taran.arkmemo.ui.dialogs.NoteDeleteDialogFragment
import space.taran.arkmemo.ui.fragments.EditTextNotesFragment

class VersionsListAdapter: RecyclerView.Adapter<VersionsListAdapter.VersionViewHolder>() {

    private var activity: MainActivity? = null
    private var fragmentManager: FragmentManager? = null

    private var notes: List<TextNote>? = null
    private var version: Version? = null

    fun setNotes(notes: List<TextNote>){
        this.notes = notes
    }

    fun setVersion(version: Version){
        this.version = version
    }

    fun setActivity(activity: AppCompatActivity){
        this.activity = activity as MainActivity
    }

    fun setFragmentManager(manager: FragmentManager){
        fragmentManager = manager
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VersionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.text_note, parent, false)
        return VersionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VersionViewHolder, position: Int) {
        holder.versionsNote.visibility = View.GONE
        holder.versionsNote.isEnabled = false
        holder.title.text = notes?.get(position)?.content?.title
        holder.date.text = notes?.get(position)?.meta?.modified.toString()
    }

    override fun getItemCount() = notes?.size ?: 0

    inner class VersionViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val binding by viewBinding{
            TextNoteBinding.bind(itemView)
        }
        val versionsNote = binding.versionsNote
        val title = binding.noteTitle
        val date = binding.noteDate

        private val clickNoteToEditListener = View.OnClickListener {
            val selectedNote = notes!![bindingAdapterPosition]
            val selectedVersion = version!!
            activity?.fragment = EditTextNotesFragment.newInstance(selectedNote,
                selectedVersion.meta?.rootResourceId,
                true
            )
            activity?.replaceFragment(activity?.fragment!!, EditTextNotesFragment.TAG)
        }

        private val deleteNoteClickListener = View.OnClickListener{
            NoteDeleteDialogFragment()
                .setNoteToBeDeleted(notes!![bindingAdapterPosition],version!!,true)
                .show(fragmentManager!!, NoteDeleteDialogFragment.TAG)
        }

        init {
            binding.theNote.setOnClickListener(clickNoteToEditListener)
            binding.deleteNote.setOnClickListener(deleteNoteClickListener)
        }

    }
}