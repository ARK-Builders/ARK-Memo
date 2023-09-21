package space.taran.arkmemo.ui.fragments

import android.content.Context
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import space.taran.arkmemo.R
import space.taran.arkmemo.data.models.TextNote
import space.taran.arkmemo.data.viewmodels.TextNotesViewModel
import space.taran.arkmemo.data.viewmodels.VersionsViewModel
import space.taran.arkmemo.databinding.FragmentTextNotesBinding
import space.taran.arkmemo.ui.activities.MainActivity
import space.taran.arkmemo.ui.adapters.TextNotesListAdapter

@AndroidEntryPoint
class TextNoteVersionsFragment: Fragment(R.layout.fragment_text_notes) {

    private val binding by viewBinding(FragmentTextNotesBinding::bind)
    private val activity by lazy {
        requireActivity() as MainActivity
    }

    private val versionsViewModel: VersionsViewModel by activityViewModels()
    private val textNotesViewModel: TextNotesViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        lifecycleScope.launch {
            versionsViewModel.init()
        }
        if (arguments != null) {
            val note = if (SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requireArguments().getParcelable(NOTE_KEY, TextNote::class.java)
            else requireArguments().getParcelable(NOTE_KEY)
            versionsViewModel.emitLatestVersionNoteId(note?.meta?.id!!)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.title = getString(R.string.ark_memo_history)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView = binding.include.recyclerView
        lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.apply {
                versionsViewModel.collectLatestVersionNoteId { latestNoteId ->
                    textNotesViewModel.collectAllNotes { notes ->
                        val latestNote = notes.find { it.meta?.id == latestNoteId }
                        val latestNoteAndItsParents = notes.filter { note ->
                            note.meta?.id == latestNoteId ||
                                    versionsViewModel.getNoteParentsFromVersions(
                                        latestNote!!
                                    )
                                        .contains(note.meta?.id)
                        }
                        val adapter = TextNotesListAdapter(latestNoteAndItsParents)
                        val layoutManager = LinearLayoutManager(requireContext())
                        adapter.setActivity(activity)
                        adapter.setFragmentManager(childFragmentManager)
                        adapter.showVersionsFork(true)
                        adapter.showLatestNoteIcon = { note ->
                            versionsViewModel.isLatestVersion(note) ||
                                    versionsViewModel.isNotVersionedYet(note)
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

        fun newInstance(note: TextNote) = TextNoteVersionsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(NOTE_KEY, note)
            }
        }
    }
}