package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentNotesBinding
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.adapters.NotesListAdapter
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.VersionsViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VersionsFragment: Fragment(R.layout.fragment_notes) {

    private val binding by viewBinding(FragmentNotesBinding::bind)
    private val activity by lazy {
        requireActivity() as MainActivity
    }
    private val childFragManager by lazy {
        childFragmentManager
    }

    private val versionsViewModel: VersionsViewModel by activityViewModels()
    private val notesViewModel: NotesViewModel by activityViewModels()

    private lateinit var rvNotes: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        versionsViewModel.init()
        notesViewModel.init {}
        readArguments()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        activity.fragment = this
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                notesViewModel.getNotes {
                    versionsViewModel.getLatestNoteFamilyTree(it) { notes ->
                        val adapter = NotesListAdapter(
                            notes,
                            isLatestNote = { note ->
                                val resourceId = note.resource?.id!!
                                versionsViewModel.isLatestResource(resourceId) ||
                                        versionsViewModel.isNotVersioned(resourceId)
                            }
                        )
                        val layoutManager = LinearLayoutManager(requireContext())
                        adapter.setActivity(activity)
                        adapter.setFragmentManager(childFragManager)
                        adapter.showVersionFork(true)
                        rvNotes.apply {
                            this.adapter = adapter
                            this.layoutManager = layoutManager
                        }
                    }
                }
            }
        }
    }

    private fun initUI() {
        activity.title = getString(R.string.ark_memo_history)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        rvNotes = binding.rvNotesBinding.rvNotes
    }

    private fun readArguments() {
        if (arguments != null) {
            if (SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requireArguments().getParcelable(NOTE_KEY, Note::class.java)?.let {
                    versionsViewModel.updateLatestResourceId(it.resource?.id!!)
                }
            else requireArguments().getParcelable<Note>(NOTE_KEY)?.let {
                versionsViewModel.updateLatestResourceId(it.resource?.id!!)
            }
        }
    }

    companion object {
        const val TAG = "versions-fragment"
        const val NOTE_KEY = "note key"

        fun newInstance(note: Note) = VersionsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(NOTE_KEY, note)
            }
        }
    }
}