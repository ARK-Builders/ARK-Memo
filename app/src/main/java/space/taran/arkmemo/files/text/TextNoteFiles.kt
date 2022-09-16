package space.taran.arkmemo.files.text

import android.content.Context
import android.util.Log
import space.taran.arkmemo.files.parsers.JsonParser
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.preferences.MemoPreferences
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.extension

class TextNoteFiles {
    companion object{
        private const val NOTE_EXT = "note"

        private fun createTextNoteFile(path: Path?, noteString: String?, filename: String){
            fun writeToFile(bufferedWriter: BufferedWriter){
                with(bufferedWriter) {
                    write(noteString)
                    Log.d("Note$filename", noteString!!)
                    close()
                }
            }
            if(path != null) {
                val file = path.toFile()
                val noteFile = File(file, filename)
                if (!noteFile.exists()) {
                    try {
                        val fileWriter = FileWriter(noteFile)
                        val bufferedWriter = BufferedWriter(fileWriter)
                        writeToFile(bufferedWriter)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        private fun removeFileFromMemory(path: Path?){
            if(path != null)
                Files.deleteIfExists(path)
        }

        fun saveNote(context: Context, note: TextNote?){
            if(note != null){
                val path = getPath(context)
                if(path != null){
                    val filename = "${sha512(note.timeStamp)}.$NOTE_EXT"
                    Log.d("File name", filename)
                    createTextNoteFile(path,
                        JsonParser.parseNoteToJson(note),
                        filename
                    )
                }
            }
        }

        fun deleteNote(context: Context, note: TextNote){
            val filePath = getPath(context)?.resolve("${sha512(note.timeStamp)}.$NOTE_EXT")
            removeFileFromMemory(filePath)
            Log.d("File deleted", filePath.toString())
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

        private fun sha512(string: String): String{
            return MessageDigest.getInstance("SHA-512")
                .digest(string.toByteArray(Charsets.UTF_8))
                .fold(""){ str, it ->
                    str + "%02x".format(it)
                }
        }
    }
}