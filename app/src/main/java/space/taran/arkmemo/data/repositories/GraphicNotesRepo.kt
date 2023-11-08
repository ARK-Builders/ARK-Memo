package space.taran.arkmemo.data.repositories

import android.util.Log
import dev.arkbuilders.arklib.computeId
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.ResourceMeta
import space.taran.arkmemo.di.IO_DISPATCHER
import space.taran.arkmemo.di.PropertiesStorageModule.STORAGE_SCOPE
import space.taran.arkmemo.models.Content
import space.taran.arkmemo.models.GraphicNote
import space.taran.arkmemo.preferences.MemoPreferences
import space.taran.arkmemo.utils.SVG
import java.nio.file.Files
import javax.inject.Inject
import javax.inject.Named
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name
import kotlin.io.path.createTempFile

class GraphicNotesRepo @Inject constructor(
    private val memoPreferences: MemoPreferences,
    @Named(IO_DISPATCHER) private val iODispatcher: CoroutineDispatcher,
    @Named(STORAGE_SCOPE) private val storageScope: CoroutineScope,
    private val propertiesStorageRepo: PropertiesStorageRepo
): NotesRepoImpl<GraphicNote>(
    memoPreferences.getNotesStorage(),
    storageScope,
    propertiesStorageRepo
) {

    override suspend fun save(note: GraphicNote) = withContext(iODispatcher) {
        write(note)
    }

    override suspend fun delete(note: GraphicNote) = withContext(iODispatcher) {
       deleteNote(note)
    }

    override suspend fun read(): List<GraphicNote> = withContext(iODispatcher) {
        readStorage()
    }

    private suspend fun write(note: GraphicNote) = withContext(iODispatcher) {
        val tempPath = createTempFile()
        note.svg?.generate(tempPath)
        val size = tempPath.fileSize()
        val id = computeId(size, tempPath)
        Log.d("graphic-repo", "initial resource name ${tempPath.name}")
        persistNoteProperties(resourceId = id, noteTitle = note.title)

        if (note.exists(id)){
            Log.d(
                "graphic-repo",
                "resource with similar content already exists"
            )
            return@withContext
        }

        val resourcePath = root.resolve("${id}.$GRAPHIC_NOTE_ID")
        renameResourceWithNewResourceMeta(
            note,
            tempPath,
            resourcePath,
            id,
            size
        )
        Log.d("graphic-repo", "file renamed to $resourcePath successfully")
    }

    private suspend fun readStorage() = withContext(iODispatcher) {
        val notes = mutableListOf<GraphicNote>()
        Files.list(root).forEach { path ->
            if (path.name.contains(GRAPHIC_NOTE_ID) && path.extension == SVG_EXT) {
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

                val userNoteProperties = readProperties(id)

                val note = GraphicNote(
                    userNoteProperties.title,
                    userNoteProperties.description,
                    Content(svg.pathData),
                    svg,
                    meta
                )
                notes.add(note)
            }
        }
        Log.d("graphic-repo", "${notes.size} graphic note resources found")
        notes
    }
}

private const val SVG_EXT = "svg"
private const val GRAPHIC_NOTE_ID = "note.$SVG_EXT"
