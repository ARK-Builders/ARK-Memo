package dev.arkbuilders.arkmemo.data.repositories

import android.util.Log
import dev.arkbuilders.arklib.computeId
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import dev.arkbuilders.arkmemo.data.ResourceMeta
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.di.PropertiesStorageModule.STORAGE_SCOPE
import dev.arkbuilders.arkmemo.models.Content
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import java.nio.file.Files
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.forEachLine
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name
import kotlin.io.path.writeLines

@Singleton
class TextNotesRepo @Inject constructor(
    private val memoPreferences: MemoPreferences,
    @Named(IO_DISPATCHER)
    private val iODispatcher: CoroutineDispatcher,
    @Named(STORAGE_SCOPE) private val storageScope: CoroutineScope,
    private val propertiesStorageRepo: PropertiesStorageRepo
): NotesRepoImpl<TextNote>(
    memoPreferences.getNotesStorage(),
    storageScope,
    propertiesStorageRepo
) {

    override suspend fun save(note: TextNote) {
        write(note)
    }

    override suspend fun delete(note: TextNote) {
        deleteNote(note)
    }

    override suspend fun read(): List<TextNote> = withContext(iODispatcher) {
        readStorage()
    }

    private suspend fun write(note: TextNote) = withContext(iODispatcher) {
        val tempPath = kotlin.io.path.createTempFile()
        val lines = note.content.data.split('\n')
        tempPath.writeLines(lines)
        val size = tempPath.fileSize()
        val id = computeId(size, tempPath)
        Log.d("text-repo", "initial resource name ${tempPath.name}")
        persistNoteProperties(resourceId = id, noteTitle = note.title)
        Log.d("text-repo", "note id: ${note.resourceMeta?.id}, computed id $id")
        if (note.exists(id)) {
            Log.d(
                "text-repo",
                "resource with similar content already exists"
            )
            return@withContext
        }

        val resourcePath = root.resolve("$id.$NOTE_EXT")
        renameResourceWithNewResourceMeta(
            note = note,
            tempPath = tempPath,
            resourcePath = resourcePath,
            resourceId = id,
            size = size
        )
        Log.d("text-repo", "file renamed to ${note.resourceMeta?.name} successfully")
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
                val meta = ResourceMeta(
                    id,
                    path.fileName.name,
                    path.extension,
                    path.getLastModifiedTime(),
                    size
                )

                val userNoteProperties = readProperties(id)

                val note = TextNote(
                    userNoteProperties.title,
                    userNoteProperties.description,
                    Content(data.toString()),
                    meta
                )
                notes.add(note)
            }
        }
        Log.d("text-repo", "${notes.size} text note resources found")
        notes
    }
}

private const val NOTE_EXT = "note"

