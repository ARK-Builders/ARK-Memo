package dev.arkbuilders.arkmemo.data.repositories

import android.util.Log
import dev.arkbuilders.arklib.computeId
import dev.arkbuilders.arklib.data.index.Resource
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import kotlinx.coroutines.CoroutineDispatcher
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
import kotlin.io.path.exists


class TextNotesRepo @Inject constructor(
    private val memoPreferences: MemoPreferences,
    @Named(IO_DISPATCHER)
    private val iODispatcher: CoroutineDispatcher,
    private val helper: NotesRepoHelper
): NotesRepo<TextNote> {

    private lateinit var root: Path

    override suspend fun init() {
        root = memoPreferences.getNotesStorage()
        helper.init()
    }
    override suspend fun save(note: TextNote, callback: (SaveNoteResult) -> Unit) {
        write(note) { callback(it) }
    }

    override suspend fun delete(note: TextNote) {
        helper.deleteNote(note)
    }

    override suspend fun read(): List<TextNote> = withContext(iODispatcher) {
        readStorage()
    }

    private suspend fun write(
        note: TextNote,
        callback: (SaveNoteResult) -> Unit
    ) = withContext(iODispatcher) {
        val tempPath = createTempFile()
        val lines = note.text.split('\n')
        tempPath.writeLines(lines)
        val size = tempPath.fileSize()
        val id = computeId(size, tempPath)
        Log.d("text-repo", "initial resource name ${tempPath.name}")
        helper.persistNoteProperties(resourceId = id, noteTitle = note.title)

        val resourcePath = root.resolve("$id.$NOTE_EXT")
        if (resourcePath.exists()) {
            Log.d(
                "text-repo",
                "resource with similar content already exists"
            )
            callback(SaveNoteResult.ERROR_EXISTING)
            return@withContext
        }

        helper.renameResource(
            note = note,
            tempPath = tempPath,
            resourcePath = resourcePath,
            resourceId = id
        )
        Log.d("text-repo", "file renamed to ${note.resource?.name} successfully")
        callback(SaveNoteResult.SUCCESS)
    }

    private suspend fun readStorage() = withContext(iODispatcher) {
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

