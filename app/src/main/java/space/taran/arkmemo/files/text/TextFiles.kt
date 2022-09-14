package space.taran.arkmemo.files.text

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import space.taran.arkmemo.files.parsers.JsonParser
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.preferences.MemoPreferences
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension

class TextFiles {
    companion object{
        private const val NOTE_EXT = "note"
        private const val FILENAME = "Note"

        private fun createFile(path: Path?, noteString: String?){
            var numberOfFiles = 0
            if(path != null) {
                Files.list(path).forEach {
                    if (it.fileName.extension == NOTE_EXT)
                        numberOfFiles += 1
                }
                val file = path.toFile()
                val noteFile = File(file, "$FILENAME$numberOfFiles.$NOTE_EXT")
                if(!noteFile.exists()) {
                    try {
                        val fileWriter = FileWriter(noteFile)
                        val bufferedWriter = BufferedWriter(fileWriter)
                        with(bufferedWriter){
                            write(noteString)
                            Log.d("Note $numberOfFiles", noteString!!)
                            close()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        fun saveNote(context: Context, note: TextNote?){
            if(note != null){
                val path = getPath(context)
                if(path != null){
                    createFile(path,
                        JsonParser.parseNoteToJson(note)
                    )
                }
            }
        }

        fun readAllNotes(context: Context): List<TextNote>{
            val notes = mutableListOf<TextNote>()
            val path = getPath(context)
            var number = 0
            if(path != null) {
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
                                notes.add(JsonParser.parseNoteFromJson(jsonTextNote.toString()))
                                Log.d("Note $number", jsonTextNote.toString())
                                close()
                            }
                        }
                        catch(e: Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
            return notes
        }

        private fun getPath(context: Context):Path?{
            val prefs = MemoPreferences.getInstance(context)
            val pathString = prefs.getPath()
            var path: Path? = null
            try {
                val file = File(pathString!!)
                file.mkdir()
                path = file.toPath()
            }
            catch(e: Exception) {
                e.printStackTrace()
            }
            return path
        }
    }
}