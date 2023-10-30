package space.taran.arkmemo.data.repositories

import android.util.Log
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.data.index.RootIndex
import dev.arkbuilders.arklib.user.properties.Properties
import dev.arkbuilders.arklib.user.properties.PropertiesStorage
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.ResourceMeta
import space.taran.arkmemo.models.BaseNote
import space.taran.arkmemo.models.TextNote
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.moveTo
import kotlin.io.path.name

open class NotesRepoImpl {

    protected lateinit var root: Path
    protected lateinit var propertiesStorage: PropertiesStorage
    private lateinit var propertiesStorageRepo: PropertiesStorageRepo

    suspend fun init(root: Path, scope: CoroutineScope) {
        this.root = root
        propertiesStorageRepo = PropertiesStorageRepo(scope)
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
        note: TextNote,
        tempPath: Path,
        resourcePath: Path,
        resourceId: ResourceId,
        size: Long
    ) {
        tempPath.moveTo(resourcePath)
        note.meta = ResourceMeta(
            id = resourceId,
            name = resourcePath.fileName.name,
            extension = resourcePath.extension,
            modified = resourcePath.getLastModifiedTime(),
            size = size
        )
        Log.d("notes-repo", "resource renamed to ${resourcePath.name} successfully")
    }

    protected suspend fun deleteNote(note: BaseNote): Unit = withContext(Dispatchers.IO) {
        val path = root.resolve("${note.resourceMeta?.name}")
        path.deleteIfExists()
        propertiesStorage.remove(note.resourceMeta?.id!!)
        propertiesStorage.persist()
        Log.d("repo", "${note.resourceMeta?.name!!} has been deleted")
    }
}