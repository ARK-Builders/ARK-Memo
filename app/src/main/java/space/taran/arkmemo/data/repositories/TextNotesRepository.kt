package space.taran.arkmemo.data.repositories

import android.content.Context
import space.taran.arkmemo.files.text.TextNoteFiles
import space.taran.arkmemo.models.TextNote
import javax.inject.Inject

class TextNotesRepository @Inject constructor() {

    fun saveNote(context: Context, note: TextNote) =
        TextNoteFiles.saveNote(context, note)

    fun deleteTextNote(context: Context, note: TextNote) =
        TextNoteFiles.deleteNote(context, note)

    fun getAllTextNotes(context: Context) =
        TextNoteFiles.readAllNotes(context)
}