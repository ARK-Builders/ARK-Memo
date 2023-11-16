package dev.arkbuilders.arkmemo.data.repositories

interface NotesRepo<Note> {

    suspend fun init()

    suspend fun save(note: Note)

    suspend fun read(): List<Note>

    suspend fun delete(note: Note)
}