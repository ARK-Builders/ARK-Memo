package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
        var title = ""
        val notesCanvas = binding.notesCanvas
        val saveButton = binding.saveNote
        val noteTitle = binding.noteTitle
        val noteTitleChangeListener = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                title = s?.toString() ?: ""
                saveButton.isEnabled = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}

        }

        activity.title = getString(R.string.edit_note)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.showSettingsButton(false)

        if (arguments != null) {
            requireArguments().getParcelable<GraphicNote>(GRAPHICAL_NOTE_KEY)?.let {
                title = it.title
            }
        }

        noteTitle.setText(title)
        noteTitle.addTextChangedListener(noteTitleChangeListener)
        notesCanvas.isVisible = true
        notesCanvas.setViewModel(graphicNotesViewModel)
        saveButton.setOnClickListener {
            val svg = graphicNotesViewModel.svg()
            val note = GraphicNote(
                title,
                content = Content(svg.pathData),
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