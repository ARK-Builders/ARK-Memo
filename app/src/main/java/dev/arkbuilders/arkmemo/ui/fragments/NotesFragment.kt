package dev.arkbuilders.arkmemo.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentHomeBinding
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.models.VoiceNote
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.adapters.NotesListAdapter
import dev.arkbuilders.arkmemo.ui.dialogs.CommonActionDialog
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerSideEffect
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.ui.views.toast
import dev.arkbuilders.arkmemo.utils.getTextFromClipBoard
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.replaceFragment
import dev.arkbuilders.arkmemo.utils.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotesFragment : BaseFragment() {
    private lateinit var binding: FragmentHomeBinding

    private val activity: MainActivity by lazy {
        requireActivity() as MainActivity
    }

    private val notesViewModel: NotesViewModel by activityViewModels()
    private val arkMediaPlayerViewModel: ArkMediaPlayerViewModel by activityViewModels()
    private var notesAdapter: NotesListAdapter? = null

    private var showingFloatingButtons = false
    private var playingAudioPath: String? = null
    private var playingAudioPosition = -1
    private var lastNoteItemPosition = 0

    private var mIsActionMode = false
    private var selectedCountForDelete = 0

    private val newTextNoteClickListener =
        View.OnClickListener {
            onFloatingActionButtonClicked()
        }

    private val pasteNoteClickListener =
        View.OnClickListener {
            requireContext().getTextFromClipBoard(view) { clipBoardText ->
                if (clipBoardText != null) {
                    activity.fragment = EditTextNotesFragment.newInstance(clipBoardText)
                    activity.replaceFragment(activity.fragment, EditTextNotesFragment.TAG)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.nothing_to_paste),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }

    private var mItemTouchHelper: ItemTouchHelper? = null
    private val mItemTouchCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int,
            ) {
                val deletePosition = viewHolder.bindingAdapterPosition
                val noteToDelete =
                    notesAdapter?.getNotes()?.getOrNull(deletePosition)?.apply {
                        pendingForDelete = true
                    } ?: return
                val noteViewHolder = viewHolder as? NotesListAdapter.NoteViewHolder
                noteViewHolder?.isSwiping = true
                binding.rvPinnedNotes.adapter?.notifyItemChanged(deletePosition)

                CommonActionDialog(
                    title = getString(R.string.delete_note),
                    message = resources.getQuantityString(R.plurals.delete_batch_note_message, 1),
                    positiveText = R.string.action_delete,
                    negativeText = R.string.ark_memo_cancel,
                    isAlert = true,
                    onPositiveClick = {
                        noteViewHolder?.isSwiping = false
                        notesViewModel.onDeleteConfirmed(listOf(noteToDelete)) {
                            notesAdapter?.removeNote(noteToDelete)
                            toast(requireContext(), getString(R.string.note_deleted))
                            binding.rvPinnedNotes.adapter?.notifyItemRemoved(deletePosition)
                            checkForEmptyState()
                        }
                    },
                    onNegativeClicked = {
                        noteViewHolder?.isSwiping = false
                        noteToDelete.pendingForDelete = false
                        binding.rvPinnedNotes.adapter?.notifyItemChanged(deletePosition)
                    },
                    onCloseClicked = {
                        noteViewHolder?.isSwiping = false
                        noteToDelete.pendingForDelete = false
                        binding.rvPinnedNotes.adapter?.notifyItemChanged(deletePosition)
                    },
                ).show(childFragmentManager, CommonActionDialog.TAG)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        activity.title = getString(R.string.app_name)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        binding.ivSettings.setOnClickListener {
            activity.fragment = SettingsFragment()
            activity.replaceFragment(activity.fragment, SettingsFragment::class.java.name)
        }
        showingFloatingButtons = false
        initBottomControlViews()
        initEmptyStateViews()

        binding.pbLoading.visible()
        notesViewModel.apply {
            init {
                readAllNotes {
                    onNotesLoaded(it)
                }
            }
        }
        initSearch()
    }

    private fun initSearch() {
        binding.edtSearch.addTextChangedListener(
            onTextChanged = { text, _, _, _ ->
                binding.pbLoading.visible()
                binding.groupSearchResultEmpty.gone()

                if (!binding.edtSearch.isFocused && text.isNullOrEmpty()) return@addTextChangedListener

                notesViewModel.searchNote(keyword = text.toString()) { notes ->
                    binding.pbLoading.gone()
                    if (notes.isEmpty()) {
                        binding.groupSearchResultEmpty.visible()
                    } else {
                        binding.groupSearchResultEmpty.gone()
                    }
                    notesAdapter?.updateData(notes, fromSearch = true, keyword = text.toString())

                    // When search text is cleared, restore previous note item position in the list
                    if (text.toString().isEmpty()) {
                        binding.rvPinnedNotes.layoutManager?.scrollToPosition(lastNoteItemPosition)
                    }
                }
            },
        )

        binding.edtSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                lastNoteItemPosition = getCurrentScrollPosition()
            }
        }
    }

    private fun onNotesLoaded(notes: List<Note>) {
        binding.pbLoading.gone()
        if (notesAdapter == null) {
            notesAdapter =
                NotesListAdapter(
                    notes.toMutableList(),
                    onPlayPauseClick = { path, pos, onStop ->
                        playingAudioPath = path
                        if (playingAudioPosition >= 0) {
                            refreshNoteItem(playingAudioPosition)
                        }

                        if (playingAudioPosition >= 0 && playingAudioPosition != pos) {
                            // Another Voice note is being played compared to the previously played one
                            markResetVoiceNotePlayback(playingAudioPosition)
                        }

                        playingAudioPosition = pos ?: -1

                        if (arkMediaPlayerViewModel.isPlaying()) {
                            mItemTouchHelper?.attachToRecyclerView(binding.rvPinnedNotes)
                            markWaitToBeResumed(playingAudioPosition)
                        } else {
                            mItemTouchHelper?.attachToRecyclerView(null)
                        }

                        arkMediaPlayerViewModel.onPlayOrPauseClick(path, pos, onStop)
                    },
                )
            observeSelectedNoteForDelete()
        } else {
            notesAdapter?.setNotes(notes)
        }

        val layoutManager = LinearLayoutManager(requireContext())
        observePlayerState()
        observePlayerSideEffect()
        notesAdapter?.setActivity(activity)
        notesAdapter?.onItemLongPressed = { pos, note ->
            toggleActionMode(pos = pos)
        }
        notesAdapter?.onItemClicked = {
            if (mIsActionMode) {
                toggleActionMode()
            }
        }
        binding.rvPinnedNotes.apply {
            this.layoutManager = layoutManager
            this.adapter = notesAdapter
            this.itemAnimator =
                object : DefaultItemAnimator() {
                    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                        val isSwiping = (viewHolder as? NotesListAdapter.NoteViewHolder)?.isSwiping ?: false
                        return !isSwiping
                    }
                }
        }
        mItemTouchHelper = ItemTouchHelper(mItemTouchCallback)
        mItemTouchHelper?.attachToRecyclerView(binding.rvPinnedNotes)

        showEmptyState(isEmpty = notes.isEmpty())
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.layoutBottomControl.gone()
            binding.groupEmptyState.visible()
            binding.rvPinnedNotes.gone()
            binding.edtSearch.gone()
        } else {
            binding.layoutBottomControl.visible()
            binding.groupEmptyState.gone()
            binding.rvPinnedNotes.visible()
            binding.edtSearch.visible()
        }
    }

    private fun markResetVoiceNotePlayback(pos: Int) {
        (notesAdapter?.getNotes()?.getOrNull(pos) as? VoiceNote)?.pendingForPlaybackReset = true
    }

    private fun markWaitToBeResumed(pos: Int) {
        (notesAdapter?.getNotes()?.getOrNull(pos) as? VoiceNote)?.waitToBeResumed = true
    }

    private fun refreshNoteItem(position: Int) {
        notesAdapter?.notifyItemChanged(position)
    }

    private fun observePlayerState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                arkMediaPlayerViewModel.playerState.collectLatest { state ->
                    state ?: return@collectLatest
                    notesAdapter?.observeItemState = { state }
                    notesAdapter?.getNotes()?.getOrNull(playingAudioPosition)?.let {
                        (it as? VoiceNote)?.currentPlayingPos = state.currentPos
                        (it as? VoiceNote)?.currentMaxAmplitude = state.maxAmplitude
                        notesAdapter?.notifyItemChanged(playingAudioPosition)
                    }
                }
            }
        }
    }

    private fun observePlayerSideEffect() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                arkMediaPlayerViewModel.playerSideEffect.collectLatest { sideEffect ->
                    sideEffect ?: return@collectLatest
                    notesAdapter?.observeItemSideEffect = { sideEffect }
                    if (sideEffect == ArkMediaPlayerSideEffect.StopPlaying) {
                        mItemTouchHelper?.attachToRecyclerView(binding.rvPinnedNotes)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (arkMediaPlayerViewModel.isPlaying()) {
            playingAudioPath?.let {
                arkMediaPlayerViewModel.onPlayOrPauseClick(it)
                (notesAdapter?.getNotes()?.getOrNull(playingAudioPosition) as? VoiceNote)?.isPlaying = false
                notesAdapter?.notifyItemChanged(playingAudioPosition)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        lastNoteItemPosition = getCurrentScrollPosition()
    }

    private fun getCurrentScrollPosition(): Int {
        val layoutMgr = (binding.rvPinnedNotes.layoutManager as? LinearLayoutManager)
        return layoutMgr?.findFirstCompletelyVisibleItemPosition() ?: 0
    }

    override fun onResume() {
        super.onResume()
        activity.fragment = this
        observeClipboardContent()
        binding.rvPinnedNotes.layoutManager?.scrollToPosition(lastNoteItemPosition)
        if (notesAdapter?.observableSelectedNotesCount?.hasActiveObservers() == false) {
            observeSelectedNoteForDelete()
        }
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
        context?.getTextFromClipBoard(view) {
            if (it.isNullOrEmpty()) {
                binding.tvPaste.alpha = 0.4f
                binding.tvPaste.isClickable = false
            } else {
                binding.tvPaste.alpha = 1f
                binding.tvPaste.isClickable = true
            }
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
            binding.fabNewAction.backgroundTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(activity, R.color.warning),
                )
            binding.groupFabActions.gone()
            showingFloatingButtons = false
        } else {
            binding.fabNewAction.extend()
            binding.fabNewAction.icon = ContextCompat.getDrawable(activity, R.drawable.ic_close)
            binding.fabNewAction.backgroundTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(activity, R.color.warning_100),
                )
            binding.groupFabActions.visible()
            showingFloatingButtons = true
        }
    }

    private fun toggleActionMode(pos: Int = -1) {
        if (mIsActionMode) {
            binding.groupActionModeTexts.gone()
            binding.layoutBottomControl.visible()
            binding.edtSearch.visible()
            binding.ivSettings.visible()
            notesAdapter?.toggleSelectAllItems(false)
        } else {
            binding.groupActionModeTexts.visible()
            updateSelectStateTexts(selectedCountForDelete)
            binding.layoutBottomControl.gone()
            binding.edtSearch.gone()
            binding.ivSettings.gone()
            binding.tvActionModeCancel.setOnClickListener {
                toggleActionMode()
            }
            binding.tvActionModeSelectAll.setOnClickListener {
                notesAdapter?.toggleSelectAllItems(
                    selected = selectedCountForDelete != notesAdapter?.getNotes()?.size,
                )
                updateSelectStateTexts(selectedCountForDelete)
            }
            binding.btnDelete.setOnClickListener {
                showBatchDeletionDialog()
            }
        }
        (binding.rvPinnedNotes.adapter as? NotesListAdapter)?.toggleActionMode(pos)
        mIsActionMode = !mIsActionMode
    }

    private fun showBatchDeletionDialog() {
        CommonActionDialog(
            title =
                resources.getQuantityString(
                    R.plurals.delete_note_count,
                    selectedCountForDelete,
                    selectedCountForDelete,
                ),
            message = resources.getQuantityString(R.plurals.delete_batch_note_message, selectedCountForDelete),
            positiveText = R.string.action_delete,
            negativeText = R.string.ark_memo_cancel,
            isAlert = true,
            onPositiveClick = {
                binding.pbLoading.visible()
                val selectedNotes = notesAdapter?.selectedNotesForDelete ?: emptyList()
                notesViewModel.onDeleteConfirmed(selectedNotes) {
                    notesAdapter?.removeNotes(selectedNotes)
                    binding.pbLoading.gone()
                    toast(requireContext(), getString(R.string.note_deleted))
                    binding.rvPinnedNotes.adapter?.notifyDataSetChanged()
                    toggleActionMode()
                    checkForEmptyState()
                }
            },
            onNegativeClicked = {},
            onCloseClicked = {},
        ).show(childFragmentManager, CommonActionDialog.TAG)
    }

    private fun updateSelectStateTexts(selectedCount: Int) {
        binding.tvSelectedNoteCount.text =
            resources.getQuantityString(
                R.plurals.selected_note_count, selectedCount, selectedCount,
            )
        binding.tvActionModeSelectAll.text =
            if (selectedCount == (notesAdapter?.getNotes()?.size ?: 0)) {
                getString(R.string.deselect_all)
            } else {
                getString(R.string.select_all)
            }
    }

    private fun changeDeleteButtonState(enabled: Boolean) {
        if (enabled) {
            binding.btnDelete.isClickable = true
            binding.btnDelete.alpha = 1f
        } else {
            binding.btnDelete.isClickable = false
            binding.btnDelete.alpha = 0.4f
        }
    }

    private fun observeSelectedNoteForDelete() {
        notesAdapter?.observableSelectedNotesCount?.observe(viewLifecycleOwner) { count ->
            selectedCountForDelete = count
            updateSelectStateTexts(count)

            changeDeleteButtonState(enabled = count > 0)
        }
    }

    override fun onBackPressed() {
        if (binding.edtSearch.isFocused) {
            binding.edtSearch.text.clear()
            binding.edtSearch.clearFocus()
            binding.rvPinnedNotes.layoutManager?.scrollToPosition(lastNoteItemPosition)
        } else if (mIsActionMode) {
            toggleActionMode()
        } else {
            activity.onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun checkForEmptyState() {
        if (notesAdapter?.getNotes()?.isEmpty() == true) {
            showEmptyState(true)
        }
    }

    companion object {
        const val TAG = "NotesFragment"
    }
}
