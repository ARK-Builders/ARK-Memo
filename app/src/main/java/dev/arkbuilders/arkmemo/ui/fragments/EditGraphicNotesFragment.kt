package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.adapters.BrushAdapter
import dev.arkbuilders.arkmemo.ui.adapters.BrushColor
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorBlack
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorBlue
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorGreen
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorGrey
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorOrange
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorPurple
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorRed
import dev.arkbuilders.arkmemo.ui.adapters.BrushSize
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeHuge
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeLarge
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeMedium
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeSmall
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeTiny
import dev.arkbuilders.arkmemo.ui.adapters.EqualSpacingItemDecoration
import dev.arkbuilders.arkmemo.ui.viewmodels.GraphicNotesViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.ui.views.data.Resolution
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.EditScreen
import dev.arkbuilders.arkmemo.utils.getBrushSize
import dev.arkbuilders.arkmemo.utils.getColorCode
import dev.arkbuilders.arkmemo.utils.getParcelableCompat
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.observeSaveResult
import dev.arkbuilders.arkmemo.utils.setDrawableColor
import dev.arkbuilders.arkmemo.utils.visible

@AndroidEntryPoint
class EditGraphicNotesFragment : BaseFragment() {
    val notesViewModel: NotesViewModel by activityViewModels()
    private val graphicNotesViewModel: GraphicNotesViewModel by viewModels()
    private var note = GraphicNote()
    val hostActivity by lazy { activity as MainActivity }

    override fun onBackPressed() {
        parentFragmentManager.popBackStack()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.getParcelableCompat(GRAPHICAL_NOTE_KEY, GraphicNote::class.java)?.let {
//            note = it
//            graphicNotesViewModel.onNoteOpened(note)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                EditScreen(
                    imagePath = null,
                    imageUri = null,
                    fragmentManager = parentFragmentManager,
                    navigateBack = { onBackPressed() },
                    launchedFromIntent = true,
                    maxResolution = Resolution(120, 120)
                )
            }
        }
    }

    companion object {
        const val TAG = "EditGraphicNotesFragment"
        private const val GRAPHICAL_NOTE_KEY = "graphical note"

        fun newInstance() = EditGraphicNotesFragment()

        fun newInstance(note: GraphicNote) = EditGraphicNotesFragment().apply {
            arguments = Bundle().apply {
                putParcelable(GRAPHICAL_NOTE_KEY, note)
            }
        }
    }
}