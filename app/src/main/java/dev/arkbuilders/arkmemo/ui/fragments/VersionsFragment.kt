package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentTextNotesBinding
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.adapters.TextNotesListAdapter
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.VersionsViewModel

@AndroidEntryPoint
class VersionsFragment: Fragment(R.layout.fragment_text_notes) {

    private val binding by viewBinding(FragmentTextNotesBinding::bind)
    private val activity by lazy {
        requireActivity() as MainActivity
    }
    private val childFragManager by lazy {
        childFragmentManager
    }

    private val versionsViewModel: VersionsViewModel by activityViewModels()
    private val notesViewModel: NotesViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        versionsViewModel.init()
        notesViewModel.init()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.title = getString(R.string.ark_memo_history)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView = binding.include.recyclerView
        lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.apply {
                versionsViewModel.collectLatestResourceId { id ->
                    notesViewModel.getNotes { notes ->
                        val latestNoteAndItsParents = notes.filter { note ->
                            note.resource?.id == id ||
                                    versionsViewModel.getParentIds(id).contains(note.resource?.id)
                        }
                        val adapter = TextNotesListAdapter(latestNoteAndItsParents)
                        val layoutManager = LinearLayoutManager(requireContext())
                        adapter.setActivity(activity)
                        adapter.setFragmentManager(childFragManager)
                        adapter.showVersionsFork(true)
                        adapter.showLatestNoteIcon = { note ->
                            val resourceId = note.resource?.id!!
                            versionsViewModel.isLatestResource(resourceId) ||
                                    versionsViewModel.isNotVersioned(resourceId)
                        }
                        recyclerView.apply {
                            this.adapter = adapter
                            this.layoutManager = layoutManager
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