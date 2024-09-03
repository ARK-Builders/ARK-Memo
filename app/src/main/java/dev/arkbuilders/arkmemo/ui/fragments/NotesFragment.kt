package dev.arkbuilders.arkmemo.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
import dev.arkbuilders.arkmemo.ui.viewmodels.ArkMediaPlayerViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.GraphicNotesViewModel
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.ui.views.toast
import dev.arkbuilders.arkmemo.utils.getTextFromClipBoard
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.replaceFragment
import dev.arkbuilders.arkmemo.utils.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class NotesFragment: Fragment() {

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

    private val newTextNoteClickListener = View.OnClickListener {
        onFloatingActionButtonClicked()
    }

    private val pasteNoteClickListener = View.OnClickListener {
        requireContext().getTextFromClipBoard(view) { clipBoardText ->
            if (clipBoardText != null) {
                activity.fragment = EditTextNotesFragment.newInstance(clipBoardText)
                activity.replaceFragment(activity.fragment, EditTextNotesFragment.TAG)
            }
            else Toast.makeText(requireContext(), getString(R.string.nothing_to_paste),
                Toast.LENGTH_SHORT).show()
        }
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
            val noteToDelete = notesAdapter?.getNotes()?.getOrNull(deletePosition)?.apply {
                pendingForDelete = true
            } ?: return
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
            }).show(childFragmentManager, CommonActionDialog.TAG)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentHomeBinding.inflate(layoutInflater)
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
            activity.fragment = SettingsFragment()
            activity.replaceFragment(activity.fragment, SettingsFragment::class.java.name)
        }
        showingFloatingButtons = false
        initBottomControlViews()
        initEmptyStateViews()

        binding.pbLoading.visible()
        notesViewModel.apply {
            init { readAllNotes {
                onNotesLoaded(it)
            } }
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
                }
        })
    }

    private fun onNotesLoaded(notes: List<Note>) {
        binding.pbLoading.gone()
        if (notesAdapter == null) {
            notesAdapter = NotesListAdapter(
                notes,
                onPlayPauseClick = { path, pos, onStop ->
                    playingAudioPath = path
                    playingAudioPosition = pos ?: -1
                    arkMediaPlayerViewModel.onPlayOrPauseClick(path, pos, onStop)
                },
                onThumbPrepare = { graphicNote, noteCanvas ->
                    val tempNoteViewModel: GraphicNotesViewModel by viewModels()
                    noteCanvas.setViewModel(viewModel = tempNoteViewModel)

                }
            )

        } else {
            notesAdapter?.setNotes(notes)
        }

        val layoutManager = LinearLayoutManager(requireContext())
        observePlayerState()
        observePlayerSideEffect()
        notesAdapter?.setActivity(activity)
        binding.rvPinnedNotes.apply {
            this.layoutManager = layoutManager
            this.adapter = notesAdapter
            this.itemAnimator = object : DefaultItemAnimator() {
                override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                    return true
                }
            }
        }
        ItemTouchHelper(mItemTouchCallback).attachToRecyclerView(binding.rvPinnedNotes)

        if (notes.isNotEmpty()) {
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
