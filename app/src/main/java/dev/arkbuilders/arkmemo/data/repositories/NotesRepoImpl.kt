package space.taran.arkmemo.data.repositories

import android.util.Log
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.data.index.RootIndex
import dev.arkbuilders.arklib.user.properties.Properties
import dev.arkbuilders.arklib.user.properties.PropertiesStorage
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.ResourceMeta
import space.taran.arkmemo.models.BaseNote
import space.taran.arkmemo.models.TextNote
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.deleteIfExists
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.moveTo
import kotlin.io.path.name

abstract class NotesRepoImpl<Note> constructor(
    protected val root: Path,
    private val scope: CoroutineScope,
    private val propertiesStorageRepo: PropertiesStorageRepo
): NotesRepo<Note> {

    protected lateinit var propertiesStorage: PropertiesStorage

    override suspend fun init() {
        propertiesStorage = propertiesStorageRepo.provide(RootIndex.provide(root))
    }

    protected suspend fun persistNoteProperties(resourceId: ResourceId, noteTitle: String) {
        with(propertiesStorage) {
            val properties = Properties(setOf(noteTitle), setOf())
            setProperties(resourceId, properties)
            persist()
        }
    }

    protected fun renameResourceWithNewResourceMeta(
        note: BaseNote,
        tempPath: Path,
        resourcePath: Path,
        resourceId: ResourceId,
        size: Long
    ) {
        tempPath.moveTo(resourcePath)
        note.resourceMeta = ResourceMeta(
            id = resourceId,
            name = resourcePath.fileName.name,
            extension = resourcePath.extension,
            modified = resourcePath.getLastModifiedTime(),
            size = size
        )
        Log.d("notes-repo", "resource renamed to ${resourcePath.name} successfully")
    }

    protected fun readProperties(id: ResourceId): UserNoteProperties {
        val title = propertiesStorage.getProperties(id).titles.let {
            if (it.isNotEmpty()) it.elementAt(0) else throw NoteTitlesException()
        }
        val description = propertiesStorage.getProperties(id).descriptions.let {
            if (it.isNotEmpty()) it.elementAt(0) else ""
        }
        return UserNoteProperties(title, description)
    }

    protected suspend fun deleteNote(note: BaseNote): Unit = withContext(Dispatchers.IO) {
        val path = root.resolve("${note.resourceMeta?.name}")
        path.deleteIfExists()
        propertiesStorage.remove(note.resourceMeta?.id!!)
        propertiesStorage.persist()
        Log.d("repo", "${note.resourceMeta?.name!!} has been deleted")
    }

    protected fun BaseNote.exists(id: ResourceId) = resourceMeta != null && resourceMeta?.id == id
}

data class UserNoteProperties(
    val title: String,
    val description: String
)

class NoteTitlesException: Exception("note resource missing at least one title")