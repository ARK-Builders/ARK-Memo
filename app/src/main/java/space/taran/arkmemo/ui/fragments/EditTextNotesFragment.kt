package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import space.taran.arkmemo.R
import space.taran.arkmemo.ui.viewmodels.NotesViewModel
import space.taran.arkmemo.databinding.FragmentEditTextNotesBinding
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.ui.activities.MainActivity

@AndroidEntryPoint
class EditTextNotesFragment: Fragment(R.layout.fragment_edit_text_notes) {

    private val activity: MainActivity by lazy{
        requireActivity() as MainActivity
    }

    private val notesViewModel: NotesViewModel by activityViewModels()

    private val binding by viewBinding(FragmentEditTextNotesBinding::bind)

    private var note = TextNote(TextNote.Content("", ""))
    private var noteStr: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel.init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var note = this.note
        val editTextListener = object: TextWatcher{
            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val noteString = s?.toString()
                var title = this@EditTextNotesFragment.note.content.title
                if(noteString != null){
                    if (title == "") for(char in noteString){
                        if(char != '\n'){
                            title += char
                        }
                        else break
                    }
                    val content = TextNote.Content(
                        title = title,
                        data = noteString
                    )
                    note = TextNote(
                        content = content
                    )
                }
            }
        }
        val editNote = binding.editNote
        val saveNoteButton = binding.saveNote

        if(arguments != null) {
            requireArguments().getParcelable<TextNote>(NOTE_KEY)?.let {
                this.note = it
            }
            noteStr = requireArguments().getString(NOTE_STRING_KEY)
        }

        activity.title = getString(R.string.edit_note)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.showSettingsButton(false)

        editNote.requestFocus()
        editNote.addTextChangedListener(editTextListener)
        editNote.setText(this.note.content.data)

        if(noteStr != null)
            editNote.setText(noteStr)

        saveNoteButton.setOnClickListener {
            notesViewModel.onSaveClick(note) { show ->
                activity.showProgressBar(show)
                if (!show) {
                    Toast.makeText(requireContext(), getString(R.string.ark_memo_note_saved),
                        Toast.LENGTH_SHORT)
                        .show()
                    activity.onBackPressedDispatcher.onBackPressed()
                }
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