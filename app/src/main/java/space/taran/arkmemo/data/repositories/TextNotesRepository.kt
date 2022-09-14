package space.taran.arkmemo.data.repositories

import android.content.Context
import space.taran.arkmemo.files.text.TextFiles
import space.taran.arkmemo.models.TextNote
import javax.inject.Inject

class TextNotesRepository @Inject constructor() {

    fun saveNote(context: Context, note: TextNote) =
        TextFiles.saveNote(context, note)

    fun getAllTextNotes(context: Context) =
        TextFiles.readAllNotes(context)
}