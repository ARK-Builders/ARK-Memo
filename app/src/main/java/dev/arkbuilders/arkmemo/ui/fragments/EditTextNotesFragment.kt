package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.databinding.FragmentEditNotesBinding
import dev.arkbuilders.arkmemo.models.DEFAULT_TITLE
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity

@AndroidEntryPoint
class EditTextNotesFragment: Fragment(R.layout.fragment_edit_notes) {

    private val activity: MainActivity by lazy{
        requireActivity() as MainActivity
    }

    private val notesViewModel: NotesViewModel by activityViewModels()

    private val binding by viewBinding(FragmentEditNotesBinding::bind)

    private var note = TextNote()
    private var noteStr: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel.init {}
        if(arguments != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requireArguments().getParcelable(NOTE_KEY, TextNote::class.java)?.let {
                    note = it
                }
            else requireArguments().getParcelable<TextNote>(NOTE_KEY)?.let {
                note = it
            }
            noteStr = requireArguments().getString(NOTE_STRING_KEY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val defaultTitle = "Text $DEFAULT_TITLE"
        var title = this.note.title
        var data = note.text
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

        noteTitle.hint = defaultTitle
        noteTitle.setText(this.note.title)
        noteTitle.addTextChangedListener(noteTitleChangeListener)
        editNote.isVisible = true
        editNote.requestFocus()
        editNote.addTextChangedListener(editTextListener)
        editNote.setText(this.note.text)

        if(noteStr != null)
            editNote.setText(noteStr)

        saveNoteButton.setOnClickListener {
            val note = TextNote(
                title = title.ifEmpty { defaultTitle },
                text = data,
                resource = this.note.resource
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