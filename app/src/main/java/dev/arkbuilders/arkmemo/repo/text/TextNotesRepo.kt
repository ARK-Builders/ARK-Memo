package dev.arkbuilders.arkmemo.repo.text

import dev.arkbuilders.arkmemo.models.SaveNoteResult
import dev.arkbuilders.arkmemo.models.TextNote
import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path

interface TextNotesRepo {
    suspend fun init(root: Path, scope: CoroutineScope)
    suspend fun save(note: TextNote, callback: (SaveNoteResult) -> Unit)
    suspend fun delete(note: TextNote): Int
    suspend fun read(): List<TextNote>

}