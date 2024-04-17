package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.dialogs.CommonActionDialog
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.ui.views.toast
import dev.arkbuilders.arkmemo.utils.observeSaveResult

@AndroidEntryPoint
class EditTextNotesFragment: BaseEditNoteFragment() {

    private val activity: MainActivity by lazy{
        requireActivity() as MainActivity
    }

    private val notesViewModel: NotesViewModel by activityViewModels()

    private var note = TextNote()
    private var noteStr: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel.init {}
        observeSaveResult(notesViewModel.getSaveNoteResultLiveData())
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
        var title = this.note.title
        var data = note.text
        val editTextListener = object: TextWatcher{
            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                data = s?.toString() ?: ""
            }
        }
        val noteTitle = binding.edtTitle
        val editNote = binding.editNote
        val noteTitleChangeListener = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                title = s?.toString() ?: ""
                if (title.isEmpty()) {
                    binding.edtTitle.hint = getString(R.string.hint_new_text_note)
                }
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
        editNote.setText(this.note.text)

        if(noteStr != null)
            editNote.setText(noteStr)

        binding.tvSave.setOnClickListener {
            notesViewModel.onSaveClick(createNewNote()) { show ->
                activity.showProgressBar(show)
            }
        }

        binding.editTextDescription.setText(this.note.description)
        binding.toolbar.ivBack.setOnClickListener {
            showSaveNoteDialog()
        }
        binding.toolbar.ivRightActionIcon.setImageResource(R.drawable.ic_delete_note)
        binding.toolbar.ivRightActionIcon.setOnClickListener {
            showDeleteNoteDialog()
        }
    }

    private fun showSaveNoteDialog() {
        val saveNoteDialog = CommonActionDialog(
            title = R.string.dialog_save_note_title,
            message = R.string.dialog_save_note_message,
            positiveText = R.string.save,
            negativeText = R.string.discard,
            isAlert = false,
            onPositiveClick = {
                notesViewModel.onSaveClick(createNewNote()) { show ->
                    activity.showProgressBar(show)
                }
            },
            onNegativeClicked = {
                activity.onBackPressedDispatcher.onBackPressed()
            })
        saveNoteDialog.show(parentFragmentManager, CommonActionDialog.TAG)
    }

    private fun showDeleteNoteDialog() {
        CommonActionDialog(
            title = R.string.delete_note,
            message = R.string.ark_memo_delete_warn ,
            positiveText = R.string.action_delete,
            negativeText = R.string.ark_memo_cancel,
            isAlert = true,
            onPositiveClick = {
            notesViewModel.onDeleteConfirmed(note)
            activity.onBackPressedDispatcher.onBackPressed()
            toast(requireContext(), getString(R.string.note_deleted))
        }, onNegativeClicked = {
        }).show(parentFragmentManager, CommonActionDialog.TAG)
    }

    private fun createNewNote(): TextNote {
        return TextNote(
            title = binding.edtTitle.text.toString(),
            description = binding.editTextDescription.text.toString(),
            text = binding.editNote.text.toString(),
            resource = note.resource
        )
    }

    companion object{
        const val TAG = "EditTextNotesFragment"
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
