package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import space.taran.arkmemo.data.viewmodels.TextNotesViewModel
import space.taran.arkmemo.databinding.TextNotesBinding
import space.taran.arkmemo.ui.adapters.TextNotesListAdapter

@AndroidEntryPoint
class TextNotes: Fragment() {

    private var _binding: TextNotesBinding? = null
    private val binding get() = _binding!!
    private val textNotesViewModel: TextNotesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TextNotesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(requireContext())
        val recyclerView = binding.include.recyclerView
        val adapter = TextNotesListAdapter(textNotesViewModel.getAllNotes())

        recyclerView.apply{
            this.layoutManager = layoutManager
            this.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val TAG = "Text Notes Fragment"
    }
}