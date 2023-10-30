package space.taran.arkmemo.data.repositories

import android.util.Log
import dev.arkbuilders.arklib.computeId
import dev.arkbuilders.arklib.data.index.RootIndex
import dev.arkbuilders.arklib.user.properties.PropertiesStorage
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import kotlinx.coroutines.CoroutineScope
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

class GraphicNotesRepo @Inject constructor(): NotesRepoImpl(), NotesRepo<GraphicNote> {

    private val iODispatcher = Dispatchers.IO

    override suspend fun save(note: GraphicNote) = withContext(iODispatcher) {
        write(note.svg!!)
    }

    override suspend fun delete(note: GraphicNote) = withContext(iODispatcher) {
       deleteNote(note)
    }

    override suspend fun read(): List<GraphicNote> = withContext(iODispatcher) {
        readStorage()
    }

    private fun write(svg: SVG) {
        val tempPath = createTempFile()
        svg.generate(tempPath)
        val id = computeId(tempPath.fileSize(), tempPath)
        val resourcePath = root.resolve("${id}.$GRAPHICAL_NOTE_EXT")
        tempPath.moveTo(resourcePath)

        Log.d("graphics-repo", "file renamed to $resourcePath successfully")
    }

    private suspend fun readStorage() = withContext(iODispatcher) {
        val notes = mutableListOf<GraphicNote>()
        Files.list(root).forEach { path ->
            if (path.extension == GRAPHICAL_NOTE_EXT) {
                val svg = SVG.parse(path)
                val size = path.fileSize()
                val id = computeId(size, path)
                val meta = ResourceMeta(
                    id,
                    path.fileName.name,
                    path.extension,
                    path.getLastModifiedTime(),
                    size
                )

                val titles = propertiesStorage.getProperties(id).titles
                val descriptions = propertiesStorage.getProperties(id).descriptions

                val note = GraphicNote(
                    titles.elementAt(0),
                    descriptions.elementAt(0),
                    Content(svg.pathData),
                    svg,
                    meta
                )
                notes.add(note)
            }
        }
        notes
    }
}

private const val GRAPHICAL_NOTE_EXT = "note.svg"
