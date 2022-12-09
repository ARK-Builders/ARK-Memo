package space.taran.arkmemo.data.repositories

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import space.taran.arklib.computeId
import space.taran.arkmemo.data.ResourceMeta
import space.taran.arkmemo.data.VersionMeta
import space.taran.arkmemo.files.parsers.JsonParser
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.models.Version
import space.taran.arkmemo.preferences.MemoPreferences
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime

class TextNotesRepository @Inject constructor() {

    @Inject @ApplicationContext lateinit var context: Context

    fun saveNote(note: TextNote?,rootResourceId: Long? = null) {
        fun writeToVersionFile(bufferedWriter: BufferedWriter,contentString:String?) {
            with(bufferedWriter) {
                write( contentString )
                close()
            }
        }
        if (note != null) {
            val path = getPath()
            if (path != null) {
                Files.list(path)
                val newResourceId =
                createTextNoteFile(
                    path,
                    JsonParser.parseNoteContentToJson(note.content),
                )!!
                if( rootResourceId == null ){//new .ark/versions/newResourceId file:
                    val versionFile:Path? = createVersionFile(path, newResourceId)
                }else{ //add relation to .ark/versions/rootResourceId file:
                    val versionDir = File(path.toFile(), "$ARK_VERSIONS_DIR")
                    val versionFile = File(versionDir,"$rootResourceId")
                    //Log.d("saveNote", "versionFile.readText(): "+versionFile.readText() )
                    val oldResourceId = getLastChildrenId( rootResourceId.toString() )
                    val fileReader = FileReader(versionFile)
                    val bufferedReader = BufferedReader(fileReader)
                    val jsonVersion = StringBuilder()
                    with(bufferedReader) {
                        forEachLine {
                            jsonVersion.append(it)
                        }
                    }
                    var verContentToWrite: Version.Content? = null
                    if(jsonVersion.toString() == ""){//adding first relation, using rootResourceId as oldResourceId
                        val newContent = "$rootResourceId -> $newResourceId"
                        verContentToWrite = Version.Content(newContent)
                        //Log.d("saveNote", "newContent: $rootResourceId -> $newResourceId" )
                    }else{
                        val oldVerContent = JsonParser.parseVersionContentFromJson(jsonVersion.toString())
                        val oldContent = oldVerContent.data
                        val newContent = "$oldResourceId -> $newResourceId"
                        val contentToWrite = oldContent + "\n" + newContent
                        verContentToWrite = Version.Content(contentToWrite)
                        //Log.d("saveNote", "newContent: $oldResourceId -> $newResourceId" )
                    }
                    val fileWriter = FileWriter( versionFile )
                    val bufferedWriter = BufferedWriter(fileWriter)
                    writeToVersionFile(bufferedWriter, JsonParser.parseVersionContentToJson(verContentToWrite) )
                }
            }
        }
    }

    fun deleteNote(note: TextNote,version:Version) {
        val notePath = getPath()?.resolve("${note.meta?.name}")
        val versionPath = getPath()?.resolve("$ARK_VERSIONS_DIR")?.resolve("${version.meta?.name}")
        removeFileFromMemory(notePath)
        removeFileFromMemory(versionPath)
        //we should delete all notes listed in version that dont have been forked?
        Log.d("deleteNote", "Deleted note: "+note.meta?.name!!)
        Log.d("deleteNote", "Deleted version: "+version.meta?.name!!)
    }

    fun getAllNotesWithHistory(): List<TextNote> {
        val versions = getAllVersions()
        val notes = mutableListOf<TextNote>()
        for(ver in versions){
            val rootResourceId = ver.meta!!.rootResourceId
            val lastChildId = getLastChildrenId( rootResourceId.toString() )
            val note = getNote( lastChildId.toString() )
            notes.add(note)
        }
        return notes
    }

    fun getNote(ResourceId:String): TextNote {
        val path = getPath()
        var note:TextNote? = null
        if (path != null) {
            val noteFile = File(path.toFile(), ResourceId+"."+NOTE_EXT)
            val notePath = noteFile.toPath()
            try {
                val fileReader = FileReader(noteFile)
                val bufferedReader = BufferedReader(fileReader)
                val jsonTextNote = StringBuilder()
                with(bufferedReader) {
                    forEachLine {
                        jsonTextNote.append(it)
                    }
                    val textnoteContent = JsonParser.parseNoteContentFromJson(jsonTextNote.toString())
                    val size = Files.size(notePath)
                    val id = computeId(size, notePath)
                    val meta = ResourceMeta(
                        id,
                        notePath.fileName.toString(),
                        notePath.extension,
                        notePath.getLastModifiedTime(),
                        size
                    )
                    note = TextNote( textnoteContent,meta )
                    close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return note!!
    }

    fun getLastChildrenId(rootResourceId: String):Long {
        val path = getPath()
        var lastChildResourceId = ""
        if (path != null) {
            val versionDir = File(path.toFile(), "$ARK_VERSIONS_DIR")
            val versionFile = File(versionDir, "$rootResourceId")
            if(versionFile.exists()){
                val fileReader = FileReader(versionFile)
                val bufferedReader = BufferedReader(fileReader)
                val jsonVersion = StringBuilder()
                with(bufferedReader) {
                    forEachLine {
                        jsonVersion.append(it)
                    }
                }
                if(jsonVersion.toString() == ""){
                    lastChildResourceId = rootResourceId
                }else{
                    val versionContentObj = JsonParser.parseVersionContentFromJson( jsonVersion.toString() )
                    val indexLastNewline = versionContentObj.data .lastIndexOf("\n")
                    //Log.d("getLastChildrenId", "indexLastNewline: "+indexLastNewline.toString() )
                    //Log.d("getLastChildrenId", "versionContentObj.data: "+versionContentObj.data )
                    var last_line = ""
                    if(indexLastNewline == -1){
                        if(versionContentObj.data == ""){
                            lastChildResourceId = rootResourceId
                            return lastChildResourceId.toLong()
                        }else{
                            last_line = versionContentObj.data
                        }
                    }else{
                        last_line = versionContentObj.data.substring( indexLastNewline )
                    }
                    //Log.d("last_line", last_line)
                    val lastLineArr = last_line.split(" ")
                    if(lastLineArr.size == 3){
                        lastChildResourceId = lastLineArr[2]
                        //Log.d("lastChildResourceId", lastChildResourceId)
                    }
                }
            }
        }
        return lastChildResourceId.toLong()
    }

    /*
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
    */

    fun getAllVersions(): List<Version> {
        val versions = mutableListOf<Version>()//content
        val path = getPath()
        if (path != null) {
            val versionDir = File(path.toFile(), "$ARK_VERSIONS_DIR")
            if(versionDir.exists() && versionDir.isDirectory ){
                Files.list( versionDir.toPath() ).forEach { filePath ->
                    try {
                        val versionFile = filePath.toFile()
                        val fileReader = FileReader(versionFile)
                        val bufferedReader = BufferedReader(fileReader)
                        val versionStr = StringBuilder()
                        with(bufferedReader) {
                            forEachLine {
                                versionStr.append(it)
                            }
                            val filePathSplit = filePath.fileName.toString().split(".")
                            var rootResourceId:Long? = null
                            //Log.d("filePathSplit", filePathSplit.toString())
                            if(filePathSplit.size == 2){//has more than one fork:
                                rootResourceId = filePathSplit[0].toLong()
                            }else if(filePathSplit.size == 1){//has only one fork or its the original fork:
                                rootResourceId = filePathSplit[0].toLong()
                            }
                            val meta = VersionMeta(
                                rootResourceId!!,
                                filePath.fileName.toString()
                            )
                            if(versionStr.toString() == ""){
                                val ver = Version(Version.Content(""),meta)
                                versions.add(ver)
                            }else{
                                val content = JsonParser.parseVersionContentFromJson(versionStr.toString())
                                val ver = Version(content,meta)
                                versions.add(ver)
                            }
                            close()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return versions
    }

    private fun createTextNoteFile(path: Path?, noteString: String?):Long? {
        fun writeToFile(bufferedWriter: BufferedWriter) {
            with(bufferedWriter) {
                write(noteString)
                close()
            }
        }
        if (path != null) {
            val file = path.toFile()
            val noteFile = File(file, "${DUMMY_FILENAME}.${NOTE_EXT}")
            try {
                val fileWriter = FileWriter(noteFile)
                val bufferedWriter = BufferedWriter(fileWriter)
                writeToFile(bufferedWriter)
                val id = computeId(Files.size(noteFile.toPath()), noteFile.toPath())
                with(noteFile){
                    val newFile = File(file, "$id.${NOTE_EXT}")
                    if(!newFile.exists())
                        if (renameTo(newFile))
                            Log.d("New filename", newFile.name)
                        else
                            removeFileFromMemory(noteFile.toPath())
                }
                return id;
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null;
    }

    private fun createVersionFile(path: Path? ,rootResourceId: Long):Path?{//returns version file Path
        if (path != null) {
            val file = path.toFile()
            val versionDir = File(file, "$ARK_VERSIONS_DIR")
            if(!versionDir.exists()){
                versionDir.mkdirs()
            }
            val versionFile = File(versionDir, "$rootResourceId")
            var fileCreated = false
            try {
                if (!versionFile.exists()) {
                    fileCreated = versionFile.createNewFile()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if( versionFile.exists() || fileCreated ){
                return versionFile.toPath()
            }
        }
        return null
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
        private const val ARK_VERSIONS_DIR =  ".ark/versions"
    }
}