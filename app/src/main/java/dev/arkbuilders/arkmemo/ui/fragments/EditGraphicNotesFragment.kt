package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentEditNotesBinding
import dev.arkbuilders.arkmemo.models.DEFAULT_TITLE
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.viewmodels.GraphicNotesViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.utils.observeSaveResult

@AndroidEntryPoint
class EditGraphicNotesFragment: Fragment(R.layout.fragment_edit_notes) {

    private val activity by lazy {
        requireActivity() as MainActivity
    }

    private val binding by viewBinding(FragmentEditNotesBinding::bind)

    private val graphicNotesViewModel: GraphicNotesViewModel by viewModels()
    private val notesViewModel: NotesViewModel by activityViewModels()

    private var note = GraphicNote( title = "")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel.init {}
        observeSaveResult(notesViewModel.getSaveNoteResultLiveData())
        if (arguments != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requireArguments().getParcelable(GRAPHICAL_NOTE_KEY, GraphicNote::class.java)?.let {
                    note = it
                    graphicNotesViewModel.updatePathsByNote(note)
                }
            else requireArguments().getParcelable<GraphicNote>(GRAPHICAL_NOTE_KEY)?.let {
                note = it
                graphicNotesViewModel.updatePathsByNote(note)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val defaultTitle = "Graphic $DEFAULT_TITLE"
        var title = note.title
        val notesCanvas = binding.notesCanvas
        val saveButton = binding.saveNote
        val noteTitle = binding.noteTitle
        val noteTitleChangeListener = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                title = s?.toString() ?: ""
            }

            override fun afterTextChanged(s: Editable?) {}

        }

        activity.title = getString(R.string.edit_note)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.showSettingsButton(false)

        noteTitle.hint = defaultTitle
        noteTitle.setText(title)
        noteTitle.addTextChangedListener(noteTitleChangeListener)
        notesCanvas.isVisible = true
        notesCanvas.setViewModel(graphicNotesViewModel)
        saveButton.setOnClickListener {
            val svg = graphicNotesViewModel.svg()
            val note = GraphicNote(
                title = title.ifEmpty { defaultTitle },
                svg = svg,
                resource = note.resource
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