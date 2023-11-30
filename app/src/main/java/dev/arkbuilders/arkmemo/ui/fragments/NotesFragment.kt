package dev.arkbuilders.arkmemo.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.databinding.FragmentTextNotesBinding
import dev.arkbuilders.arkmemo.data.models.TextNote
import space.taran.arkmemo.data.viewmodels.VersionsViewModel
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.activities.getTextFromClipBoard
import dev.arkbuilders.arkmemo.ui.activities.replaceFragment
import dev.arkbuilders.arkmemo.ui.adapters.TextNotesListAdapter

@AndroidEntryPoint
class TextNotesFragment: Fragment(R.layout.fragment_text_notes) {

    private val binding by viewBinding(FragmentTextNotesBinding::bind)

    private val activity: MainActivity by lazy {
        requireActivity() as MainActivity
    }
    private val mainScope = MainScope()
    private val textNotesViewModel: TextNotesViewModel by activityViewModels()
    private val versionsViewModel: VersionsViewModel by activityViewModels()

    private lateinit var newNoteButton: FloatingActionButton
    private lateinit var pasteNoteButton: Button

    private lateinit var recyclerView: RecyclerView

    private val newNoteClickListener = View.OnClickListener {
        activity.fragment = EditTextNotesFragment()
        activity.replaceFragment(activity.fragment, EditTextNotesFragment.TAG)
    }

    private val pasteNoteClickListener = View.OnClickListener {
        val clipBoardText = requireContext().getTextFromClipBoard()
        if (clipBoardText != null) {
            activity.fragment = EditTextNotesFragment.newInstance(clipBoardText)
            activity.replaceFragment(activity.fragment, EditTextNotesFragment.TAG)
        }
        else Toast.makeText(requireContext(), getString(R.string.nothing_to_paste), Toast.LENGTH_SHORT).show()
    }

    private var showButtons = false

    private fun delayHide() {
        if (showButtons) {
            mainScope.launch {
                delay(DELAY_HIDE)
                hide()
            }
        }
    }

    private fun hide() {
        newNoteButton.isVisible = false
        pasteNoteButton.isVisible = false
    }

    private fun maintainButtonsVisibility() {
        newNoteButton.isVisible = showButtons
        pasteNoteButton.isVisible = showButtons
    }

    private fun toggle() {
        newNoteButton.apply {
            isVisible = !isVisible
        }
        pasteNoteButton.apply {
            isVisible = !isVisible
            showButtons = isVisible
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        lifecycleScope.launch {
            versionsViewModel.init()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textNotesViewModel.init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.include.recyclerView
        activity.title = getString(R.string.app_name)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        newNoteButton = binding.newNote
        pasteNoteButton = binding.pasteNote
        newNoteButton.apply {
            isVisible = true
            setOnClickListener(newNoteClickListener)
        }
        pasteNoteButton.apply {
            isVisible = true
            setOnClickListener(pasteNoteClickListener)
        }

        delayHide()
        recyclerView.apply {
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

        lifecycleScope.launch {
            viewLifecycleOwner.apply {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    textNotesViewModel.collectAllNotes {
                        val latestNotes = it.filter { note ->
                            versionsViewModel
                                .getLatestVersions().contains(note.meta?.id) ||
                                    versionsViewModel.isNotVersionedYet(note)
                        }
                        val adapter = TextNotesListAdapter(latestNotes)
                        val layoutManager = LinearLayoutManager(requireContext())
                        adapter.setActivity(activity)
                        adapter.setFragmentManager(childFragmentManager)
                        adapter.showVersionsTracker(true)
                        recyclerView.apply {
                            this.layoutManager = layoutManager
                            this.adapter = adapter
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity.fragment = this
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

    companion object{
        const val TAG = "text-notes-fragment"
        const val DELAY_HIDE = 10000L
    }
}

fun Fragment.deleteTextNote(note: TextNote){
    val viewModel: TextNotesViewModel by activityViewModels()
    val versionsViewModel: VersionsViewModel by activityViewModels()
    if (
        versionsViewModel.isVersioned(note) &&
        versionsViewModel.isLatestVersion(note)
    )
        lifecycleScope.launch {
            viewModel.collectAllNotes {
                it.filter { note1 ->
                    note.meta?.id == note1.meta?.id ||
                            versionsViewModel.getNoteParentsFromVersions(note)
                                .contains(note1.meta?.id!!)
                }
                    .forEach { note2 ->
                        viewModel.deleteNote(note2)
                    }
            }
        }
    else viewModel.deleteNote(note)
    versionsViewModel.forgetNoteFromVersions(note)
}