package dev.arkbuilders.arkmemo.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.ui.fragments.deleteNote
import dev.arkbuilders.arkmemo.ui.views.toast

class NoteDeleteDialog: DialogFragment() {

    private var note: Note? = null

    fun setNoteToBeDeleted(note: Note): NoteDeleteDialog {
        this.note = note
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
                if(note != null) {
                    parentFragment?.deleteNote(note!!)
                    toast(requireContext(), getString(R.string.note_deleted))
                    dialog.cancel()
                }
            }
        return builder.create()
    }

    companion object{
        const val TAG = "Note Delete Dialog"
    }
}