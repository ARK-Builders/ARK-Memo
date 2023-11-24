package dev.arkbuilders.arkmemo.data.repositories

import dev.arkbuilders.arkmemo.models.SaveNoteResult

interface NotesRepo<Note> {

    suspend fun init()

    suspend fun save(note: Note, callback: (SaveNoteResult) -> Unit)

    suspend fun read(): List<Note>

    suspend fun delete(note: Note)
}