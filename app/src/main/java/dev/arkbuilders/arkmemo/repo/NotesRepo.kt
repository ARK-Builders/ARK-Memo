package dev.arkbuilders.arkmemo.repo

import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arkmemo.models.SaveNoteResult

interface NotesRepo<Note> {
    suspend fun init(root: String)

    suspend fun save(
        note: Note,
        callback: (SaveNoteResult) -> Unit,
    )

    suspend fun read(): List<Note>

    suspend fun delete(note: Note)

    suspend fun delete(notes: List<Note>)

    suspend fun findNote(id: ResourceId): Note?
}
