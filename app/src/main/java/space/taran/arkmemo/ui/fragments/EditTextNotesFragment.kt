package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import space.taran.arkmemo.R
import space.taran.arkmemo.data.viewmodels.EditTextNotesViewModel
import space.taran.arkmemo.data.viewmodels.TextNotesViewModel
import space.taran.arkmemo.databinding.FragmentEditTextNotesBinding
import space.taran.arkmemo.models.TextNote
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
    private var rootResourceId: Long? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var note:TextNote? = null
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
                    note = TextNote(
                        content = content
                    )
                }
            }
        }
        val editNote = binding.editNote
        val saveNoteButton = binding.saveNote

        if(arguments != null) {
            this.note = requireArguments().getParcelable(NOTE_KEY)
            //Log.d("Note", "${this.note?.content}")
            noteStr = requireArguments().getString(NOTE_STRING_KEY)
            rootResourceId = requireArguments().getLong(ROOT_RESOURCE_ID_LONG_KEY)
        }

        activity.title = getString(R.string.edit_note)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.showSettingsButton(false)

        editNote.requestFocus()
        editNote.addTextChangedListener(editTextListener)

        if(this.note != null)
            editNote.setText(this.note?.content?.data!!)

        if(noteStr != null)
            editNote.setText(noteStr)

        saveNoteButton.setOnClickListener {
            if(note != null) {
                with(editViewModel){
                    saveNote(note!!,rootResourceId)
                    Toast.makeText(requireContext(), getString(R.string.ark_memo_note_saved),
                        Toast.LENGTH_SHORT)
                        .show()
                    activity.onBackPressed()
                }
            }
        }
    }


    companion object{
        const val TAG = "Edit Text Notes"
        private const val NOTE_STRING_KEY = "note string"
        private const val ROOT_RESOURCE_ID_LONG_KEY = "root resource id long"
        private const val NOTE_KEY = "note key"

        fun newInstance(note: String,rootResourceId:Long? = null) = EditTextNotesFragment().apply{
            arguments = Bundle().apply {
                putString(NOTE_STRING_KEY, note)
                rootResourceId?.let { putLong(ROOT_RESOURCE_ID_LONG_KEY, it) }
            }
        }

        fun newInstance(note: TextNote,rootResourceId:Long? = null) = EditTextNotesFragment().apply{
            arguments = Bundle().apply{
                putParcelable(NOTE_KEY, note)
                rootResourceId?.let { putLong(ROOT_RESOURCE_ID_LONG_KEY, it) }
            }
        }
    }
}