package space.taran.arkmemo.data.repositories

import space.taran.arkmemo.data.VersionMeta
import space.taran.arkmemo.files.parsers.JsonParser
import space.taran.arkmemo.models.Version
import space.taran.arkmemo.space.taran.arkmemo.utils.CODES_DELETING_NOTE
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories

typealias ResourceId = Long

class VersionStorage(val root: Path){

    private val versionsDir = root.arkFolder().arkVersions()

    private fun versionPath(id: ResourceId): Path =
        versionsDir.resolve(id.toString())

    init {
        versionsDir.createDirectories()
    }

    fun versions(id: ResourceId):List<ResourceId>{
        return getVersionContent(id)
    }

    fun addVersion(id: ResourceId,newVersion: ResourceId?){
        if( newVersion == null ){//new .ark/versions/newResourceId file:
            val versionFile = versionPath(id).toFile()
            try {
                if (!versionFile.exists()) {
                    versionFile.createNewFile()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{ //add relation to .ark/versions/rootResourceId file:
            val versionFile = versionPath(newVersion).toFile()
            val fileReader = FileReader(versionFile)
            val bufferedReader = BufferedReader(fileReader)
            val jsonVersion = StringBuilder()
            with(bufferedReader) {
                forEachLine {
                    jsonVersion.append(it)
                }
            }
            val meta = VersionMeta(
                newVersion
            )
            val verToWrite = if(jsonVersion.toString() == ""){//adding first relation, using rootResourceId as oldResourceId
                Version(Version.Content( listOf() ),meta )
            }else{
                val oldVerContent = JsonParser.parseVersionContentFromJson(jsonVersion.toString())
                Version( oldVerContent, meta )
            }
            val idListMutable = verToWrite.content.idList.toMutableList()
            idListMutable.add(id)
            writeToVersionFile(versionFile,Version.Content( idListMutable.toList() ))
        }
    }

    fun removeVersion(id: ResourceId, removedVersion:ResourceId):Int {
        val versionContentNow = getVersionContent(removedVersion)
        val notePath = root.resolve("$id.${TextNotesRepository.NOTE_EXT}")
        if( versionContentNow.isEmpty() ){//means that root TextNote its being deleted, and no more content
            //just delete note and version.
            removeFileFromMemory(notePath)
            removeFileFromMemory(versionPath(id))
            return CODES_DELETING_NOTE.SUCCESS_NOTE_AND_VERSION_DELETED.code
        }else if( removedVersion == id ){//means that root TextNote its being deleted,
            val secondNoteFromVId = versionContentNow[0]
            val versionFile = versionPath(removedVersion).toFile()
            val newVersionFile = versionPath(secondNoteFromVId).toFile()
            //rename version file:
            versionFile.renameTo(newVersionFile)
            val newVersionContent = Version.Content( versionContentNow.drop(1) )
            writeToVersionFile( newVersionFile, newVersionContent )
            //delete note:
            removeFileFromMemory(notePath)
            return CODES_DELETING_NOTE.SUCCESS_NOTE_DELETED_VERSION_CHANGED.code
        }else{
            val noteIndex = versionContentNow.indexOf(id)
            if(noteIndex == -1){
                return CODES_DELETING_NOTE.NOTE_NOT_DELETED.code
            }
            val idListMutable = versionContentNow.toMutableList()
            idListMutable.removeAt(noteIndex)
            //overwrite version file:
            writeToVersionFile(versionPath(removedVersion).toFile(),Version.Content( idListMutable.toList() ))
            //delete note:
            removeFileFromMemory(notePath)
        }
        return CODES_DELETING_NOTE.SUCCESS.code
    }

    private fun removeFileFromMemory(path: Path) {
        Files.deleteIfExists(path)
    }

    private fun getVersionContent(rootResourceId:ResourceId):List<ResourceId> {
        val versionPath = versionPath(rootResourceId)
        var verContent: Version.Content? = null
        try {
            val fileReader = FileReader(versionPath.toFile())
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
        return verContent!!.idList
    }

    private fun writeToVersionFile(file:File,versionCont:Version.Content){
        val fileWriter = FileWriter( file )
        val bufferedWriter = BufferedWriter(fileWriter)
        with(bufferedWriter) {
            write( JsonParser.parseVersionContentToJson(versionCont) )
            close()
        }
    }

}