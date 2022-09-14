package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import space.taran.arkmemo.R
import space.taran.arkmemo.data.viewmodels.TextNotesViewModel
import space.taran.arkmemo.databinding.EditTextNotesBinding
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.ui.activities.hideSettingsButton

@AndroidEntryPoint
class EditTextNotes(): Fragment(R.layout.edit_text_notes) {
    constructor(note: TextNote): this(){
        this.note = note
    }

    private val activity: AppCompatActivity by lazy{
        requireActivity() as AppCompatActivity
    }

    private val textNotesViewModel: TextNotesViewModel by activityViewModels()

    private val binding by viewBinding(EditTextNotesBinding::bind)

    var noteTimeStamp = ""

    private var note: TextNote? = null

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
                            date = noteTimeStamp
                        )
                    }
                }
            }
            val editNote: EditText = binding.editNote
            val saveNoteButton: ExtendedFloatingActionButton = binding.saveNote

            activity.title = getString(R.string.edit_note)
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            hideSettingsButton()

            editNote.addTextChangedListener(editTextListener)

            if(this.note != null)
                editNote.setText(this.note?.contents)

            saveNoteButton.setOnClickListener {
                if(note != null) {
                    with(textNotesViewModel){
                        saveNote(requireContext(), note!!)
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