package space.taran.arkmemo.data.repositories

import android.util.Log
import dev.arkbuilders.arklib.computeId
import dev.arkbuilders.arklib.data.index.RootIndex
import dev.arkbuilders.arklib.user.properties.Properties
import dev.arkbuilders.arklib.user.properties.PropertiesStorage
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.ResourceMeta
import space.taran.arkmemo.models.TextNote
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.forEachLine
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name
import kotlin.io.path.writeLines

@Singleton
class TextNotesRepo @Inject constructor() {

    private val _textNotes = MutableStateFlow(listOf<TextNote>())
    val textNotes: StateFlow<List<TextNote>> = _textNotes

    private val iODispatcher = Dispatchers.IO

    private lateinit var propertiesStorage: PropertiesStorage
    private lateinit var propertiesStorageRepo: PropertiesStorageRepo

    private lateinit var root: Path

    suspend fun init(root: Path, scope: CoroutineScope) {
        this.root = root
        propertiesStorageRepo = PropertiesStorageRepo(scope)
        propertiesStorage = propertiesStorageRepo.provide(RootIndex.provide(root))
    }

    suspend fun save(note: TextNote?) {
        if (note != null) {
            add(note)
            write(note)
        }
    }

    suspend fun delete(note: TextNote) = withContext(iODispatcher) {
        val path = root.resolve("${note.meta?.name}")
        remove(note)
        delete(path)
        propertiesStorage.remove(note.meta?.id!!)
        propertiesStorage.persist()
        Log.d("text-repo", "${note.meta?.name!!} has been deleted")
    }

    suspend fun read() = withContext(Dispatchers.IO) {
        val notes = mutableListOf<TextNote>()
        Files.list(root).forEach { path ->
            if (path.fileName.extension == NOTE_EXT) {
                try {
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
                    val titles = propertiesStorage.getProperties(id).titles
                    val content = TextNote.Content(titles.elementAt(0), data.toString())
                    val note = TextNote(content, meta)
                    notes.add(note)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        Log.d("text-repo", "${notes.size} text note resources found")
        _textNotes.value = notes
    }

    private suspend fun write(note: TextNote) = withContext(Dispatchers.IO) {
        val path = root.resolve("${DUMMY_FILENAME}.${NOTE_EXT}")
        if (!path.exists()) {
            try {
                val lines = note.content.data.split(NEWLINE)
                path.writeLines(lines)
                val size = path.fileSize()
                val id = computeId(size, path)
                val properties = Properties(setOf(note.content.title), setOf())

                Log.d("text-repo", "initial resource name ${path.name}")

                propertiesStorage.setProperties(id, properties)
                propertiesStorage.persist()

                val newPath = root.resolve("$id.$NOTE_EXT")
                if (!newPath.exists()) {
                    if (path.toFile().renameTo(newPath.toFile())) {
                        note.meta = ResourceMeta(
                            id,
                            newPath.fileName.name,
                            newPath.extension,
                            newPath.getLastModifiedTime(),
                            size
                        )
                        Log.d("notes-repo", "resource renamed to ${newPath.name} successfully")
                    } else delete(path)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        this.coroutineContext.job
    }

    private fun delete(path: Path) {
        path.deleteIfExists()
    }

    private fun add(note: TextNote) {
        val notes = textNotes.value.toMutableList()
        notes.add(note)
        _textNotes.value = notes
    }

    private fun remove(note: TextNote) {
        val notes = textNotes.value.toMutableList()
        notes.remove(note)
        _textNotes.value = notes
    }
}

private const val NOTE_EXT = "note"
private const val DUMMY_FILENAME =  "Note"
private const val NEWLINE = "\n"

