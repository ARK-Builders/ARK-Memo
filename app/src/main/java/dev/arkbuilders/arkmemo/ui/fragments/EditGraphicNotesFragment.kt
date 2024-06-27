package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.ui.adapters.BrushAdapter
import dev.arkbuilders.arkmemo.ui.adapters.BrushColor
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorBlack
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorBlue
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorGreen
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorGrey
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorOrange
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorPurple
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorRed
import dev.arkbuilders.arkmemo.ui.adapters.BrushSize
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeHuge
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeLarge
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeMedium
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeSmall
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeTiny
import dev.arkbuilders.arkmemo.ui.adapters.EqualSpacingItemDecoration
import dev.arkbuilders.arkmemo.ui.viewmodels.GraphicNotesViewModel
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.observeSaveResult
import dev.arkbuilders.arkmemo.utils.visible

@AndroidEntryPoint
class EditGraphicNotesFragment: BaseEditNoteFragment() {

    private val graphicNotesViewModel: GraphicNotesViewModel by viewModels()
    private var note = GraphicNote()

    private val colorBrushes by lazy {
        listOf(
            BrushColorBlack, BrushColorGrey, BrushColorRed,
            BrushColorOrange, BrushColorGreen, BrushColorBlue, BrushColorPurple)
    }

    private val sizeBrushes by lazy {
        listOf(
            BrushSizeTiny, BrushSizeSmall, BrushSizeMedium,
            BrushSizeLarge, BrushSizeHuge)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel.init {}
        observeSaveResult(notesViewModel.getSaveNoteResultLiveData())
        if (arguments != null) {
            @Suppress("DEPRECATION")
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
                enableSaveText(isContentChanged() && !isContentEmpty())
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
        notesCanvas.setViewModel(graphicNotesViewModel.apply {
            colorBrushes.firstOrNull { it.isSelected }?.let {
                setPaintColor(it)
            }

            sizeBrushes.firstOrNull { it.isSelected }?.let {
                setBrushSize(it)
            }
        })
        btnSave.setOnClickListener {
            val note = createNewNote()
            notesViewModel.onSaveClick(note, parentNote = this.note) { show ->
                hostActivity.showProgressBar(show)
            }
        }
        enableSaveText(false)

        binding.tvLastModified.gone()
        binding.editTextDescription.setText(this.note.description)
        initBottomControls()
        observeDrawEvent()
    }

    private fun observeDrawEvent() {
        graphicNotesViewModel.observableSvgLiveData.observe(viewLifecycleOwner) {
            enableSaveText(it.getPaths().isNotEmpty())
        }
    }

    override fun createNewNote(): Note {
        return GraphicNote(
            title = binding.edtTitle.text.toString(),
            svg = graphicNotesViewModel.svg(),
            description = binding.editTextDescription.text.toString(),
            resource = note.resource
        )
    }

    override fun getCurrentNote(): Note {
        return note
    }

    override fun isContentChanged(): Boolean {
        val originalPaths = note.svg?.getPaths() ?: emptyList()
        val newPaths = graphicNotesViewModel.svg().getPaths()

        return note.title != binding.edtTitle.text.toString()
                || ((newPaths.size != originalPaths.size) || (!newPaths.containsAll(originalPaths)))
    }

    override fun isContentEmpty(): Boolean {
        return graphicNotesViewModel.svg().getPaths().isEmpty()
    }

    private fun initBottomControls() {
        val tvBrushSize = binding.layoutGraphicsControl.tvBrushSize
        tvBrushSize.setOnClickListener {
            tvBrushSize.setSelectState(!tvBrushSize.isSelectedState)
            if (tvBrushSize.isSelectedState) {
                binding.layoutGraphicsControl.layoutSizeChooser.root.visible()
                showBrushSizeList()
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
                showBrushSizeList()
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
                showBrushColorList()
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

    private fun showBrushSizeList() {

        val brushSizeAdapter = BrushAdapter(
            attributes = sizeBrushes.apply {
                val selectedIndex = this.indexOfFirst { it.isSelected }
                if (selectedIndex == -1) {
                    sizeBrushes[0].isSelected = true
                }
            },
            onItemClick = { attribute, pos ->
                Log.v(TAG, "onSizeSelected: " + attribute)
                graphicNotesViewModel.setBrushSize(attribute as BrushSize)
            }
        )

        val layoutMgr = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.layoutGraphicsControl.layoutSizeChooser.rvBrushSizes.apply {
            while (this.itemDecorationCount > 0) {
                this.removeItemDecorationAt(0)
            }
            addItemDecoration(EqualSpacingItemDecoration(context.resources.getDimensionPixelSize(
                R.dimen.brush_size_item_margin), EqualSpacingItemDecoration.HORIZONTAL)
            )
            this.isNestedScrollingEnabled = false
            layoutManager = layoutMgr
            adapter = brushSizeAdapter
        }
    }

    private fun showBrushColorList() {

        val brushColorAdapter = BrushAdapter(
            attributes = colorBrushes.apply {
                val selectedIndex = this.indexOfFirst { it.isSelected }
                if (selectedIndex == -1) {
                    colorBrushes[0].isSelected = true
                }
            },
            onItemClick = { attribute, pos ->
                Log.v(TAG, "onColorSelected: " + attribute)
                graphicNotesViewModel.setPaintColor(attribute as BrushColor)
            }
        )

        val layoutMgr = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.layoutGraphicsControl.layoutColorChooser.rvBrushColors.apply {
            while (this.itemDecorationCount > 0) {
                this.removeItemDecorationAt(0)
            }
            addItemDecoration(EqualSpacingItemDecoration(context.resources.getDimensionPixelSize(
                R.dimen.brush_color_item_margin), EqualSpacingItemDecoration.HORIZONTAL)
            )
            this.isNestedScrollingEnabled = false
            layoutManager = layoutMgr
            adapter = brushColorAdapter
        }
    }

    private fun enableSaveText(enabled: Boolean) {
        binding.toolbar.tvRightActionText.isEnabled = enabled
        if (enabled) {
            binding.toolbar.tvRightActionText.alpha = 1f
        } else {
            binding.toolbar.tvRightActionText.alpha = 0.4f
        }
    }

    companion object {
        const val TAG = "EditGraphicNotesFragment"
        private const val GRAPHICAL_NOTE_KEY = "graphical note"

        fun newInstance() = EditGraphicNotesFragment()

        fun newInstance(note: GraphicNote) = EditGraphicNotesFragment().apply {
            arguments = Bundle().apply {
                putParcelable(GRAPHICAL_NOTE_KEY, note)
            }
        }
    }
}