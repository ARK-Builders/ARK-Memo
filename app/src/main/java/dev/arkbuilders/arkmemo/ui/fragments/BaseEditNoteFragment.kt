package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentEditNotesBinding
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.dialogs.CommonActionDialog
import dev.arkbuilders.arkmemo.ui.viewmodels.NotesViewModel
import dev.arkbuilders.arkmemo.ui.views.toast
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.visible
import java.util.Calendar
import java.util.Locale

abstract class BaseEditNoteFragment : BaseFragment() {
    lateinit var binding: FragmentEditNotesBinding
    val notesViewModel: NotesViewModel by activityViewModels()
    val hostActivity by lazy { activity as MainActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentEditNotesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDescription.setOnClickListener {
            if (binding.editTextDescription.visibility == View.GONE) {
                binding.tvDescription.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_chevron_down,
                    0,
                )
                binding.editTextDescription.visibility = View.VISIBLE
            } else {
                binding.tvDescription.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_chevron_right,
                    0,
                )
                binding.editTextDescription.visibility = View.GONE
            }
        }

        binding.toolbar.ivBack.setOnClickListener {
            handleBackPressed()
        }

        if (this is ArkMediaPlayerFragment) {
            binding.toolbar.ivRightActionIcon.gone()
            binding.toolbar.tvRightActionText.visible()
            binding.groupTextControls.gone()
            binding.layoutGraphicsControl.root.gone()
            binding.layoutAudioRecord.root.visible()
        } else {
            binding.toolbar.ivRightActionIcon.visible()
            binding.toolbar.tvRightActionText.gone()
            binding.layoutAudioRecord.root.gone()

            if (this is EditTextNotesFragment || this is ArkRecorderFragment) {
                binding.layoutGraphicsControl.root.gone()

                if (this is EditTextNotesFragment) {
                    binding.groupTextControls.visible()
                }
            } else {
                binding.layoutGraphicsControl.root.visible()
                binding.groupTextControls.gone()
            }
        }

        if (this is EditGraphicNotesFragment) {
            binding.toolbar.tvRightActionText.visible()
            binding.toolbar.ivRightActionIcon.gone()
        } else {
            binding.toolbar.tvRightActionText.gone()
            binding.toolbar.ivRightActionIcon.visible()
        }

        if (getCurrentNote().resource == null) {
            binding.tvLastModified.gone()
        } else {
            binding.tvLastModified.visible()
            val calendar = Calendar.getInstance(Locale.ENGLISH)
            calendar.timeInMillis = getCurrentNote().resource?.modified?.toMillis()
                ?: System.currentTimeMillis()
            val lastModifiedTime =
                DateFormat.format(
                    "dd MMM yyyy', 'hh:mm aa",
                    calendar,
                ).toString()
            binding.tvLastModified.text = getString(R.string.note_last_modified_time, lastModifiedTime)
        }
    }

    private fun showSaveNoteDialog(
        needStopRecording: Boolean = false,
        onDiscard: (needClearResource: Boolean) -> Unit,
    ) {
        val saveNoteDialog =
            CommonActionDialog(
                title = getString(R.string.dialog_save_note_title),
                message = getString(R.string.dialog_save_note_message),
                positiveText = R.string.save,
                negativeText = R.string.discard,
                isAlert = false,
                onPositiveClick = {
                    if (needStopRecording) {
                        (this as? ArkRecorderFragment)?.stopIfRecording()
                    }
                    notesViewModel.onSaveClick(createNewNote()) { show ->
                        hostActivity.showProgressBar(show)
                    }
                },
                onNegativeClicked = {
                    onDiscard.invoke(getCurrentNote().resource?.id == null)
                    hostActivity.onBackPressedDispatcher.onBackPressed()
                },
            )
        saveNoteDialog.show(parentFragmentManager, CommonActionDialog.TAG)
    }

    fun showDeleteNoteDialog(note: Note) {
        CommonActionDialog(
            title = getString(R.string.delete_note),
            message = resources.getQuantityString(R.plurals.delete_batch_note_message, 1),
            positiveText = R.string.action_delete,
            negativeText = R.string.ark_memo_cancel,
            isAlert = true,
            onPositiveClick = {
                notesViewModel.onDeleteConfirmed(listOf(note)) {
                    hostActivity.onBackPressedDispatcher.onBackPressed()
                    toast(requireContext(), getString(R.string.note_deleted))
                }
            },
            onNegativeClicked = {
            },
        ).show(parentFragmentManager, CommonActionDialog.TAG)
    }

    private fun handleBackPressed() {
        val recordFragment = this as? ArkRecorderFragment
        val isRecording = recordFragment?.isRecordingVoiceNote() ?: false

        if (isContentChanged() && !isContentEmpty() || isRecording) {
            showSaveNoteDialog(needStopRecording = isRecording) { needClearResource ->
                if (needClearResource) {
                    recordFragment?.stopIfRecording()
                    recordFragment?.deleteTempFile()
                }
            }
        } else {
            hostActivity.onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onBackPressed() {
        handleBackPressed()
    }

    abstract fun createNewNote(): Note

    abstract fun getCurrentNote(): Note

    abstract fun isContentChanged(): Boolean

    abstract fun isContentEmpty(): Boolean
}
