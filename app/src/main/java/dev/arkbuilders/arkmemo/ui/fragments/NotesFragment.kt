package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.databinding.FragmentNotesBinding
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.ui.viewmodels.VersionsViewModel
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.adapters.NotesListAdapter
import dev.arkbuilders.arkmemo.ui.views.toast
import dev.arkbuilders.arkmemo.utils.getTextFromClipBoard
import dev.arkbuilders.arkmemo.utils.replaceFragment

@AndroidEntryPoint
class NotesFragment: Fragment(R.layout.fragment_notes) {

    private val binding by viewBinding(FragmentNotesBinding::bind)

    private val activity: MainActivity by lazy {
        requireActivity() as MainActivity
    }
    private val childFragManager: FragmentManager by lazy {
        childFragmentManager
    }
    private val mainScope = MainScope()
    private val notesViewModel: NotesViewModel by activityViewModels()
    private val versionsViewModel: VersionsViewModel by activityViewModels()

    private lateinit var fabNewText: FloatingActionButton
    private lateinit var fabNewGraphic: FloatingActionButton
    private lateinit var btnPaste: Button
    private lateinit var fabNew: ExtendedFloatingActionButton

    private lateinit var rvNotes: RecyclerView

    private var showFabs = false

    private val fabNewTextClickListener = View.OnClickListener {
        activity.fragment = EditTextNotesFragment()
        activity.replaceFragment(activity.fragment, EditTextNotesFragment.TAG)
        showFabs = false
    }

    private val fabNewGraphicClickListener = View.OnClickListener{
        activity.fragment = EditGraphicNotesFragment.newInstance()
        activity.replaceFragment(activity.fragment, EditGraphicNotesFragment.TAG)
        showFabs = false
    }

    private val btnPasteClickListener = View.OnClickListener {
        val clipBoardText = requireContext().getTextFromClipBoard()
        if (clipBoardText != null) {
            activity.fragment = EditTextNotesFragment.newInstance(clipBoardText)
            activity.replaceFragment(activity.fragment, EditTextNotesFragment.TAG)
            showFabs = false
        }
        else toast(requireContext(), getString(R.string.nothing_to_paste))
    }

    private var showButtons = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel.apply {  init { readAllNotes() } }
        versionsViewModel.init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        activity.fragment = this
        showFabs = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

    private fun initUI() {
        rvNotes = binding.rvNotesBinding.rvNotes
        activity.title = getString(R.string.app_name)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        fabNewText = binding.fabNewText
        btnPaste = binding.fabPaste
        fabNewGraphic = binding.fabNewGraphic
        fabNew = binding.fabNew
        fabNew.isVisible = true
        fabNew.shrink()
        fabNew.setOnClickListener {
            showFabs = if (!showFabs) {
                fabNew.extend()
                fabNewText.show()
                fabNewGraphic.show()
                true
            } else {
                fabNew.shrink()
                fabNewText.hide()
                fabNewGraphic.hide()
                false
            }
        }
        fabNewText.setOnClickListener(fabNewTextClickListener)
        fabNewGraphic.setOnClickListener(fabNewGraphicClickListener)
        btnPaste.isVisible = true
        btnPaste.setOnClickListener(btnPasteClickListener)
        delayHide()
        rvNotes.apply {
            setOnClickListener {
                toggle()
            }
            setOnTouchListener { view1, motionEvent ->
                view1.onTouchEvent(motionEvent)
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> view1.performClick()
                    MotionEvent.ACTION_MOVE -> maintainButtonsVisibility()
                    MotionEvent.ACTION_UP -> delayHide()
                }
                true
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                notesViewModel.getNotes {
                    versionsViewModel.getLatestNotes(it) { notes ->
                        val adapter = NotesListAdapter(notes)
                        val layoutManager = LinearLayoutManager(requireContext())
                        adapter.setActivity(activity)
                        adapter.setFragmentManager(childFragManager)
                        adapter.showVersionTracker(true)
                        rvNotes.apply {
                            this.layoutManager = layoutManager
                            this.adapter = adapter
                        }
                    }
                }
            }
        }
    }

    private fun delayHide() {
        if (showButtons) {
            mainScope.launch {
                delay(DELAY_HIDE)
                hide()
            }
        }
    }

    private fun hide() {
        fabNew.isVisible = false
        btnPaste.isVisible = false
    }

    private fun maintainButtonsVisibility() {
        fabNew.isVisible = showButtons
        btnPaste.isVisible = showButtons
    }

    private fun toggle() {
        fabNew.apply {
            isVisible = !isVisible
        }
        btnPaste.apply {
            isVisible = !isVisible
            showButtons = isVisible
        }
    }

    companion object{
        const val TAG = "text-notes-fragment"
        const val DELAY_HIDE = 10000L
    }
}

fun Fragment.deleteNote(note: Note) {
    val notesViewModel: NotesViewModel by activityViewModels()
    val versionsViewModel: VersionsViewModel by activityViewModels()
    if (
        versionsViewModel.isVersioned(note.resource?.id!!) &&
        versionsViewModel.isLatestResource(note.resource?.id!!)
    )
        lifecycleScope.launch {
            notesViewModel.getNotes {
                versionsViewModel.getLatestNoteFamilyTree(it) { notes ->
                    notes.forEach { note ->
                        notesViewModel.onDeleteConfirmed(note)
                    }
                }
            }
        }
    else notesViewModel.onDeleteConfirmed(note)
    versionsViewModel.onDelete(note.resource?.id!!)
}