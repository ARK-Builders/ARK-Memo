package dev.arkbuilders.arkmemo.data.repositories

import android.util.Log
import dev.arkbuilders.arklib.computeId
import dev.arkbuilders.arklib.data.index.Resource
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.di.PropertiesStorageModule.STORAGE_SCOPE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Named
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.forEachLine
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name
import kotlin.io.path.writeLines
import kotlin.io.path.createTempFile


class TextNotesRepo @Inject constructor(
    private val memoPreferences: MemoPreferences,
    @Named(IO_DISPATCHER)
    private val iODispatcher: CoroutineDispatcher,
    @Named(STORAGE_SCOPE) private val storageScope: CoroutineScope,
    private val helper: NotesRepoHelper,
    private val propertiesStorageRepo: PropertiesStorageRepo
): NotesRepo<TextNote> {

    private lateinit var root: Path

    override suspend fun init() {
        root = memoPreferences.getNotesStorage()
        helper.init()
    }
    override suspend fun save(note: TextNote) {
        write(note)
    }

    override suspend fun delete(note: TextNote) {
        helper.deleteNote(note)
    }

    override suspend fun read(): List<TextNote> = withContext(iODispatcher) {
        readStorage()
    }

    private suspend fun write(note: TextNote) = withContext(iODispatcher) {
        val tempPath = createTempFile()
        val lines = note.text.split('\n')
        tempPath.writeLines(lines)
        val size = tempPath.fileSize()
        val id = computeId(size, tempPath)
        Log.d("text-repo", "initial resource name ${tempPath.name}")
        helper.persistNoteProperties(resourceId = id, noteTitle = note.title)
        Log.d("text-repo", "note id: ${note.resource?.id}, computed id $id")
        if (note.exists(id)) {
            Log.d(
                "text-repo",
                "resource with similar text already exists"
            )
            return@withContext
        }

        val resourcePath = root.resolve("$id.$NOTE_EXT")
        helper.renameResource(
            note = note,
            tempPath = tempPath,
            resourcePath = resourcePath,
            resourceId = id
        )
        Log.d("text-repo", "file renamed to ${note.resource?.name} successfully")
    }

    private suspend fun readStorage() = withContext(iODispatcher){
        val notes = mutableListOf<TextNote>()
        Files.list(root).forEach { path ->
            if (path.fileName.extension == NOTE_EXT) {
                val data = StringBuilder()
                path.forEachLine {
                    data.appendLine(it)
                }
                val size = path.fileSize()
                val id = computeId(size, path)
                val resource = Resource(
                    id = id,
                    name = path.fileName.name,
                    extension = path.extension,
                    modified = path.getLastModifiedTime()
                )

                val userNoteProperties = helper.readProperties(id)

                val note = TextNote(
                    title = userNoteProperties.title,
                    description = userNoteProperties.description,
                    text = data.toString(),
                    resource = resource
                )
                notes.add(note)
            }
        }
        Log.d("text-repo", "${notes.size} text note resources found")
        notes
    }
}

private const val NOTE_EXT = "note"

