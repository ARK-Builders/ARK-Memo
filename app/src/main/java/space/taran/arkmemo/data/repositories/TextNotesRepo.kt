package space.taran.arkmemo.data.repositories

import kotlinx.coroutines.CoroutineScope
import space.taran.arkmemo.models.TextNote
import java.nio.file.Path

interface TextNotesRepo {
    suspend fun init(root: Path, scope: CoroutineScope)
    suspend fun save(note: TextNote)
    suspend fun delete(note: TextNote): Int
    suspend fun read(): List<TextNote>

}