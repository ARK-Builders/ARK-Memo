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
import space.taran.arkmemo.space.taran.arkmemo.utils.CODES_CREATING_NOTE
import space.taran.arkmemo.space.taran.arkmemo.utils.CODES_DELETING_NOTE

class TextNotesRepository @Inject constructor() {

    @Inject @ApplicationContext lateinit var context: Context

    fun saveNote(note: TextNote?,rootResourceId: String? = null):Long {
        if (note != null) {
            val path = getPath()
            if (path != null) {
                Files.list(path)
                val newResourceId =
                createTextNoteFile(
                    path,
                    JsonParser.parseNoteContentToJson(note.content),
                )!!
                if(newResourceId < 0){//Error creating TextNote file:
                    return newResourceId
                }
                if( rootResourceId == null ){//new .ark/versions/newResourceId file:
                    createVersionFile(path, newResourceId)
                }else{ //add relation to .ark/versions/rootResourceId file:
                    val versionDir = File(path.toFile(), ARK_VERSIONS_DIR)
                    val versionFile = File(versionDir,rootResourceId)
                    val fileReader = FileReader(versionFile)
                    val bufferedReader = BufferedReader(fileReader)
                    val jsonVersion = StringBuilder()
                    with(bufferedReader) {
                        forEachLine {
                            jsonVersion.append(it)
                        }
                    }
                    val meta = VersionMeta(
                        rootResourceId,
                        versionFile.toPath().fileName.toString()
                    )
                    val verToWrite = if(jsonVersion.toString() == ""){//adding first relation, using rootResourceId as oldResourceId
                        Version(Version.Content( listOf() ),meta )
                    }else{
                        val oldVerContent = JsonParser.parseVersionContentFromJson(jsonVersion.toString())
                        Version( oldVerContent, meta )
                    }
                    addNoteToVersion(newResourceId.toString(), verToWrite)
                }
                return newResourceId
            }
        }
        return CODES_CREATING_NOTE.NOTE_NOT_CREATED.errCode.toLong()
    }

    fun deleteNote(note: TextNote,version:Version) {
        val notePath = getPath()?.resolve("${note.meta?.name}")
        val versionPath = getPath()?.resolve(ARK_VERSIONS_DIR)?.resolve("${version.meta?.name}")
        removeFileFromMemory(notePath)
        removeFileFromMemory(versionPath)
        //we should delete all notes listed in version that don't have been forked? by now it just deletes the last note.
    }

    fun deleteNoteFromVersion(note: TextNote,version:Version):Int {
        if(version.meta == null || note.meta == null){
            return CODES_DELETING_NOTE.NOTE_NOT_DELETED.code
        }
        val versionPath = getPath()?.resolve(ARK_VERSIONS_DIR)?.resolve(version.meta.name)
        val notePath = getPath()?.resolve(note.meta.name)
        if( version.content.idList.isEmpty() ){//means that root TextNote its being deleted, and no more content
            //just delete note and version.
            removeFileFromMemory(notePath)
            removeFileFromMemory(versionPath)
            return CODES_DELETING_NOTE.SUCCESS_NOTE_AND_VERSION_DELETED.code
        }else if( version.meta.rootResourceId == note.meta.id ){//means that root TextNote its being deleted,
            //rename version file:
            val newVersionContent = Version.Content(version.content.idList.drop(1))
            val secondNoteFromVId = version.content.idList[0]
            val newVersionPath = getPath()?.resolve(ARK_VERSIONS_DIR)?.resolve(secondNoteFromVId)
            val versionFile = versionPath!!.toFile()
            val newVersionFile = newVersionPath!!.toFile()
            versionFile.renameTo(newVersionFile)
            writeToVersionFile( newVersionPath, newVersionContent )
            //delete note:
            removeFileFromMemory(notePath)
            return CODES_DELETING_NOTE.SUCCESS_NOTE_DELETED_VERSION_CHANGED.code
        }else{
            val noteIndex = version.content.idList.indexOf(note.meta.id)
            if(noteIndex == -1){
                return CODES_DELETING_NOTE.NOTE_NOT_DELETED.code
            }
            val idListMutable = version.content.idList.toMutableList()
            idListMutable.removeAt(noteIndex)
            //overwrite version file:
            writeToVersionFile(versionPath!!,Version.Content( idListMutable.toList() ))
            //delete note:
            removeFileFromMemory(notePath)
        }
        return CODES_DELETING_NOTE.SUCCESS.code
    }

    fun getAllNotesFromVersion(version:Version): List<TextNote> {
        val notes = mutableListOf<TextNote>()
        val rootNote = getNote( version.meta!!.rootResourceId )
        notes.add(rootNote)
        for(noteResId in version.content.idList){
            val note = getNote( noteResId )
            notes.add(note)
        }
        return notes
    }

    fun getSecondNoteFromVersion(version:Version): TextNote {
        val noteResId = version.content.idList[0]
        return getNote( noteResId )
    }

    fun getAllNotesWithHistory(): List<TextNote> {
        val versions = getAllVersions()
        val notes = mutableListOf<TextNote>()
        for(ver in versions){
            val rootResourceId = ver.meta!!.rootResourceId
            val lastChildId = getLastChildrenId(rootResourceId)
            val note = getNote(lastChildId)
            notes.add(note)
        }
        return notes
    }

    fun getAllVersions(): List<Version> {
        val versions = mutableListOf<Version>()//content
        val path = getPath()
        if (path != null) {
            val versionDir = File(path.toFile(), ARK_VERSIONS_DIR)
            if(versionDir.exists() && versionDir.isDirectory ){
                Files.list( versionDir.toPath() ).forEach { filePath ->
                    try {
                        val fileName = filePath.fileName.toString()
                        val rootResourceId:String = fileName
                        val meta = VersionMeta(
                            rootResourceId,
                            fileName
                        )
                        val verContent = getVersionContent(rootResourceId)
                        val ver = Version(verContent,meta)
                        versions.add(ver)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return versions
    }

    fun getVersionContent(rootResourceId:String):Version.Content {
        val path = getPath()
        var verContent:Version.Content? = null
        if (path != null) {
            val versionDir = File(path.toFile(), ARK_VERSIONS_DIR)
            val versionFile = File(versionDir, rootResourceId)
            try {
                val fileReader = FileReader(versionFile)
                val bufferedReader = BufferedReader(fileReader)
                val jsonVersion = StringBuilder()
                with(bufferedReader) {
                    forEachLine {
                        jsonVersion.append(it)
                    }
                }
                verContent = if(jsonVersion.toString() == ""){
                    Version.Content(listOf())
                }else{
                    JsonParser.parseVersionContentFromJson(jsonVersion.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return verContent!!
    }

    private fun getLastChildrenId(rootResourceId: String):String {
        val path = getPath()
        var lastChildResourceId:String? = null
        if (path != null) {
            val versionDir = File(path.toFile(), ARK_VERSIONS_DIR)
            val versionFile = File(versionDir, rootResourceId)
            if(versionFile.exists()){
                val fileReader = FileReader(versionFile)
                val bufferedReader = BufferedReader(fileReader)
                val jsonVersion = StringBuilder()
                with(bufferedReader) {
                    forEachLine {
                        jsonVersion.append(it)
                    }
                }
                lastChildResourceId = if(jsonVersion.toString() == ""){
                    rootResourceId
                }else{
                    val versionContent = JsonParser.parseVersionContentFromJson( jsonVersion.toString() )
                    if(versionContent.idList.isEmpty()){
                        rootResourceId
                    }else{
                        versionContent.idList.last()
                    }
                }
            }
        }
        return lastChildResourceId!!
    }

    private fun getNote(ResourceId:String): TextNote {
        val path = getPath()
        var note:TextNote? = null
        if (path != null) {
            val noteFile = File(path.toFile(), "$ResourceId.$NOTE_EXT")
            val notePath = noteFile.toPath()
            try {
                val fileReader = FileReader(noteFile)
                val bufferedReader = BufferedReader(fileReader)
                val jsonTextNote = StringBuilder()
                with(bufferedReader) {
                    forEachLine {
                        jsonTextNote.append(it)
                    }
                }
                val textnoteContent = JsonParser.parseNoteContentFromJson(jsonTextNote.toString())
                val size = Files.size(notePath)
                val id = computeId(size, notePath)
                val meta = ResourceMeta(
                    id.toString(),
                    notePath.fileName.toString(),
                    notePath.extension,
                    notePath.getLastModifiedTime(),
                    size
                )
                note = TextNote( textnoteContent,meta )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return note!!
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
            val tempFile = File(file, "${DUMMY_FILENAME}.${NOTE_EXT}")
            try {
                val fileWriter = FileWriter(tempFile)
                val bufferedWriter = BufferedWriter(fileWriter)
                writeToFile(bufferedWriter)
                val id = computeId(Files.size(tempFile.toPath()), tempFile.toPath())
                val noteFile = File(file, "$id.${NOTE_EXT}")
                if(!noteFile.exists()){
                    if (tempFile.renameTo(noteFile))
                        Log.d("New filename", noteFile.name)
                    else
                        removeFileFromMemory(tempFile.toPath())
                }else{//file already exists, since we use content-addressing, it means content already existed in another note.
                    //for the time being, we just ban having same content.
                    removeFileFromMemory(tempFile.toPath())
                    return CODES_CREATING_NOTE.NOTE_ALREADY_EXISTS.errCode.toLong()
                }
                return id
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun createVersionFile(path: Path? ,rootResourceId: Long):Path?{//returns version file Path
        if (path != null) {
            val file = path.toFile()
            val versionDir = File(file, ARK_VERSIONS_DIR)
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

    private fun addNoteToVersion(textNoteId: String,version: Version):Int{
        val idListMutable = version.content.idList.toMutableList()
        if(version.meta == null){
            return -1
        }
        val versionPath = getPath()?.resolve(ARK_VERSIONS_DIR)?.resolve(version.meta.name)
        idListMutable.add(textNoteId)
        writeToVersionFile(versionPath!!,Version.Content( idListMutable.toList() ))
        return 0
    }

    private fun writeToVersionFile(path:Path,versionCont:Version.Content){
        val fileWriter = FileWriter( path.toFile() )
        val bufferedWriter = BufferedWriter(fileWriter)
        with(bufferedWriter) {
            write( JsonParser.parseVersionContentToJson(versionCont) )
            close()
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
        private const val ARK_VERSIONS_DIR =  ".ark/versions"
    }
}