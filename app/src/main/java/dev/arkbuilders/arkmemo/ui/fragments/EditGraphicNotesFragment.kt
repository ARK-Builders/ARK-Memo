package dev.arkbuilders.arkmemo.ui.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentEditNotesBinding
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.viewmodels.GraphicNotesViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.VersionsViewModel
import dev.arkbuilders.arkmemo.ui.views.NotesCanvas
import dev.arkbuilders.arkmemo.utils.observeSaveResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class EditGraphicNotesFragment: Fragment(R.layout.fragment_edit_notes) {

    private val activity by lazy {
        requireActivity() as MainActivity
    }

    private val binding by viewBinding(FragmentEditNotesBinding::bind)

    private lateinit var btnSave: Button
    private lateinit var notesCanvas: NotesCanvas
    private lateinit var etTitle: EditText

    private val graphicNotesViewModel: GraphicNotesViewModel by viewModels()
    private val notesViewModel: NotesViewModel by activityViewModels()
    private val versionsViewModel: VersionsViewModel by activityViewModels()

    private var note = GraphicNote()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel.init {}
        observeSaveResult(notesViewModel.getSaveNoteResultLiveData())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.initEditUI()
        initUI()
        readArguments()
    }

    override fun onResume() {
        super.onResume()
        activity.fragment = this
    }

    private fun initUI() {
        val defaultTitle = getString(
            R.string.ark_memo_graphic_note,
            LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        )
        var title = note.title
        val noteTitleChangeListener = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                title = s?.toString() ?: ""
            }

            override fun afterTextChanged(s: Editable?) {}

        }
        notesCanvas = binding.notesCanvas
        btnSave = binding.saveNote
        etTitle = binding.noteTitle

        etTitle.hint = defaultTitle
        etTitle.setText(title)
        etTitle.addTextChangedListener(noteTitleChangeListener)
        notesCanvas.isVisible = true
        notesCanvas.setViewModel(graphicNotesViewModel)
        btnSave.apply {
            if (isVisible) {
                setOnClickListener {
                    val svg = graphicNotesViewModel.svg()
                    val note = GraphicNote(
                        title = title.ifEmpty { defaultTitle },
                        svg = svg,
                        resource = note.resource
                    )
                    notesViewModel.onSaveClick(
                        note,
                        showProgress = { show ->
                            activity.showProgressBar(show)
                        },
                        saveVersion = { oldId, newId ->
                            versionsViewModel.createVersion(oldId, newId)
                            versionsViewModel.updateLatestResourceId(newId)
                        }
                    )
                }
            }
        }
    }

    private fun readArguments() {
        if (arguments != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requireArguments().getParcelable(GRAPHIC_NOTE_KEY, GraphicNote::class.java)?.let {
                    note = it
                    graphicNotesViewModel.onNoteOpened(note)
                    checkNoteForReadOnly()
                }
            else requireArguments().getParcelable<GraphicNote>(GRAPHIC_NOTE_KEY)?.let {
                note = it
                graphicNotesViewModel.onNoteOpened(note)
                checkNoteForReadOnly()
            }
        }
    }

    private fun checkNoteForReadOnly() {
        val resourceId = note.resource?.id!!
        val isReadOnly = versionsViewModel.isVersioned(resourceId) &&
                !versionsViewModel.isLatestResource(resourceId) && !note.isForked
        if (isReadOnly) {
            activity.title = getString(R.string.ark_memo_old_version)
            notesCanvas.disableDrawing = true
            etTitle.isClickable = false
            etTitle.isFocusable = false
            etTitle.setBackgroundColor(Color.LTGRAY)
        }
        btnSave.isVisible = !isReadOnly
    }

    companion object {
        const val TAG = "graphic-notes-fragment"
        private const val GRAPHIC_NOTE_KEY = "graphic note"

        fun newInstance() = EditGraphicNotesFragment()

        fun newInstance(note: GraphicNote) = EditGraphicNotesFragment().apply {
            arguments = Bundle().apply {
                putParcelable(GRAPHIC_NOTE_KEY, note)
            }
        }
    }
}