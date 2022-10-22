package space.taran.arkmemo.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import space.taran.arkmemo.R
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.ui.fragments.deleteTextNote

class NoteDeleteDialogFragment: DialogFragment() {

    private var note: TextNote? = null

    fun setNoteToBeDeleted(note: TextNote): NoteDeleteDialogFragment{
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
                    parentFragment?.deleteTextNote(note!!)
                    Toast.makeText(
                        requireContext(), getString(R.string.note_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.cancel()
                }
            }
        return builder.create()
    }

    companion object{
        const val TAG = "Note Delete Dialog"
    }
}