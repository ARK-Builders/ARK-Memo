package space.taran.arkmemo.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import space.taran.arkmemo.data.viewmodels.TextNotesViewModel
import space.taran.arkmemo.databinding.FragmentTextNotesBinding
import space.taran.arkmemo.time.MemoCalendar
import space.taran.arkmemo.ui.activities.getTextFromClipBoard
import space.taran.arkmemo.ui.activities.replaceFragment
import space.taran.arkmemo.ui.activities.showSettingsButton
import space.taran.arkmemo.ui.adapters.TextNotesListAdapter

@AndroidEntryPoint
class TextNotes: Fragment(R.layout.fragment_text_notes) {

    private val binding by viewBinding(FragmentTextNotesBinding::bind)
    private val activity: AppCompatActivity by lazy {
        requireActivity() as AppCompatActivity
    }
    private val textNotesViewModel: TextNotesViewModel by activityViewModels()

    private lateinit var newNoteButton: FloatingActionButton
    private lateinit var pasteNoteButton: Button

    private lateinit var recyclerView: RecyclerView

    private val newNoteClickListener = View.OnClickListener{
        val editTextNotes = EditTextNotes()
        editTextNotes.noteDate = MemoCalendar.getDateToday()
        editTextNotes.noteTimeStamp = MemoCalendar.getFullDateToday()
        (requireActivity() as AppCompatActivity).replaceFragment(editTextNotes, EditTextNotes.TAG)
    }

    private val pasteNoteClickListener = View.OnClickListener {
        val clipBoardText = requireContext().getTextFromClipBoard()
        if (clipBoardText != null) {
            val editTextNotes = EditTextNotes(clipBoardText)
            editTextNotes.noteDate = MemoCalendar.getDateToday()
            editTextNotes.noteTimeStamp = MemoCalendar.getFullDateToday()
            activity.replaceFragment(editTextNotes, EditTextNotes.TAG)
        }
        else Toast.makeText(requireContext(), getString(R.string.nothing_to_paste), Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            recyclerView = binding.include.recyclerView
            activity.title = getString(R.string.app_name_debug) //To change before production
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
            newNoteButton = binding.newNote
            pasteNoteButton = binding.pasteNote
            newNoteButton.setOnClickListener(newNoteClickListener)
            pasteNoteButton.setOnClickListener(pasteNoteClickListener)
            lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    textNotesViewModel.getAllTextNotes(requireContext()).collect {
                        val adapter = TextNotesListAdapter(it)
                        val layoutManager = LinearLayoutManager(requireContext())
                        adapter.setActivity(activity)
                        recyclerView.apply {
                            this.layoutManager = layoutManager
                            this.adapter = adapter
                        }
                    }
                }
            }
        }
    }

    companion object{
        const val TAG = "Text Notes Fragment"
    }
}