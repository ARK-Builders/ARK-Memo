package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import space.taran.arkmemo.R
import space.taran.arkmemo.data.viewmodels.TextNotesViewModel
import space.taran.arkmemo.databinding.FragmentTextNotesBinding
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.ui.activities.MainActivity
import space.taran.arkmemo.ui.activities.getTextFromClipBoard
import space.taran.arkmemo.ui.activities.replaceFragment
import space.taran.arkmemo.ui.adapters.TextNotesListAdapter

@AndroidEntryPoint
class TextNotesFragment: Fragment(R.layout.fragment_text_notes) {

    private val binding by viewBinding(FragmentTextNotesBinding::bind)

    private val activity: MainActivity by lazy {
        requireActivity() as MainActivity
    }
    private val textNotesViewModel: TextNotesViewModel by viewModels()

    private lateinit var newNoteButton: FloatingActionButton
    private lateinit var pasteNoteButton: Button

    private lateinit var recyclerView: RecyclerView

    private val newNoteClickListener = View.OnClickListener{
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.include.recyclerView
        activity.title = getString(R.string.app_name)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        newNoteButton = binding.newNote
        pasteNoteButton = binding.pasteNote
        newNoteButton.setOnClickListener(newNoteClickListener)
        pasteNoteButton.setOnClickListener(pasteNoteClickListener)
        lifecycleScope.launch {
            viewLifecycleOwner.apply{
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    textNotesViewModel.getAllNotes().collect {
                        val adapter = TextNotesListAdapter(it)
                        val layoutManager = LinearLayoutManager(requireContext())
                        adapter.setActivity(activity)
                        adapter.setFragmentManager(childFragmentManager)
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

    companion object{
        const val TAG = "Text Notes Fragment"
    }
}

fun Fragment.deleteTextNote(note: TextNote){
    val viewModel: TextNotesViewModel by viewModels()
    viewModel.deleteNote(note)
}