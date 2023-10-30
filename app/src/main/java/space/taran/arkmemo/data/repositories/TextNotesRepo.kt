package space.taran.arkmemo.data.repositories

import android.util.Log
import dev.arkbuilders.arklib.computeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.ResourceMeta
import space.taran.arkmemo.models.Content
import space.taran.arkmemo.models.TextNote
import java.nio.file.Files
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.forEachLine
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name
import kotlin.io.path.writeLines

@Singleton
class TextNotesRepo @Inject constructor(): NotesRepoImpl(), NotesRepo<TextNote> {

    override suspend fun save(note: TextNote) {
        write(note)
    }

    override suspend fun delete(note: TextNote) {
        deleteNote(note)
    }

    override suspend fun read(): List<TextNote> = withContext(Dispatchers.IO) {
       readStorage()
    }

    private suspend fun write(note: TextNote) = withContext(Dispatchers.IO) {
        val tempPath = kotlin.io.path.createTempFile()
        val lines = note.content.data.split('\n')
        tempPath.writeLines(lines)
        val size = tempPath.fileSize()
        val id = computeId(size, tempPath)
        Log.d("text-repo", "initial resource name ${tempPath.name}")
        persistNoteProperties(resourceId = id, noteTitle = note.title)

        val resourcePath = root.resolve("$id.$NOTE_EXT")
        renameResourceWithNewResourceMeta(
            note = note,
            tempPath = tempPath,
            resourcePath = resourcePath,
            resourceId = id,
            size = size
        )
    }

    private suspend fun readStorage() = withContext(Dispatchers.IO){
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
                val titles = propertiesStorage.getProperties(id).titles
                val descriptions = propertiesStorage.getProperties(id).descriptions
                val content = Content(data.toString())
                val note = TextNote(
                    titles.elementAt(0),
                    descriptions.elementAt(0),
                    content,
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

