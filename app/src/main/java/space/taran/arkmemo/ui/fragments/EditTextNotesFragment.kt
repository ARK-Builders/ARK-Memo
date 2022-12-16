package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.taran.arkmemo.R
import space.taran.arkmemo.data.viewmodels.EditTextNotesViewModel
import space.taran.arkmemo.databinding.FragmentEditTextNotesBinding
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.space.taran.arkmemo.utils.CODES_CREATING_NOTE
import space.taran.arkmemo.ui.activities.MainActivity

@AndroidEntryPoint
class EditTextNotesFragment: Fragment(R.layout.fragment_edit_text_notes) {

    private val activity: MainActivity by lazy{
        requireActivity() as MainActivity
    }

    private val editViewModel: EditTextNotesViewModel by viewModels()

    private val binding by viewBinding(FragmentEditTextNotesBinding::bind)

    private var note: TextNote? = null
    private var noteStr: String? = null
    private var isReadOnly = false
    private var rootResourceId: String? = null
    private var noteContent:TextNote.Content=TextNote.Content("","")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                    noteContent = TextNote.Content(
                        title = title,
                        data = noteString
                    )
                }
            }
        }
        val editNote = binding.editNote
        val saveNoteButton = binding.saveNote

        if(arguments != null) {
            this.note = requireArguments().getParcelable(NOTE_KEY)
            noteStr = requireArguments().getString(NOTE_STRING_KEY)
            rootResourceId = requireArguments().getString(ROOT_RESOURCE_ID_LONG_KEY)
            isReadOnly = requireArguments().getBoolean(READ_ONLY_KEY)
        }

        activity.title = getString(R.string.edit_note)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.showSettingsButton(false)

        editNote.requestFocus()
        editNote.addTextChangedListener(editTextListener)

        if(this.note != null) {
            noteContent = note!!.content
            editNote.setText(this.note?.content?.data!!)
        }

        if(noteStr != null)
            editNote.setText(noteStr)

        if(isReadOnly){
            saveNoteButton.visibility = View.GONE
            saveNoteButton.isEnabled = false
            //sets editNote readOnly: maybe it would be best to just use TextView instead, since this maybe brokes scrolling
            editNote.inputType = InputType.TYPE_NULL
            editNote.isLongClickable = false
        }else{
            saveNoteButton.setOnClickListener{
                val noteToSave = if(note != null && note!!.meta != null){//is not new note
                    TextNote( noteContent,note?.meta )
                }else{
                    TextNote( noteContent )
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    editViewModel.saveNote(noteToSave,rootResourceId).takeWhile {
                        it != 0L
                    }.collect {
                        //Show toast here
                        val toastText = if(it < 0){
                            when (it) {
                                CODES_CREATING_NOTE.NOTE_ALREADY_EXISTS.errCode.toLong() -> {
                                    getString(R.string.ark_memo_err_note_already_exists)
                                }
                                else -> {
                                    getString(R.string.ark_memo_err_note_not_created)
                                }
                            }
                        }else{
                            getString(R.string.ark_memo_note_saved)
                        }
                        Toast.makeText(requireContext(), toastText,
                            Toast.LENGTH_SHORT)
                            .show()
                        activity.onBackPressed()
                    }
                }
            }
        }
    }


    companion object{
        const val TAG = "Edit Text Notes"
        private const val NOTE_STRING_KEY = "note string"
        private const val ROOT_RESOURCE_ID_LONG_KEY = "root resource id long"
        private const val READ_ONLY_KEY = "read only"
        private const val NOTE_KEY = "note key"

        fun newInstance(note: String,rootResourceId:String? = null,isReadOnly:Boolean? = null) = EditTextNotesFragment().apply{
            arguments = Bundle().apply {
                putString(NOTE_STRING_KEY, note)
                rootResourceId?.let { putString(ROOT_RESOURCE_ID_LONG_KEY, it) }
                isReadOnly?.let { putBoolean(READ_ONLY_KEY, it) }
            }
        }

        fun newInstance(note: TextNote,rootResourceId:String? = null,isReadOnly:Boolean? = null) = EditTextNotesFragment().apply{
            arguments = Bundle().apply{
                putParcelable(NOTE_KEY, note)
                rootResourceId?.let { putString(ROOT_RESOURCE_ID_LONG_KEY, it) }
                isReadOnly?.let { putBoolean(READ_ONLY_KEY, it) }
            }
        }
    }
}