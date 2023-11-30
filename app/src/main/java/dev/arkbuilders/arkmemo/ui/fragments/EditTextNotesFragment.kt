package dev.arkbuilders.arkmemo.ui.fragments

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
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.databinding.FragmentEditTextNotesBinding
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.viewmodels.VersionsViewModel

@AndroidEntryPoint
class EditTextNotesFragment: Fragment(R.layout.fragment_edit_text_notes) {

    private val activity: MainActivity by lazy{
        requireActivity() as MainActivity
    }

    private val notesViewModel: NotesViewModel by activityViewModels()
    private val versionsViewModel: VersionsViewModel by activityViewModels()

    private val binding by viewBinding(FragmentEditTextNotesBinding::bind)

    private var note =  TextNote()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel.init()
        subscribeToSaveResultLiveData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var title = note.title
        var data = note.text
        val editNote = binding.editNote
        val saveNoteButton = binding.saveNote
        val editTextListener = object: TextWatcher{
            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                data = s?.toString() ?: ""
                if (title.isEmpty()) for(char in data){
                    if(char != '\n'){
                        title += char
                    }
                    else break
                }
                if (note.isForked) {
                    saveNoteButton.isEnabled = data != note.text
                }
            }
        }

        fun prepare(newNote: TextNote) {
            note = newNote
            data = newNote.text
            title = newNote.title
        }

        fun setupKeyboard() {
            val inputMethodManager = requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            inputMethodManager.showSoftInput(editNote, SHOW_IMPLICIT)
        }

        fun checkNoteForReadOnly() {
            val resourceId = note.resource?.id!!
            val isReadOnly = versionsViewModel.isVersioned(resourceId) &&
                    !versionsViewModel.isLatestResource(resourceId) && !note.isForked
            if (isReadOnly)             {
                activity.title = getString(R.string.ark_memo_old_version)
                editNote.isClickable = false
                editNote.isFocusable = false
                editNote.setBackgroundColor(Color.LTGRAY)
            }
            saveNoteButton.isVisible = !isReadOnly
        }

        activity.title = getString(R.string.edit_note)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.showSettingsButton(false)

        setupKeyboard()

        if(arguments != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requireArguments().getParcelable(NOTE_KEY, TextNote::class.java)?.let {
                    prepare(it)
                    checkNoteForReadOnly()
                }
            else requireArguments().getParcelable<TextNote>(NOTE_KEY)?.let {
                prepare(it)
                checkNoteForReadOnly()
            }
            requireArguments().getString(NOTE_STRING_KEY)?.let {
                data = it
                title = data.split("\n")[0]
            }
            editNote.setText(data)
        }

        editNote.requestFocus()
        editNote.addTextChangedListener(editTextListener)

        saveNoteButton.apply {
            if (note.isForked) isEnabled = data != note.text
            if (isVisible) {
                setOnClickListener {
                    if (data.isNotEmpty()) {
                        with(notesViewModel) {
                            onSaveClick(
                                TextNote(
                                    title = title,
                                    text = data,
                                    resource = note.resource
                                ),
                                showProgress = { show ->
                                    activity.showProgressBar(show)
                                },
                                saveVersion = { oldId, newId ->
                                    versionsViewModel.createVersion(oldId, newId)
                                    versionsViewModel.updateLatestResourceId(newId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun subscribeToSaveResultLiveData() {
        lifecycleScope.launchWhenStarted {
            notesViewModel.getSaveNoteResultLiveData().observe(viewLifecycleOwner) {
                if (!isResumed) return@observe
                if (it == SaveNoteResult.SUCCESS) {
                    Toast.makeText(
                        requireContext(), getString(R.string.ark_memo_note_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                    activity.onBackPressedDispatcher.onBackPressed()
                } else {
                    Toast.makeText(
                        requireContext(), getString(R.string.ark_memo_note_existing),
                        Toast.LENGTH_SHORT
                    ).show()
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