package space.taran.arkmemo.data.repositories

import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path

interface NotesRepo<Note> {

    suspend fun init()

    suspend fun save(note: Note)

    suspend fun read(): List<Note>

    suspend fun delete(note: Note)

}