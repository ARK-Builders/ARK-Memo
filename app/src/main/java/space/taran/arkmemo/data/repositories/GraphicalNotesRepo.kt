package space.taran.arkmemo.data.repositories

import android.util.Log
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.computeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.ResourceMeta
import space.taran.arkmemo.models.GraphicalNote
import space.taran.arkmemo.preferences.MemoPreferences
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

class GraphicalNotesRepo @Inject constructor() {

    private val iODispatcher = Dispatchers.IO
    private val root = MemoPreferences.getNotesStorage()
    private val _notes = MutableStateFlow(listOf<GraphicalNote>())
    val notes: StateFlow<List<GraphicalNote>> = _notes

    suspend fun save(note: GraphicalNote) = withContext(iODispatcher) {
        write(note.svgData)
    }

    suspend fun delete(note: GraphicalNote) = withContext(iODispatcher) {
        if (root != null && note.meta != null) {
            val path = root.resolve(note.meta.name)
            delete(path)
        }
    }

    suspend fun read() = withContext(Dispatchers.IO) {
        readStorage()
    }

    private fun write(svg: String) {
        if (root != null) {
            try {
                val path = Files.createTempFile(root, "gr_note", ".$GRAPHICAL_NOTE_EXT")
                path.writeText(svg)
                val id = computeId(Files.size(path), path)
                if (
                    path.toFile().renameTo(root.resolve("${id.crc32}.$GRAPHICAL_NOTE_EXT").toFile())
                ) Log.d("graphics-repo", "file renamed successfully")
                else delete(path)

            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun delete(path: Path) {
        Files.deleteIfExists(path)
    }

    private suspend fun readStorage() = withContext(Dispatchers.IO) {
        if (root != null) {
            try {
                val notes = mutableListOf<GraphicalNote>()
                Files.list(root).forEach { path ->
                    if (path.extension == GRAPHICAL_NOTE_EXT) {
                        val svgStr = path.readText()
                        val size = path.fileSize()
                        val meta = ResourceMeta(
                            computeId(size, path),
                            path.fileName.name,
                            path.extension,
                            path.getLastModifiedTime(),
                            size
                        )
                        val note = GraphicalNote(
                            svgData = svgStr,
                            meta = meta
                        )
                        notes.add(note)
                    }
                }
                _notes.value = notes
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val GRAPHICAL_NOTE_EXT = "note.svg"
    }
}