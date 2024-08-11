package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver.OnWindowFocusChangeListener
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.utils.getParcelableCompat
import dev.arkbuilders.arkmemo.utils.getTextFromClipBoard
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.observeSaveResult
import java.lang.StringBuilder

@AndroidEntryPoint
class EditTextNotesFragment: BaseEditNoteFragment() {

    private var note = TextNote()
    private var noteStr: String? = null

    private val pasteNoteClickListener = View.OnClickListener {
        requireContext().getTextFromClipBoard(view) { clipBoardText ->
            if (clipBoardText != null) {
                val newTextBuilder = StringBuilder()
                newTextBuilder.append(binding.editNote.text.toString()).append(clipBoardText)
                binding.editNote.setText(newTextBuilder.toString())
                binding.editNote.setSelection(binding.editNote.text.length)
            }
            else Toast.makeText(requireContext(),
                getString(R.string.nothing_to_paste), Toast.LENGTH_SHORT).show()
        }
    }

    private val windowFocusedListener = OnWindowFocusChangeListener {
        if (it) {
            observeClipboardContent()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel.init {}
        observeSaveResult(notesViewModel.getSaveNoteResultLiveData())
        if(arguments != null) {
            requireArguments().getParcelableCompat(NOTE_KEY, TextNote::class.java)?.let {
                note = it
            }
            noteStr = requireArguments().getString(NOTE_STRING_KEY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var title: String
        val noteTitle = binding.edtTitle
        val editNote = binding.editNote
        val noteTitleChangeListener = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                title = s?.toString() ?: ""
                if (title.isEmpty()) {
                    binding.edtTitle.hint = getString(R.string.hint_new_text_note)
                }
                enableSaveText(isContentChanged() && !isContentEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        hostActivity.title = getString(R.string.edit_note)
        hostActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        hostActivity.showSettingsButton(false)

        noteTitle.setText(this.note.title)
        noteTitle.addTextChangedListener(noteTitleChangeListener)
        editNote.addTextChangedListener {
            enableSaveText(isContentChanged() && !isContentEmpty())
        }
        editNote.isVisible = true
        editNote.requestFocus()
        editNote.setText(this.note.text)

        if(noteStr != null)
            editNote.setText(noteStr)

        binding.tvSave.setOnClickListener {
            notesViewModel.onSaveClick(createNewNote(), parentNote = note) { show ->
                hostActivity.showProgressBar(show)
            }
        }
        enableSaveText(noteStr?.isNotBlank() == true)

        binding.tvPaste.setOnClickListener(pasteNoteClickListener)

        binding.editTextDescription.setText(this.note.description)
        binding.toolbar.ivRightActionIcon.setImageResource(R.drawable.ic_delete_note)
        binding.toolbar.ivRightActionIcon.setOnClickListener {
            showDeleteNoteDialog(note)
        }
        arguments?.getParcelableCompat(NOTE_KEY, TextNote::class.java)
            ?: binding.toolbar.ivRightActionIcon.gone()

        view.viewTreeObserver.addOnWindowFocusChangeListener(windowFocusedListener)
    }

    override fun isContentChanged(): Boolean {
        return note.title != binding.edtTitle.text.toString()
                || note.text != binding.editNote.text.toString()
    }

    override fun isContentEmpty(): Boolean {
        return binding.editNote.text.toString().trim().isEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.viewTreeObserver?.removeOnWindowFocusChangeListener(windowFocusedListener)
    }

    override fun createNewNote(): Note {
        return TextNote(
            title = binding.edtTitle.text.toString(),
            description = binding.editTextDescription.text.toString(),
            text = binding.editNote.text.toString(),
            resource = note.resource
        )
    }

    override fun getCurrentNote(): Note {
        return note
    }

    private fun observeClipboardContent() {
        context?.getTextFromClipBoard(view) {
            val clipboardTextEmpty = it.isNullOrEmpty()
            if (clipboardTextEmpty) {
                binding.tvPaste.alpha = 0.4f
                binding.tvPaste.isClickable = false
            } else {
                binding.tvPaste.alpha = 1f
                binding.tvPaste.isClickable = true
            }
            enableSaveText(!clipboardTextEmpty && isContentChanged())
        }
    }

    private fun enableSaveText(enabled: Boolean) {
        binding.tvSave.isEnabled = enabled
        if (enabled) {
            binding.tvSave.alpha = 1f
        } else {
            binding.tvSave.alpha = 0.4f
        }
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
