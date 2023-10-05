package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import space.taran.arkmemo.R
import space.taran.arkmemo.data.viewmodels.GraphicalNotesViewModel
import space.taran.arkmemo.databinding.FragmentEditTextNotesBinding
import space.taran.arkmemo.models.GraphicalNote
import space.taran.arkmemo.ui.activities.MainActivity

@AndroidEntryPoint
class EditGraphicalNotes: Fragment(R.layout.fragment_edit_text_notes) {

    private val activity by lazy {
        requireActivity() as MainActivity
    }

    private val binding by viewBinding(FragmentEditTextNotesBinding::bind)

    private val viewModel: GraphicalNotesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val notesCanvas = binding.notesCanvas
        val saveButton = binding.saveNote

        activity.title = getString(R.string.edit_note)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.showSettingsButton(false)

        notesCanvas.isVisible = true
        notesCanvas.setViewModel(viewModel)
        saveButton.setOnClickListener {
            val svgText = viewModel.svg().get()
            val note = GraphicalNote(svgText)
            viewModel.onSaveClick(note)
            Toast.makeText(
                requireContext(),
                getString(R.string.ark_memo_note_saved),
                Toast.LENGTH_SHORT
            ).show()
            activity.onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        activity.fragment = this
    }

    companion object {
        const val TAG = "graphical notes"
        private const val GRAPHICAL_NOTE_KEY = "graphical note"

        fun newInstance() = EditGraphicalNotes()

        fun newInstance(note: GraphicalNote) = EditGraphicalNotes().apply {
            arguments = Bundle().apply {
                putParcelable(GRAPHICAL_NOTE_KEY, note)
            }
        }
    }
}