package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.view.View
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
import space.taran.arkmemo.databinding.TextNotesBinding
import space.taran.arkmemo.time.MemoCalendar
import space.taran.arkmemo.ui.activities.replaceFragment
import space.taran.arkmemo.ui.activities.showSettingsButton
import space.taran.arkmemo.ui.adapters.TextNotesListAdapter

@AndroidEntryPoint
class TextNotes: Fragment(R.layout.text_notes) {

    private val binding by viewBinding(TextNotesBinding::bind)
    private val activity: AppCompatActivity by lazy {
        requireActivity() as AppCompatActivity
    }
    private val textNotesViewModel: TextNotesViewModel by activityViewModels()

    private lateinit var newNoteButton: FloatingActionButton

    private lateinit var recyclerView: RecyclerView

    private val newNoteClickListener = View.OnClickListener{
        val editTextNotes = EditTextNotes()
        editTextNotes.noteTimeStamp = MemoCalendar.getDateToday()
        (requireActivity() as AppCompatActivity).replaceFragment(editTextNotes, EditTextNotes.TAG)
        (it as FloatingActionButton).hide()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            recyclerView = binding.include.recyclerView
            activity.title = getString(R.string.app_name_debug) //To change before production
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
            newNoteButton = binding.newNote
            newNoteButton.setOnClickListener(newNoteClickListener)
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

    override fun onResume() {
        super.onResume()
        newNoteButton.show()
        showSettingsButton()
    }

    companion object{
        const val TAG = "Text Notes Fragment"
    }
}