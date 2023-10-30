package space.taran.arkmemo.data.repositories

import dev.arkbuilders.arklib.user.properties.PropertiesStorage
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import kotlinx.coroutines.CoroutineScope
import space.taran.arkmemo.models.BaseNote
import java.nio.file.Path

interface NotesRepo<Note> {
    suspend fun save(note: Note)

    suspend fun read(): List<Note>

    suspend fun delete(note: Note)

}