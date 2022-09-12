package space.taran.arkmemo.data.repositories

import space.taran.arkmemo.models.TextNote
import javax.inject.Inject

class TextNotesRepository @Inject constructor() {

    fun saveNote(note: TextNote){

    }

    fun getAllNotes(): List<TextNote>{
        val notes = listOf(
            TextNote(),
            TextNote(),
            TextNote()
        )
        return notes
    }
}