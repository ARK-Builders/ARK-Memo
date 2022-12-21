package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import space.taran.arkmemo.R
import space.taran.arkmemo.data.viewmodels.VersionsViewModel
import space.taran.arkmemo.databinding.FragmentVersionsBinding
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.models.Version
import space.taran.arkmemo.ui.activities.MainActivity
import space.taran.arkmemo.ui.adapters.VersionsListAdapter

@AndroidEntryPoint
class VersionsFragment : Fragment(R.layout.fragment_versions) {

    private val binding by viewBinding(FragmentVersionsBinding::bind)

    private val activity: MainActivity by lazy {
        requireActivity() as MainActivity
    }
    private val versionsViewModel: VersionsViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView

    private val versionsListAdapter = VersionsListAdapter()

    private var versionFromArgument: Version? = null
    private var verFromArgumentChanged = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.include.recyclerView
        activity.title = getString(R.string.versions_title)
        activity.showSettingsButton(false)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        if (arguments != null) {
            this.versionFromArgument = requireArguments().getParcelable(VERSION_KEY)
            if (!verFromArgumentChanged) {//this if is needed since onViewCreated its executed again after version rootResourceId changed.
                versionsListAdapter.setVersion(versionFromArgument!!)
                versionsViewModel.setVersion(versionFromArgument!!)
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.apply {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    val layoutManager = LinearLayoutManager(requireContext())
                    versionsListAdapter.setActivity(activity)
                    versionsListAdapter.setFragmentManager(childFragmentManager)
                    recyclerView.apply {
                        this.layoutManager = layoutManager
                    }
                    val notesFlow = versionsViewModel.getAllNotes()
                    val verFlow = versionsViewModel.getVersion()
                    notesFlow.combine(verFlow) { notes, ver ->
                        if (ver == null) {//close fragment:
                            activity.onBackPressed()
                        } else if (ver.meta != null) {
                            versionsListAdapter.setVersion(ver)
                            if (ver.meta.rootResourceId != versionFromArgument!!.meta!!.rootResourceId) {
                                verFromArgumentChanged = true
                            }
                        }
                        versionsListAdapter.setNotes(notes)
                        //Log.d("combine", "ver: "+ver)
                        recyclerView.apply {
                            this.adapter = versionsListAdapter
                        }
                    }.collect()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity.fragment = this
    }

    companion object {
        const val TAG = "Versions Fragment"
        private const val VERSION_KEY = "version key"

        fun newInstance(ver: Version) = VersionsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(VERSION_KEY, ver)
            }
        }
    }
}


fun Fragment.deleteTextNoteFromVersion(note: TextNote) {
    val viewModel: VersionsViewModel by viewModels()
    lifecycleScope.launch {
        viewLifecycleOwner.apply {
            viewModel.deleteNote(note)
        }
    }

}