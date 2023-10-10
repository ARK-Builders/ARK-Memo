package dev.arkbuilders.arkmemo.data.repositories

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.arkbuilders.arklib.computeId
import dev.arkbuilders.arkmemo.data.ResourceMeta
import dev.arkbuilders.arkmemo.files.parsers.JsonParser
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime

class TextNotesRepository @Inject constructor() {

    @Inject @ApplicationContext lateinit var context: Context

    fun saveNote(note: TextNote?) {
        if (note != null) {
            val path = getPath()
            if (path != null) {
                Files.list(path)
                createTextNoteFile(
                    path,
                    JsonParser.parseNoteToJson(note.content),
                )
            }
        }
    }

    fun deleteNote(note: TextNote) {
        val filePath = getPath()?.resolve("${note.meta?.name}")
        removeFileFromMemory(filePath)
        Log.d("Deleted", note.meta?.name!!)
    }

    fun getAllNotes(): List<TextNote> {
        val notes = mutableListOf<TextNote>()
        val path = getPath()
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

    private fun createTextNoteFile(path: Path?, noteString: String?) {
        fun writeToFile(bufferedWriter: BufferedWriter) {
            with(bufferedWriter) {
                write(noteString)
                close()
            }
        }
        if (path != null) {
            val file = path.toFile()
            val noteFile = File(file, "${DUMMY_FILENAME}.${NOTE_EXT}")
            if (!noteFile.exists()) {
                try {
                    val fileWriter = FileWriter(noteFile)
                    val bufferedWriter = BufferedWriter(fileWriter)
                    writeToFile(bufferedWriter)

                    val id = computeId(Files.size(noteFile.toPath()), noteFile.toPath())

                    Log.d("Filename", noteFile.name)

                    with(noteFile){
                        val newFile = File(file, "$id.${NOTE_EXT}")

                        if(!newFile.exists())
                            if (renameTo(newFile))
                                Log.d("New filename", newFile.name)
                            else
                                removeFileFromMemory(noteFile.toPath())

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun removeFileFromMemory(path: Path?) {
        if (path != null)
            Files.deleteIfExists(path)
    }

    private fun getPath(): Path? {
        val prefs = MemoPreferences.getInstance(context)
        val pathString = prefs.getPath()
        var path: Path? = null
        try {
            val file = File(pathString!!)
            file.mkdir()
            path = file.toPath()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return path
    }

    companion object {
        private const val NOTE_EXT = "note"
        private const val DUMMY_FILENAME =  "Note"
    }
}