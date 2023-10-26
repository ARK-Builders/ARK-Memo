package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import space.taran.arkmemo.R
import space.taran.arkmemo.ui.viewmodels.GraphicalNotesViewModel
import space.taran.arkmemo.databinding.FragmentEditNotesBinding
import space.taran.arkmemo.models.Content
import space.taran.arkmemo.models.GraphicNote
import space.taran.arkmemo.ui.activities.MainActivity
import space.taran.arkmemo.ui.viewmodels.NotesViewModel

@AndroidEntryPoint
class EditGraphicNotesFragment: Fragment(R.layout.fragment_edit_notes) {

    private val activity by lazy {
        requireActivity() as MainActivity
    }

    private val binding by viewBinding(FragmentEditNotesBinding::bind)

    private val graphicNotesViewModel: GraphicalNotesViewModel by viewModels()
    private val notesViewModel: NotesViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val notesCanvas = binding.notesCanvas
        val saveButton = binding.saveNote

        activity.title = getString(R.string.edit_note)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.showSettingsButton(false)

        notesCanvas.isVisible = true
        notesCanvas.setViewModel(graphicNotesViewModel)
        saveButton.setOnClickListener {
            val svg = graphicNotesViewModel.svg()
            val note = GraphicNote(
                content = Content("", svg.pathData),
                svg = svg
            )
            notesViewModel.onSaveClick(note) { show ->
                activity.showProgressBar(show)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity.fragment = this
    }

    companion object {
        const val TAG = "graphical notes"
        private const val GRAPHICAL_NOTE_KEY = "graphical note"

        fun newInstance() = EditGraphicNotesFragment()

        fun newInstance(note: GraphicNote) = EditGraphicNotesFragment().apply {
            arguments = Bundle().apply {
                putParcelable(GRAPHICAL_NOTE_KEY, note)
            }
        }
    }
}