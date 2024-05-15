package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.ui.viewmodels.GraphicNotesViewModel
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.observeSaveResult
import dev.arkbuilders.arkmemo.utils.visible

@AndroidEntryPoint
class EditGraphicNotesFragment: BaseEditNoteFragment() {

    private val graphicNotesViewModel: GraphicNotesViewModel by viewModels()

    private var note = GraphicNote()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel.init {}
        observeSaveResult(notesViewModel.getSaveNoteResultLiveData())
        if (arguments != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requireArguments().getParcelable(GRAPHICAL_NOTE_KEY, GraphicNote::class.java)?.let {
                    note = it
                    graphicNotesViewModel.onNoteOpened(note)
                }
            else requireArguments().getParcelable<GraphicNote>(GRAPHICAL_NOTE_KEY)?.let {
                note = it
                graphicNotesViewModel.onNoteOpened(note)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var title = note.title
        val notesCanvas = binding.notesCanvas
        val btnSave = binding.toolbar.tvRightActionText
        val noteTitle = binding.edtTitle
        val noteTitleChangeListener = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                title = s?.toString() ?: ""
                if (title.isEmpty()) {
                    binding.edtTitle.hint = getString(R.string.hint_new_graphical_note)
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        }

        hostActivity.title = getString(R.string.edit_note)
        hostActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        hostActivity.showSettingsButton(false)

        noteTitle.hint = getString(R.string.hint_new_graphical_note)
        noteTitle.setText(title)
        noteTitle.addTextChangedListener(noteTitleChangeListener)
        notesCanvas.isVisible = true
        notesCanvas.setViewModel(graphicNotesViewModel)
        btnSave.setOnClickListener {
            val note = createNewNote()
            notesViewModel.onSaveClick(note) { show ->
                hostActivity.showProgressBar(show)
            }
        }

        binding.tvLastModified.gone()
        binding.editTextDescription.setText(this.note.description)
        initBottomControls()

    }

    override fun createNewNote(): Note {
        return GraphicNote(
            title = binding.edtTitle.text.toString(),
            svg = graphicNotesViewModel.svg(),
            description = binding.editTextDescription.text.toString(),
            resource = note.resource
        )
    }

    private fun initBottomControls() {
        val tvBrushSize = binding.layoutGraphicsControl.tvBrushSize
        tvBrushSize.setOnClickListener {
            tvBrushSize.setSelectState(!tvBrushSize.isSelectedState)
            if (tvBrushSize.isSelectedState) {
                binding.layoutGraphicsControl.layoutSizeChooser.root.visible()
                binding.layoutGraphicsControl.layoutColorChooser.root.gone()
                binding.layoutGraphicsControl.tvEraser.setSelectState(false)
                binding.layoutGraphicsControl.tvBrushColor.setSelectState(false)
            } else {
                binding.layoutGraphicsControl.layoutSizeChooser.root.gone()
            }
        }

        val tvEraser = binding.layoutGraphicsControl.tvEraser
        tvEraser.setOnClickListener {
            tvEraser.setSelectState(!tvEraser.isSelectedState)
            if (tvEraser.isSelectedState) {
                binding.layoutGraphicsControl.layoutSizeChooser.root.visible()
                binding.layoutGraphicsControl.layoutColorChooser.root.gone()
                binding.layoutGraphicsControl.tvBrushSize.setSelectState(false)
                binding.layoutGraphicsControl.tvBrushColor.setSelectState(false)
            } else {
                binding.layoutGraphicsControl.layoutSizeChooser.root.gone()
            }
        }

        val tvColor = binding.layoutGraphicsControl.tvBrushColor
        tvColor.setOnClickListener {
            tvColor.setSelectState(!tvColor.isSelectedState)
            if (tvColor.isSelectedState) {
                binding.layoutGraphicsControl.layoutColorChooser.root.visible()
                binding.layoutGraphicsControl.layoutSizeChooser.root.gone()
                binding.layoutGraphicsControl.tvBrushSize.setSelectState(false)
                binding.layoutGraphicsControl.tvEraser.setSelectState(false)
            } else {
                binding.layoutGraphicsControl.layoutColorChooser.root.gone()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hostActivity.fragment = this
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