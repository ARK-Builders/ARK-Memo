package space.taran.arkmemo.data.repo.notes.text

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import space.taran.arklib.ResourceId
import space.taran.arklib.computeId
import space.taran.arkmemo.data.ResourceMeta
import space.taran.arkmemo.files.parsers.JsonParser
import space.taran.arkmemo.data.models.TextNote
import space.taran.arkmemo.data.models.Version
import space.taran.arkmemo.data.repo.versions.VersionStorageRepo
import space.taran.arkmemo.preferences.MemoPreferences
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.*

class TextNotesRepo @Inject constructor() {

    @Inject @ApplicationContext lateinit var context: Context

    fun saveNote(note: TextNote): ResourceId {
        var id: ResourceId? = null
        val path = MemoPreferences.getInstance(context)
            .getPath()
        if (path != null) {
            Files.list(path)
            id = createTextNoteFile(
                path,
                note
            )
        }
        return id!!
    }

    fun deleteNote(note: TextNote) {
        val filePath = MemoPreferences.getInstance(context)
            .getPath()?.resolve("${note.meta?.name}")
        removeFileFromMemory(filePath)
        Log.d("text-notes-repo", "deleted ${note.meta?.name!!}")
    }

    fun getAllNotes(): List<TextNote> {
        val notes = mutableListOf<TextNote>()
        val path = MemoPreferences.getInstance(context)
            .getPath()
        var number = 0
        if (path != null) {
            Files.list(path).forEach { filePath ->
                if (filePath.fileName.extension == NOTE_EXT) {
                    number += 1
                    try {
                        val jsonFile = filePath.toFile()
                        val fileReader = FileReader(jsonFile)
                        val bufferedReader = BufferedReader(fileReader)
                        val jsonTextNote = StringBuilder()
                        with(bufferedReader) {
                            forEachLine {
                                jsonTextNote.append(it)
                            }
                            val content = JsonParser.parseNoteFromJson(jsonTextNote.toString())
                            val size = Files.size(filePath)
                            val id = computeId(size, filePath)
                            val meta = ResourceMeta(
                                id,
                                filePath.fileName.toString(),
                                filePath.extension,
                                filePath.getLastModifiedTime(),
                                size
                            )

                            val note = TextNote(content, meta)
                            notes.add(note)
                            close()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return notes
    }

    private fun createTextNoteFile(path: Path?, note: TextNote): ResourceId {
        var id: ResourceId? = null
        fun writeToFile(bufferedWriter: BufferedWriter) {
            with(bufferedWriter) {
                val noteString = JsonParser.parseNoteToJson(note.content)
                write(noteString)
                close()
            }
        }
        if (path != null) {
            val file = path.toFile()
            val noteFile = File.createTempFile(FILENAME,".$NOTE_EXT")
            val notePath = noteFile.toPath()
            try {
                val fileWriter = FileWriter(noteFile)
                val bufferedWriter = BufferedWriter(fileWriter)
                writeToFile(bufferedWriter)

                id = computeId(Files.size(notePath), notePath)

                Log.d("text-notes-repo/filename", notePath.name)
                Log.d("text-notes-repo/file id", id.toString())

                with(noteFile){
                    val newFile = File(file, "${id.crc32}.$NOTE_EXT")
                    if(!newFile.exists())
                        if (renameTo(newFile))
                            Log.d("text-notes-repo/new filename", newFile.name)
                        else
                            removeFileFromMemory(noteFile.toPath())

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return id!!
    }

    private fun removeFileFromMemory(path: Path?) {
        if (path != null)
            Files.deleteIfExists(path)
    }

    companion object {
        private const val NOTE_EXT = "note"
        private const val FILENAME =  "Note"
    }
}