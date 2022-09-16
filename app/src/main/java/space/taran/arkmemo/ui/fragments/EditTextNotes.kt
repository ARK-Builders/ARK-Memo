package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import space.taran.arkmemo.R
import space.taran.arkmemo.data.viewmodels.TextNotesViewModel
import space.taran.arkmemo.databinding.FragmentEditTextNotesBinding
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.ui.activities.hideSettingsButton

@AndroidEntryPoint
class EditTextNotes(): Fragment(R.layout.fragment_edit_text_notes) {
    constructor(note: TextNote): this(){
        this.note = note
    }

    constructor(note: String): this(){
        noteStr = note
    }

    private val activity: AppCompatActivity by lazy{
        requireActivity() as AppCompatActivity
    }

    private val textNotesViewModel: TextNotesViewModel by activityViewModels()

    private val binding by viewBinding(FragmentEditTextNotesBinding::bind)

    var noteTimeStamp = ""
    var noteDate = ""

    private var note: TextNote? = null
    private var noteStr = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
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
                        note = TextNote(
                            title = title ,
                            contents = noteString,
                            date = noteDate,
                            timeStamp = noteTimeStamp
                        )
                    }
                }
            }
            val editNote = binding.editNote
            val saveNoteButton = binding.saveNote

            activity.title = getString(R.string.edit_note)
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            hideSettingsButton()

            editNote.requestFocus()
            editNote.addTextChangedListener(editTextListener)

            if(this.note != null)
                editNote.setText(this.note?.contents)

            if(noteStr.isNotEmpty())
                editNote.setText(noteStr)

            saveNoteButton.setOnClickListener {
                if(note != null) {
                    with(textNotesViewModel){
                        saveNote(requireContext(), note!!)
                        Toast.makeText(requireContext(), getString(R.string.ark_memo_note_saved),
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
    }
}