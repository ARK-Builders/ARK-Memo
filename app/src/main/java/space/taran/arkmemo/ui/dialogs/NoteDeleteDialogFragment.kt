package space.taran.arkmemo.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import space.taran.arkmemo.R
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.models.Version
import space.taran.arkmemo.ui.fragments.deleteTextNote
import space.taran.arkmemo.ui.fragments.deleteTextNoteFromVersion

class NoteDeleteDialogFragment: DialogFragment() {

    private var note: TextNote? = null
    private var version: Version? = null
    private var isHistory: Boolean = false

    fun setNoteToBeDeleted(note: TextNote, version:Version, isHistory:Boolean? = null): NoteDeleteDialogFragment{
        this.note = note
        this.version = version
        isHistory?.let { this.isHistory = it }
        return this
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if(isHistory){
            val builder = AlertDialog.Builder(requireContext())
                .setMessage(R.string.ark_memo_delete_warn_history)
                .setNegativeButton(R.string.ark_memo_cancel)
                { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton(R.string.ark_memo_ok){ dialog, _ ->
                    if(note != null) {
                        parentFragment?.deleteTextNoteFromVersion(note!!,version!!)
                        Toast.makeText(
                            requireContext(), getString(R.string.note_deleted),
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.cancel()
                    }
                }
            return builder.create()
        }

        val builder = AlertDialog.Builder(requireContext())
            .setMessage(R.string.ark_memo_delete_warn)
            .setNegativeButton(R.string.ark_memo_cancel)
            { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(R.string.ark_memo_ok){ dialog, _ ->
                if(note != null) {
                    parentFragment?.deleteTextNote(note!!,version!!)
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