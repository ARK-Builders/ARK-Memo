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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.ResourceMeta
import space.taran.arkmemo.models.TextNote
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime

@Singleton
class TextNotesRepository @Inject constructor() {

    private val _textNotes = MutableStateFlow(listOf<TextNote>())
    val textNotes: StateFlow<List<TextNote>> = _textNotes

    private val iODispatcher = Dispatchers.IO

    private lateinit var propertiesStorage: PropertiesStorage
    private lateinit var propertiesStorageRepo: PropertiesStorageRepo

    private lateinit var root: Path

    fun init(root: Path, scope: CoroutineScope) {
        this.root = root
        scope.launch(iODispatcher) {
            propertiesStorageRepo = PropertiesStorageRepo(this)
            propertiesStorage = propertiesStorageRepo.provide(RootIndex.provide(root))
            readAllNotes()
        }
    }

    suspend fun saveNote(note: TextNote?) {
        if (note != null) {
            withContext(iODispatcher) {
                writeTextNoteFile(note.content)
                readAllNotes()
            }
        }
    }

    suspend fun deleteNote(note: TextNote) = withContext(iODispatcher) {
        val filePath = root.resolve("${note.meta?.name}")
        removeFileFromMemory(filePath)
        propertiesStorage.remove(note.meta?.id!!)
        propertiesStorage.persist()
        Log.d("notes-repo", "${note.meta.name} has been deleted")
        readAllNotes()
    }

    private suspend fun readAllNotes() {
        val notes = mutableListOf<TextNote>()
        Files.list(root).forEach { filePath ->
            if (filePath.fileName.extension == NOTE_EXT) {
                try {
                    val file = filePath.toFile()
                    val fileReader = FileReader(file)
                    val bufferedReader = BufferedReader(fileReader)
                    val data = StringBuilder()
                    with(bufferedReader) {
                        forEachLine {
                            data.append(it)
                        }
                        val size = Files.size(filePath)
                        val id = computeId(size, filePath)
                        val meta = ResourceMeta(
                            id,
                            filePath.fileName.toString(),
                            filePath.extension,
                            filePath.getLastModifiedTime(),
                            size
                        )
                        val titles = propertiesStorage.getProperties(id).titles
                        val content = TextNote.Content(titles.elementAt(0), data.toString())
                        val note = TextNote(content, meta)
                        notes.add(note)
                        close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        Log.d("notes-repo", "notes ${notes.size}")
        _textNotes.emit(notes)
    }

    private suspend fun writeTextNoteFile(content: TextNote.Content) = withContext(Dispatchers.IO) {
        val file = root.toFile()
        val noteFile = File(file, "${DUMMY_FILENAME}.${NOTE_EXT}")
        if (!noteFile.exists()) {
            try {
                val fileWriter = FileWriter(noteFile)
                val bufferedWriter = BufferedWriter(fileWriter)
                writeToFile(bufferedWriter, content.data)

                val id = computeId(Files.size(noteFile.toPath()), noteFile.toPath())

                val properties = Properties(setOf(content.title), setOf())

                Log.d("notes-repo", "filename ${noteFile.name}")

                propertiesStorage.setProperties(id, properties)
                propertiesStorage.persist()

                with(noteFile) {
                    val newFile = File(file, "${id.crc32}.${NOTE_EXT}")

                    if (!newFile.exists())
                        if (renameTo(newFile))
                            Log.d("notes-repo", "new filename ${newFile.name}")
                        else
                            removeFileFromMemory(this.toPath())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun writeToFile(bufferedWriter: BufferedWriter, string: String) {
        with(bufferedWriter) {
            write(string)
            close()
        }
    }

    private fun removeFileFromMemory(filePath: Path) {
        Files.deleteIfExists(filePath)
    }

    companion object {
        private const val NOTE_EXT = "note"
        private const val DUMMY_FILENAME =  "Note"
    }
}