package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
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
import kotlinx.coroutines.launch
import space.taran.arkmemo.R
import space.taran.arkmemo.ui.viewmodels.NotesViewModel
import space.taran.arkmemo.databinding.FragmentNotesBinding
import space.taran.arkmemo.models.BaseNote
import space.taran.arkmemo.ui.activities.MainActivity
import space.taran.arkmemo.ui.activities.getTextFromClipBoard
import space.taran.arkmemo.ui.activities.replaceFragment
import space.taran.arkmemo.ui.adapters.NotesListAdapter

@AndroidEntryPoint
class TextNotesFragment: Fragment(R.layout.fragment_notes) {

    private val binding by viewBinding(FragmentNotesBinding::bind)

    private val activity: MainActivity by lazy {
        requireActivity() as MainActivity
    }

    private val notesViewModel: NotesViewModel by activityViewModels()

    private lateinit var newTextNoteButton: FloatingActionButton
    private lateinit var newGraphicNoteButton: FloatingActionButton
    private lateinit var pasteNoteButton: Button

    private lateinit var recyclerView: RecyclerView

    private val newTextNoteClickListener = View.OnClickListener {
        activity.fragment = EditTextNotesFragment()
        activity.replaceFragment(activity.fragment, EditTextNotesFragment.TAG)
    }

    private val newGraphicNoteClickListener = View.OnClickListener{
        activity.fragment = EditGraphicNotesFragment.newInstance()
        activity.replaceFragment(activity.fragment, EditGraphicNotesFragment.TAG)
    }

    private val pasteNoteClickListener = View.OnClickListener {
        val clipBoardText = requireContext().getTextFromClipBoard()
        if (clipBoardText != null) {
            activity.fragment = EditTextNotesFragment.newInstance(clipBoardText)
            activity.replaceFragment(activity.fragment, EditTextNotesFragment.TAG)
        }
        else Toast.makeText(requireContext(), getString(R.string.nothing_to_paste), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel.init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.include.recyclerView
        activity.title = getString(R.string.app_name)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        newTextNoteButton = binding.newNote
        pasteNoteButton = binding.pasteNote
        newGraphicNoteButton = binding.newGraphicNote
        newTextNoteButton.setOnClickListener(newTextNoteClickListener)
        newGraphicNoteButton.setOnClickListener(newGraphicNoteClickListener)
        pasteNoteButton.setOnClickListener(pasteNoteClickListener)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                notesViewModel.getTextNotes {
                    val adapter = NotesListAdapter(it)
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

    override fun onResume() {
        super.onResume()
        activity.fragment = this
    }

    companion object {
        const val TAG = "Text Notes Fragment"
    }
}

fun Fragment.deleteNote(note: BaseNote){
    val viewModel: NotesViewModel by activityViewModels()
    viewModel.onDeleteClick(note)
}