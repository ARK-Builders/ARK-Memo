package dev.arkbuilders.arkmemo.repo

import android.util.Log
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.data.index.Resource
import dev.arkbuilders.arklib.data.index.RootIndex
import dev.arkbuilders.arklib.user.properties.Properties
import dev.arkbuilders.arklib.user.properties.PropertiesStorage
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.utils.isEqual
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Named
import kotlin.NullPointerException
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.moveTo
import kotlin.io.path.name

class NotesRepoHelper
    @Inject
    constructor(
        private val propertiesStorageRepo: PropertiesStorageRepo,
        @Named(IO_DISPATCHER) private val iODispatcher: CoroutineDispatcher,
    ) {
        lateinit var root: Path

        private lateinit var propertiesStorage: PropertiesStorage
        private val lazyPropertiesStorage by lazy {
            CoroutineScope(iODispatcher).async {
                val propertyStorage = propertiesStorageRepo.provide(RootIndex.provide(root))
                propertyStorage
            }
        }

        suspend fun init(root: String) {
            this.root = Path(root)
            propertiesStorage = lazyPropertiesStorage.await()
        }

        suspend fun persistNoteProperties(
            resourceId: ResourceId,
            noteTitle: String,
            description: String? = null,
        ): Boolean {
            with(propertiesStorage) {
                val properties =
                    Properties(
                        setOf(noteTitle),
                        mutableSetOf<String>().apply {
                            description?.let { this.add(description) }
                        },
                    )
                val currentProperties = getProperties(resourceId)
                if (currentProperties.isEqual(properties)) {
                    return false
                } else {
                    setProperties(resourceId, properties)
                    persist()
                    return true
                }
            }
        }

        fun renameResource(
            note: Note,
            tempPath: Path,
            resourcePath: Path,
            resourceId: ResourceId,
        ) {
            tempPath.moveTo(resourcePath)
            note.resource =
                Resource(
                    id = resourceId,
                    name = resourcePath.fileName.name,
                    extension = resourcePath.extension,
                    modified = resourcePath.getLastModifiedTime(),
                )
            Log.d("notes-repo", "resource renamed to ${resourcePath.name} successfully")
        }

        fun readProperties(
            id: ResourceId,
            defaultTitle: String,
        ): UserNoteProperties {
            val title =
                propertiesStorage.getProperties(id).titles.let {
                    if (it.isNotEmpty()) it.elementAt(0) else defaultTitle
                }
            val description =
                propertiesStorage.getProperties(id).descriptions.let {
                    if (it.isNotEmpty()) it.elementAt(0) else ""
                }
            return UserNoteProperties(title, description)
        }

        suspend fun deleteNotes(notes: List<Note>): Unit =
            withContext(Dispatchers.IO) {
                notes.forEach { note ->
                    deleteNote(note)
                }
            }

        suspend fun deleteNote(note: Note): Unit =
            withContext(Dispatchers.IO) {
                val id = note.resource?.id

                val path = root.resolve("${note.resource?.name}")
                path.deleteIfExists()
                note.resource?.id?.let { resourceId ->
                    try {
                        propertiesStorage.remove(resourceId)
                    } catch (ex: NullPointerException) {
                        Log.e("NotesRepoHelper", "deleteNote exception: " + ex.message)
                    }
                }

                propertiesStorage.persist()
                note.resource?.name?.let { name ->
                    Log.d("NotesRepoHelper", "$name has been deleted. id: " + id)
                }
            }
    }

data class UserNoteProperties(
    val title: String,
    val description: String,
)
