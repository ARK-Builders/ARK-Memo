package dev.arkbuilders.arkmemo.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentHomeBinding
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.adapters.NotesListAdapter
import dev.arkbuilders.arkmemo.ui.dialogs.CommonActionDialog
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.GraphicNotesViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.ui.views.toast
import dev.arkbuilders.arkmemo.utils.getTextFromClipBoard
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.replaceFragment
import dev.arkbuilders.arkmemo.utils.visible


@AndroidEntryPoint
class NotesFragment: Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val activity: MainActivity by lazy {
        requireActivity() as MainActivity
    }

    private val notesViewModel: NotesViewModel by activityViewModels()
    private val arkMediaPlayerViewModel: ArkMediaPlayerViewModel by activityViewModels()
    private val graphicNotesViewModel: GraphicNotesViewModel by viewModels()

    private var notes = listOf<Note>()

    private var showingFloatingButtons = false

    private val newTextNoteClickListener = View.OnClickListener {
        onFloatingActionButtonClicked()
    }

    private val pasteNoteClickListener = View.OnClickListener {
        val clipBoardText = requireContext().getTextFromClipBoard()
        if (clipBoardText != null) {
            activity.fragment = EditTextNotesFragment.newInstance(clipBoardText)
            activity.replaceFragment(activity.fragment, EditTextNotesFragment.TAG)
        }
        else Toast.makeText(requireContext(), getString(R.string.nothing_to_paste), Toast.LENGTH_SHORT).show()
    }

    private val mItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder,
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val deletePosition = viewHolder.bindingAdapterPosition
            val noteToDelete = notes[deletePosition].apply { pendingForDelete = true }
            binding.rvPinnedNotes.adapter?.notifyItemChanged(deletePosition)

            CommonActionDialog(title = R.string.delete_note,
                message = R.string.ark_memo_delete_warn,
                positiveText = R.string.action_delete,
                negativeText = R.string.ark_memo_cancel,
                isAlert = true,
                onPositiveClick = {
                    notesViewModel.onDeleteConfirmed(noteToDelete)
                    toast(requireContext(), getString(R.string.note_deleted))
                    binding.rvPinnedNotes.adapter?.notifyItemRemoved(deletePosition)
            }, onNegativeClicked = {
                    noteToDelete.pendingForDelete = false
                    binding.rvPinnedNotes.adapter?.notifyItemChanged(deletePosition)
            }, onCloseClicked = {
                    noteToDelete.pendingForDelete = false
                    binding.rvPinnedNotes.adapter?.notifyItemChanged(deletePosition)
            }).show(parentFragmentManager, CommonActionDialog.TAG)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentHomeBinding.inflate(layoutInflater)
        notesViewModel.apply {  init { readAllNotes() } }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.title = getString(R.string.app_name)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        binding.ivSettings.setOnClickListener {
            activity.fragment = SettingFragmentsV2()
            activity.replaceFragment(activity.fragment, SettingFragmentsV2::class.java.name)
        }
        showingFloatingButtons = false
        initBottomControlViews()
        lifecycleScope.launchWhenStarted {
            notesViewModel.getNotes {
                notes = it
                val adapter = NotesListAdapter(
                    it,
                    onPlayPauseClick = { path ->
                        arkMediaPlayerViewModel.onPlayOrPauseClick(path)
                    },
                    onThumbPrepare = { graphicNote, noteCanvas ->
//                        val tempNoteViewModel: GraphicNotesViewModel by viewModels()
//                        noteCanvas.setViewModel(viewModel = /*graphicNotesViewModel*/tempNoteViewModel)
//                        tempNoteViewModel.onNoteOpened(graphicNote)
//                        noteCanvas.invalidate()

                    }
                )
                val layoutManager = LinearLayoutManager(requireContext())
                arkMediaPlayerViewModel.collect(
                    stateToUI = { state -> adapter.observeItemState = { state } },
                    handleSideEffect = { effect -> adapter.observeItemSideEffect = { effect } }
                )
                adapter.setActivity(activity)
                adapter.setFragmentManager(childFragmentManager)
                binding.rvPinnedNotes.apply {
                    this.layoutManager = layoutManager
                    this.adapter = adapter
                }
                ItemTouchHelper(mItemTouchCallback).attachToRecyclerView(binding.rvPinnedNotes)

                if (it.isNotEmpty()) {
                    binding.layoutBottomControl.visible()
                    binding.groupEmptyState.gone()
                    binding.rvPinnedNotes.visible()
                    binding.edtSearch.visible()
                    binding.scrollViewNotes.visible()
                } else {
                    binding.layoutBottomControl.gone()
                    binding.groupEmptyState.visible()
                    binding.rvPinnedNotes.gone()
                    binding.edtSearch.gone()
                    binding.scrollViewNotes.gone()
                }
            }
        }

        initEmptyStateViews()
    }

    override fun onResume() {
        super.onResume()
        activity.fragment = this
        observeClipboardContent()
    }

    private fun createTextNote() {
        activity.fragment = EditTextNotesFragment()
        activity.replaceFragment(activity.fragment, EditTextNotesFragment.TAG)
    }

    private fun createVoiceNote() {
        activity.fragment = ArkRecorderFragment.newInstance()
        activity.replaceFragment(activity.fragment, ArkRecorderFragment.TAG)
    }

    private fun createGraphicNote() {
        activity.fragment = EditGraphicNotesFragment.newInstance()
        activity.replaceFragment(activity.fragment, EditGraphicNotesFragment.TAG)
    }

    private fun initEmptyStateViews() {
        binding.tvNewMemo.setOnClickListener {
            createTextNote()
        }
        binding.ivRecord.setOnClickListener {
            createVoiceNote()
        }
        binding.ivDraw.setOnClickListener {
            createGraphicNote()
        }
        binding.tvPaste.setOnClickListener(pasteNoteClickListener)
        binding.tvInstructions.setOnClickListener {
        }

    }

    private fun observeClipboardContent() {
        context?.getTextFromClipBoard()?.let {
            binding.tvPaste.alpha = 1f
            binding.tvPaste.isClickable = true
        } ?: let {
            binding.tvPaste.alpha = 0.4f
            binding.tvPaste.isClickable = false
        }
    }

    private fun initBottomControlViews() {
        binding.fabNewAction.setOnClickListener(newTextNoteClickListener)
        binding.fabSimpleMemo.setOnClickListener {
            onFloatingActionButtonClicked()
            createTextNote()
        }
        binding.fabRecordVoice.setOnClickListener {
            createVoiceNote()
        }
        binding.fabDraw.setOnClickListener {
            createGraphicNote()
        }
        binding.tvBottomPanelPaste.setOnClickListener(pasteNoteClickListener)
    }

    private fun onFloatingActionButtonClicked() {
        if (showingFloatingButtons) {
            binding.fabNewAction.shrink()
            binding.fabNewAction.icon = ContextCompat.getDrawable(activity, R.drawable.ic_add)
            binding.fabNewAction.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(activity, R.color.warning)
            )
            binding.groupFabActions.gone()
            showingFloatingButtons = false
        } else {
            binding.fabNewAction.extend()
            binding.fabNewAction.icon = ContextCompat.getDrawable(activity, R.drawable.ic_close)
            binding.fabNewAction.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(activity, R.color.warning_100)
            )
            binding.groupFabActions.visible()
            showingFloatingButtons = true
        }
    }

    companion object {
        const val TAG = "NotesFragment"
    }
}
