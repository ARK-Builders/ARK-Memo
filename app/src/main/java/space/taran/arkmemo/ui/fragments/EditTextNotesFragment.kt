package space.taran.arkmemo.ui.fragments

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import space.taran.arkmemo.R
import space.taran.arkmemo.data.viewmodels.EditTextNotesViewModel
import space.taran.arkmemo.databinding.FragmentEditTextNotesBinding
import space.taran.arkmemo.data.models.TextNote
import space.taran.arkmemo.data.viewmodels.VersionsViewModel
import space.taran.arkmemo.ui.activities.MainActivity

@AndroidEntryPoint
class EditTextNotesFragment: Fragment(R.layout.fragment_edit_text_notes) {

    private val activity: MainActivity by lazy{
        requireActivity() as MainActivity
    }

    private val editViewModel: EditTextNotesViewModel by activityViewModels()
    private val versionsViewModel: VersionsViewModel by activityViewModels()

    private val binding by viewBinding(FragmentEditTextNotesBinding::bind)

    private var note =  TextNote(
        TextNote.Content("", "")
    )

    private var noteStr: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        lifecycleScope.launch {
            versionsViewModel.init()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editNote = binding.editNote
        val saveNoteButton = binding.saveNote
        val editTextListener = object: TextWatcher{
            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val noteString = s?.toString()
                var title = ""
                if(noteString != null){
                    for(char in noteString){
                        if(char != '\n'){
                            title += char
                        }
                        else break
                    }
                    val content = TextNote.Content(
                        title = title,
                        data = noteString
                    )
                    note.putContent(content)
                    if (note.isForked) {
                        lifecycleScope.launchWhenStarted {
                            note.hasChanged {
                                saveNoteButton.isEnabled = it
                            }
                        }
                    }
                }
            }
        }

        fun setupKeyboard() {
            val inputMethodManager = requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            editNote.requestFocus()
            inputMethodManager.showSoftInput(editNote, SHOW_IMPLICIT)
            activity.title = getString(R.string.edit_note)
            editNote.addTextChangedListener(editTextListener)
        }

        if(arguments != null) {
            val note = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requireArguments().getParcelable(NOTE_KEY, TextNote::class.java)
            else requireArguments().getParcelable(NOTE_KEY)
            noteStr = requireArguments().getString(NOTE_STRING_KEY)
            if (note != null) this.note = note
            if (noteStr != null) {
                val title = noteStr?.split("\n")?.get(0)!!
                this.note.putContent(
                    TextNote.Content(
                        title = title,
                        data = noteStr!!
                    )
                )
            }
        }

        if (
            versionsViewModel.isVersioned(note) && !versionsViewModel.isLatestVersion(note)
        ) {
            if (note.isForked) setupKeyboard() else {
                activity.title = getString(R.string.ark_memo_old_version)
                editNote.isClickable = false
                editNote.isFocusable = false
                editNote.setBackgroundColor(Color.LTGRAY)
            }
        } else setupKeyboard()

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.showSettingsButton(false)

        editNote.setText(note.content.data)

        saveNoteButton.apply {
            isVisible = versionsViewModel.isLatestVersion(note) ||
                    versionsViewModel.isNotVersionedYet(note) || note.isForked
            if (isVisible) {
                setOnClickListener {
                    if (note.isNotEmpty()) {
                        with(editViewModel) {
                            saveNote(note) { note, newId ->
                                versionsViewModel.addNoteToVersions(note, newId)
                                versionsViewModel.emitLatestVersionNoteId(newId)
                            }
                            Toast.makeText(
                                requireContext(), getString(R.string.ark_memo_note_saved),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            activity.onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
        }
    }


    companion object{
        const val TAG = "edit-text-notes-fragment"
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