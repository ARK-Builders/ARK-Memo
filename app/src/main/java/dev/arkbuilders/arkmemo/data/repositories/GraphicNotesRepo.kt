package dev.arkbuilders.arkmemo.data.repositories

import android.util.Log
import dev.arkbuilders.arklib.computeId
import dev.arkbuilders.arklib.data.index.Resource
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.di.PropertiesStorageModule.STORAGE_SCOPE
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import dev.arkbuilders.arkmemo.graphics.SVG
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import java.nio.file.Files
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

    override suspend fun delete(note: GraphicNote) = withContext(iODispatcher) {
        helper.deleteNote(note)
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
        Log.d("graphic-repo", "initial resource name ${tempPath.name}")
        helper.persistNoteProperties(resourceId = id, noteTitle = note.title)

        val resourcePath = root.resolve("${id}.$SVG_EXT")
        if (resourcePath.exists()) {
            Log.d(
                "graphic-repo",
                "resource with similar text already exists"
            )
            callback(SaveNoteResult.ERROR_EXISTING)
            return@withContext
        }

        helper.renameResource(
            note,
            tempPath,
            resourcePath,
            id
        )
        Log.d("graphic-repo", "file renamed to $resourcePath successfully")
        callback(SaveNoteResult.SUCCESS)
    }

    private suspend fun readStorage() = withContext(iODispatcher) {
        val notes = mutableListOf<GraphicNote>()
        Files.list(root).forEach { path ->
            if (path.extension == SVG_EXT) {
                val svg = SVG.parse(path)
                val size = path.fileSize()
                val id = computeId(size, path)
                val resource = Resource(
                    id = id,
                    name = path.fileName.name,
                    extension = path.extension,
                    modified = path.getLastModifiedTime()
                )

                val userNoteProperties = helper.readProperties(id)

                val note = GraphicNote(
                    title = userNoteProperties.title,
                    description = userNoteProperties.description,
                    svg = svg,
                    resource = resource
                )
                notes.add(note)
            }
        }
        Log.d("graphic-repo", "${notes.size} graphic note resources found")
        notes
    }
}

private const val SVG_EXT = "svg"
