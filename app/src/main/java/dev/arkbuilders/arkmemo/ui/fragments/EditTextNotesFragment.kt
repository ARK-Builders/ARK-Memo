package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Build
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.databinding.FragmentEditNotesBinding
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.utils.observeSaveResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import dev.arkbuilders.arkmemo.ui.viewmodels.VersionsViewModel
import dev.arkbuilders.arkmemo.utils.NOTE_KEY
import dev.arkbuilders.arkmemo.utils.NOTE_PASTE_KEY

@AndroidEntryPoint
class EditTextNotesFragment: Fragment(R.layout.fragment_edit_notes) {

    private val activity: MainActivity by lazy{
        requireActivity() as MainActivity
    }

    private val notesViewModel: NotesViewModel by activityViewModels()
    private val versionsViewModel: VersionsViewModel by activityViewModels()

    private val binding by viewBinding(FragmentEditNotesBinding::bind)

    private lateinit var btnSave: Button
    private lateinit var etNote: EditText
    private lateinit var etTitle: EditText

    private var data: String = ""
    private var title: String = ""
    private var note = TextNote()

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
            Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            data = s?.toString() ?: ""
            if (note.isForked) {
                btnSave.isEnabled = data != note.text
            }
        }
    }

    private val titleWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            title = s?.toString() ?: ""
        }

        override fun afterTextChanged(s: Editable?) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel.init {}
        observeSaveResult(notesViewModel.getSaveNoteResultLiveData())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.initEditUI()
        readArguments()
        initUI()
    }

    private fun initUI() {
        val defaultTitle = getString(
            R.string.ark_memo_text_note,
            LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        )
        etTitle = binding.noteTitle
        etNote = binding.editNote
        btnSave = binding.saveNote

        setupKeyboard()

        etTitle.hint = defaultTitle
        etTitle.setText(this.note.title)
        etTitle.addTextChangedListener(titleWatcher)
        etNote.isVisible = true
        etNote.requestFocus()
        etNote.addTextChangedListener(textWatcher)

        btnSave.apply {
            if (note.isForked) isEnabled = data != note.text
            if (isVisible) {
                setOnClickListener {
                    if (data.isNotEmpty()) {
                        notesViewModel.onSaveClick(
                            TextNote(
                                title = title.ifEmpty { defaultTitle },
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

    private fun readArguments() {
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
            requireArguments().getString(NOTE_PASTE_KEY)?.let {
                data = it
                title = it.split("\n")[0].take(20)
            }
            etNote.setText(data)
        }
    }

    private fun prepare(newNote: TextNote) {
        note = newNote
        data = newNote.text
        title = newNote.title.ifEmpty {
            // For backward compatibility, text notes before dedicated properties storage
            data.split("\n")[0].take(20)
        }
    }

    private fun setupKeyboard() {
        val inputMethodManager = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
        inputMethodManager.showSoftInput(etNote, SHOW_IMPLICIT)
    }

    private fun checkNoteForReadOnly() {
        val resourceId = note.resource?.id!!
        val isReadOnly = versionsViewModel.isVersioned(resourceId) &&
                !versionsViewModel.isLatestResource(resourceId) && !note.isForked
        if (isReadOnly) {
            activity.title = getString(R.string.ark_memo_old_version)
            etNote.isClickable = false
            etNote.isFocusable = false
            etNote.setBackgroundColor(Color.LTGRAY)
        }
        btnSave.isVisible = !isReadOnly
    }

    companion object{
        const val TAG = "text-notes-fragment"

        fun newInstance(note: String) = EditTextNotesFragment().apply{
            arguments = Bundle().apply {
                putString(NOTE_PASTE_KEY, note)
            }
        }

        fun newInstance(note: TextNote) = EditTextNotesFragment().apply{
            arguments = Bundle().apply{
                putParcelable(NOTE_KEY, note)
            }
        }
    }
}
