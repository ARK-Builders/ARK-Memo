package space.taran.arkmemo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import space.taran.arkmemo.R
import space.taran.arkmemo.databinding.TextNoteBinding
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.time.MemoCalendar
import space.taran.arkmemo.ui.activities.replaceFragment
import space.taran.arkmemo.ui.fragments.EditTextNotes
import javax.inject.Inject

class TextNotesListAdapter(private val notes: List<TextNote>): RecyclerView.Adapter<TextNotesListAdapter.NoteViewHolder>() {

    private var activity: AppCompatActivity? = null

    fun setActivity(activity: AppCompatActivity){
        this.activity = activity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.text_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.title.text = notes[position].title
        holder.date.text = notes[position].date
    }

    override fun getItemCount() = notes.size

    inner class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val binding by viewBinding{
            TextNoteBinding.bind(itemView)
        }

        private val clickNoteToEditListener = View.OnClickListener {
            val selectedNote = notes[bindingAdapterPosition]
            val editTextNotes = EditTextNotes(selectedNote)
            editTextNotes.noteTimeStamp = MemoCalendar.getDateToday()
            activity?.replaceFragment(editTextNotes, EditTextNotes.TAG)
        }

        init {
            binding.root.setOnClickListener(clickNoteToEditListener)
        }

        val title: TextView = binding.noteTitle
        val date: TextView = binding.noteDate
    }
}