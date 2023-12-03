package dev.arkbuilders.arkmemo.repo

import android.util.Log
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.data.index.Resource
import dev.arkbuilders.arklib.data.index.RootIndex
import dev.arkbuilders.arklib.user.properties.Properties
import dev.arkbuilders.arklib.user.properties.PropertiesStorage
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.deleteIfExists
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.moveTo
import kotlin.io.path.name

class NotesRepoHelper @Inject constructor(
    private val memoPreferences: MemoPreferences,
    private val propertiesStorageRepo: PropertiesStorageRepo
) {

    private lateinit var root: Path
    private lateinit var propertiesStorage: PropertiesStorage

    suspend fun init() {
        root = memoPreferences.getNotesStorage()
        propertiesStorage = propertiesStorageRepo.provide(RootIndex.provide(root))
    }

    suspend fun persistNoteProperties(resourceId: ResourceId, noteTitle: String) {
        with(propertiesStorage) {
            val properties = Properties(setOf(noteTitle), setOf())
            setProperties(resourceId, properties)
            persist()
        }
    }

    fun renameResource(
        note: Note,
        tempPath: Path,
        resourcePath: Path,
        resourceId: ResourceId,
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

    fun readProperties(id: ResourceId): UserNoteProperties {
        val title = propertiesStorage.getProperties(id).titles.let {
            if (it.isNotEmpty()) it.elementAt(0) else throw NoteTitlesException()
        }
        val description = propertiesStorage.getProperties(id).descriptions.let {
            if (it.isNotEmpty()) it.elementAt(0) else ""
        }
        return UserNoteProperties(title, description)
    }

    suspend fun deleteNote(note: Note): Unit = withContext(Dispatchers.IO) {
        val path = root.resolve("${note.resource?.name}")
        path.deleteIfExists()
        propertiesStorage.remove(note.resource?.id!!)
        propertiesStorage.persist()
        Log.d("repo", "${note.resource?.name!!} has been deleted")
    }
}

data class UserNoteProperties(
    val title: String,
    val description: String
)

class NoteTitlesException: Exception("note resource missing at least one title")