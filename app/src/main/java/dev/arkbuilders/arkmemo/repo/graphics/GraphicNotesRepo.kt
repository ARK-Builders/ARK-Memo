package dev.arkbuilders.arkmemo.repo.graphics

import android.util.Log
import dev.arkbuilders.arklib.computeId
import dev.arkbuilders.arklib.data.index.Resource
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import dev.arkbuilders.arkmemo.graphics.SVG
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import dev.arkbuilders.arkmemo.repo.NotesRepo
import dev.arkbuilders.arkmemo.repo.NotesRepoHelper
import dev.arkbuilders.arkmemo.utils.listFiles
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Named
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name
import kotlin.io.path.createTempFile
import kotlin.io.path.exists

class GraphicNotesRepo @Inject constructor(
    private val memoPreferences: MemoPreferences,
    @Named(IO_DISPATCHER) private val iODispatcher: CoroutineDispatcher,
    private val helper: NotesRepoHelper
): NotesRepo<GraphicNote> {

    private lateinit var root: Path

    override suspend fun init() {
        helper.init()
        root = memoPreferences.getNotesStorage()
    }

    override suspend fun save(
        note: GraphicNote,
        callback: (SaveNoteResult) -> Unit
    ) = withContext(iODispatcher) {
        write(note) { callback(it) }
    }

    override suspend fun delete(notes: GraphicNote) = withContext(iODispatcher) {
        helper.deleteNote(notes)
    }

    override suspend fun read(): List<GraphicNote> = withContext(iODispatcher) {
        readStorage()
    }

    private suspend fun write(
        note: GraphicNote,
        callback: (SaveNoteResult) -> Unit
    ) = withContext(iODispatcher) {
        val tempPath = createTempFile()
        note.svg?.generate(tempPath)
        val size = tempPath.fileSize()
        val id = computeId(size, tempPath)
        Log.d(GRAPHICS_REPO, "initial resource name is ${tempPath.name}")
        val isPropertiesChanged = helper.persistNoteProperties(
            resourceId = id,
            noteTitle = note.title,
            description = note.description)

        val resourcePath = root.resolve("${id}.$SVG_EXT")
        if (resourcePath.exists()) {
            if (isPropertiesChanged) {
                callback(SaveNoteResult.SUCCESS)
            } else {
                Log.d(GRAPHICS_REPO, "resource with similar content already exists")
                callback(SaveNoteResult.ERROR_EXISTING)
            }
            return@withContext
        }

        helper.renameResource(
            note,
            tempPath,
            resourcePath,
            id
        )
        Log.d(GRAPHICS_REPO, "resource renamed to $resourcePath successfully")
        callback(SaveNoteResult.SUCCESS)
    }

    private suspend fun readStorage() = withContext(iODispatcher) {
        root.listFiles(SVG_EXT) { path ->
            val svg = SVG.parse(path)
            val size = path.fileSize()
            val id = computeId(size, path)
            val resource = Resource(
                id = id,
                name = path.fileName.name,
                extension = path.extension,
                modified = path.getLastModifiedTime()
            )

            val userNoteProperties = helper.readProperties(id, "")

            GraphicNote(
                title = userNoteProperties.title,
                description = userNoteProperties.description,
                svg = svg,
                resource = resource
            )
        }
    }
}

private const val GRAPHICS_REPO = "graphics-repo"
private const val SVG_EXT = "svg"
