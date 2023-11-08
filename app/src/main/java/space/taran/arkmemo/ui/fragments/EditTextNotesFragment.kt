package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import space.taran.arkmemo.R
import space.taran.arkmemo.ui.viewmodels.NotesViewModel
import space.taran.arkmemo.databinding.FragmentEditNotesBinding
import space.taran.arkmemo.models.Content
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.ui.activities.MainActivity

@AndroidEntryPoint
class EditTextNotesFragment: Fragment(R.layout.fragment_edit_notes) {

    private val activity: MainActivity by lazy{
        requireActivity() as MainActivity
    }

    private val notesViewModel: NotesViewModel by activityViewModels()

    private val binding by viewBinding(FragmentEditNotesBinding::bind)

    private var note = TextNote(
        "",
        "",
        Content( "")
    )
    private var noteStr: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel.init {}
        if(arguments != null) {
            requireArguments().getParcelable<TextNote>(NOTE_KEY)?.let {
                this.note = it
            }
            noteStr = requireArguments().getString(NOTE_STRING_KEY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var title = this.note.title
        var data = note.content.data
        val editTextListener = object: TextWatcher{
            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                data = s?.toString() ?: ""
            }
        }
        val noteTitle = binding.noteTitle
        val editNote = binding.editNote
        val saveNoteButton = binding.saveNote
        val noteTitleChangeListener = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                title = s?.toString() ?: ""
                saveNoteButton.isEnabled = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        activity.title = getString(R.string.edit_note)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.showSettingsButton(false)

        noteTitle.setText(this.note.title)
        noteTitle.addTextChangedListener(noteTitleChangeListener)
        editNote.isVisible = true
        editNote.requestFocus()
        editNote.addTextChangedListener(editTextListener)
        editNote.setText(this.note.content.data)

        if(noteStr != null)
            editNote.setText(noteStr)

        saveNoteButton.isEnabled = title.isNotEmpty()
        saveNoteButton.setOnClickListener {
            val note = TextNote(
                title,
                content = Content(data),
                meta = this.note.resourceMeta
            )
            notesViewModel.onSaveClick(note) { show ->
                activity.showProgressBar(show)
            }
        }
    }


    companion object{
        const val TAG = "Edit Text Notes"
        private const val NOTE_STRING_KEY = "note string"
        private const val NOTE_KEY = "note key"

        fun newInstance(note: String) = EditTextNotesFragment().apply{
            arguments = Bundle().apply {
                putString(NOTE_STRING_KEY, note)
            }
        }

        fun newInstance(note: TextNote) = EditTextNotesFragment().apply{
            arguments = Bundle().apply{
                putParcelable(NOTE_KEY, note)
            }
        }
    }
}