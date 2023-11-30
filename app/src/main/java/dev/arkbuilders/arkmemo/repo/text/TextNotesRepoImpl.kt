package dev.arkbuilders.arkmemo.repo.text

import android.util.Log
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.computeId
import dev.arkbuilders.arklib.data.index.Resource
import dev.arkbuilders.arklib.data.index.RootIndex
import dev.arkbuilders.arklib.user.properties.Properties
import dev.arkbuilders.arklib.user.properties.PropertiesStorage
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import dev.arkbuilders.arkmemo.models.TextNote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.forEachLine
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.writeLines

class TextNotesRepoImpl @Inject constructor(): TextNotesRepo {

    private val iODispatcher = Dispatchers.IO

    private lateinit var propertiesStorage: PropertiesStorage
    private lateinit var propertiesStorageRepo: PropertiesStorageRepo

    private lateinit var root: Path

    override suspend fun init(root: Path, scope: CoroutineScope) {
        this.root = root
        propertiesStorageRepo = PropertiesStorageRepo(scope)
        propertiesStorage = propertiesStorageRepo.provide(RootIndex.provide(root))
    }

   override suspend fun save(note: TextNote, callback: (SaveNoteResult) -> Unit) {
        write(note, callback)
    }

    override suspend fun delete(note: TextNote) = withContext(iODispatcher) {
        val path = root.resolve("${note.resource?.name}")
        delete(path)
        propertiesStorage.remove(note.resource?.id!!)
        propertiesStorage.persist()
        Log.d("text-repo", "${note.resource?.name!!} has been deleted")
    }

    override  suspend fun read(): List<TextNote> = withContext(Dispatchers.IO) {
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
                    id,
                    path.fileName.name,
                    path.extension,
                    path.getLastModifiedTime()
                )
                val titles = propertiesStorage.getProperties(id).titles
                val note = TextNote(
                    title = titles.elementAtOrNull(0) ?: "",
                    text = data.toString(),
                    resource = resource
                )
                notes.add(note)
            }
        }
        Log.d("text-repo", "${notes.size} text note resources found")
        notes
    }

    private suspend fun write(
        note: TextNote,
        saveResultCallback: (SaveNoteResult) -> Unit
    ) = withContext(Dispatchers.IO) {
        val tempPath = kotlin.io.path.createTempFile()
        val lines = note.text.split('\n')
        tempPath.writeLines(lines)
        val size = tempPath.fileSize()
        val id = computeId(size, tempPath)
        Log.d("text-repo", "initial resource name ${tempPath.name}")
        persistNoteProperties(resourceId = id, noteTitle = note.title)

        val resourcePath = root.resolve("$id.$NOTE_EXT")
        if (resourcePath.exists()) {
            saveResultCallback(SaveNoteResult.ERROR_EXISTING)
            return@withContext
        }
        renameResourceWithNewResourceMeta(
            note = note,
            tempPath = tempPath,
            resourcePath = resourcePath,
            resourceId = id,
            size = size
        )
        saveResultCallback(SaveNoteResult.SUCCESS)
    }

    private suspend fun persistNoteProperties(resourceId: ResourceId, noteTitle: String) {
        with(propertiesStorage) {
            val properties = Properties(setOf(noteTitle), setOf())
            setProperties(resourceId, properties)
            persist()
        }
    }

    private fun renameResourceWithNewResourceMeta(
        note: TextNote,
        tempPath: Path,
        resourcePath: Path,
        resourceId: ResourceId,
        size: Long
    ) {
        tempPath.moveTo(resourcePath)
        note.resource = Resource(
            id = resourceId,
            name = resourcePath.fileName.name,
            extension = resourcePath.extension,
            modified = resourcePath.getLastModifiedTime()
        )
        Log.d("notes-repo", "resource renamed to ${resourcePath.name} successfully")
    }

    private fun delete(path: Path) {
        path.deleteIfExists()
    }
}

private const val NOTE_EXT = "note"
