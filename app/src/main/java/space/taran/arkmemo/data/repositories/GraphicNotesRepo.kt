package space.taran.arkmemo.data.repositories

import android.util.Log
import dev.arkbuilders.arklib.computeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.ResourceMeta
import space.taran.arkmemo.models.Content
import space.taran.arkmemo.models.GraphicNote
import space.taran.arkmemo.preferences.MemoPreferences
import space.taran.arkmemo.utils.SVG
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.moveTo

class GraphicNotesRepo @Inject constructor() {

    private val iODispatcher = Dispatchers.IO
    private val root = MemoPreferences.getNotesStorage()

    suspend fun save(note: GraphicNote) = withContext(iODispatcher) {
        write(note.svg!!)
    }

    suspend fun delete(note: GraphicNote) = withContext(iODispatcher) {
        if (root != null && note.meta != null) {
            val path = root.resolve(note.meta.name)
            delete(path)
        }
    }

    suspend fun read(): List<GraphicNote> = withContext(Dispatchers.IO) {
        readStorage()
    }

    private fun write(svg: SVG) {
        if (root != null) {
            val tempPath = createTempFile()
            svg.generate(tempPath)
            val id = computeId(tempPath.fileSize(), tempPath)
            val resourcePath = root.resolve("${id}.$GRAPHICAL_NOTE_EXT")
            tempPath.moveTo(resourcePath)

            Log.d("graphics-repo", "file renamed to $resourcePath successfully")
        }
    }

    private fun delete(path: Path) {
        path.deleteIfExists()
    }

    private suspend fun readStorage() = withContext(Dispatchers.IO) {
        val notes = mutableListOf<GraphicNote>()
        if (root != null) {
            var i = 0
            Files.list(root).forEach { path ->
                if (path.extension == GRAPHICAL_NOTE_EXT) {
                    val svg = SVG.parse(path)
                    val size = path.fileSize()
                    val meta = ResourceMeta(
                        computeId(size, path),
                        path.fileName.name,
                        path.extension,
                        path.getLastModifiedTime(),
                        size
                    )
                    val note = GraphicNote(
                        content = Content("Note ${i++}", svg.pathData),
                        svg = svg,
                        meta = meta
                    )
                    notes.add(note)
                }
            }
        }
        notes
    }

    companion object {
        private const val GRAPHICAL_NOTE_EXT = "note.svg"
    }
}