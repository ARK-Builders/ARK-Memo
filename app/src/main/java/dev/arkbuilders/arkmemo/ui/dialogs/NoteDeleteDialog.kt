package dev.arkbuilders.arkmemo.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.ui.fragments.deleteNote
import dev.arkbuilders.arkmemo.ui.views.toast

class NoteDeleteDialog(private val mCustomMessage: String? = null,
                       private val mPositiveAction: (() -> Unit)? = null): DialogFragment() {

    private var notes: List<Note>? = null

    fun setNoteToBeDeleted(notes: List<Note>): NoteDeleteDialog {
        this.notes = notes
        return this
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
            .setMessage(R.string.ark_memo_delete_warn)
            .setNegativeButton(R.string.ark_memo_cancel)
            { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(R.string.ark_memo_ok){ dialog, _ ->
                if(notes != null) {
                    parentFragment?.deleteNote(notes!!)
                    toast(requireContext(), getString(R.string.note_deleted))
                    dialog.cancel()
                }

                mPositiveAction?.invoke()
            }
        return builder.create()
    }

    companion object{
        const val TAG = "Note Delete Dialog"
    }
}