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
import space.taran.arkmemo.space.taran.arkmemo.utils.CODES_CREATING_NOTE
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime

class TextNotesRepository @Inject constructor() {

    @Inject
    @ApplicationContext
    lateinit var context: Context

    fun saveNote(note: TextNote?, rootResourceId: ResourceId? = null): Long {
        val versionStorage = VersionStorage(getPath()!!)
        if (note != null) {
            val path = getPath()
            if (path != null) {
                Files.list(path)
                val newResourceId =
                    createTextNoteFile(
                        path,
                        JsonParser.parseNoteContentToJson(note.content),
                    )!!
                if (newResourceId < 0) {//Error creating TextNote file:
                    return newResourceId
                }
                versionStorage.addVersion(newResourceId, rootResourceId)
                return newResourceId
            }
        }
        return CODES_CREATING_NOTE.NOTE_NOT_CREATED.errCode.toLong()
    }

    fun deleteNote(note: TextNote, version: Version) {
        val notePath = getPath()?.resolve("${note.meta?.name}")
        val versionPath =
            getPath()?.resolve(ARK_VERSIONS_DIR)?.resolve("${version.meta?.rootResourceId}")
        removeFileFromMemory(notePath)
        removeFileFromMemory(versionPath)
        //we should delete all notes listed in version that don't have been forked? by now it just deletes the last note.
    }

    fun getAllNotesWithHistory(): List<TextNote> {
        val versions = getAllVersions()
        val notes = mutableListOf<TextNote>()
        for (ver in versions) {
            val rootResourceId = ver.meta!!.rootResourceId
            val lastChildId = getLastChildrenId(rootResourceId)
            val note = getNote(lastChildId)
            notes.add(note)
        }
        return notes
    }

    fun getAllNotesFromVersion(rootResourceId: ResourceId): List<TextNote> {
        val versionStorage = VersionStorage(getPath()!!)
        val versions = versionStorage.versions(rootResourceId)
        val notes = mutableListOf<TextNote>()
        val rootNote = getNote(rootResourceId)
        notes.add(rootNote)
        for (noteResId in versions) {
            val note = getNote(noteResId)
            notes.add(note)
        }
        return notes
    }

    fun getAllVersions(): List<Version> {
        val versions = mutableListOf<Version>()//content
        val path = getPath()
        if (path != null) {
            val versionDir = File(path.toFile(), ARK_VERSIONS_DIR)
            if (versionDir.exists() && versionDir.isDirectory) {
                Files.list(versionDir.toPath()).forEach { filePath ->
                    try {
                        val fileName = filePath.fileName.toString()
                        val rootResourceId: ResourceId = fileName.toLong()
                        val meta = VersionMeta(
                            rootResourceId
                        )
                        val versionStorage = VersionStorage(getPath()!!)
                        val verContent = Version.Content(versionStorage.versions(rootResourceId))
                        val ver = Version(verContent, meta)
                        versions.add(ver)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return versions
    }

    private fun getLastChildrenId(rootResourceId: ResourceId): ResourceId {
        val path = getPath()
        var lastChildResourceId: ResourceId? = null
        if (path != null) {
            val versionDir = File(path.toFile(), ARK_VERSIONS_DIR)
            val versionFile = File(versionDir, rootResourceId.toString())
            if (versionFile.exists()) {
                val fileReader = FileReader(versionFile)
                val bufferedReader = BufferedReader(fileReader)
                val jsonVersion = StringBuilder()
                with(bufferedReader) {
                    forEachLine {
                        jsonVersion.append(it)
                    }
                }
                lastChildResourceId = if (jsonVersion.toString() == "") {
                    rootResourceId
                } else {
                    val versionContent =
                        JsonParser.parseVersionContentFromJson(jsonVersion.toString())
                    if (versionContent.idList.isEmpty()) {
                        rootResourceId
                    } else {
                        versionContent.idList.last()
                    }
                }
            }
        }
        return lastChildResourceId!!
    }

    private fun getNote(ResourceId: ResourceId): TextNote {
        val path = getPath()
        var note: TextNote? = null
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
                    id,
                    notePath.fileName.toString(),
                    notePath.extension,
                    notePath.getLastModifiedTime(),
                    size
                )
                note = TextNote(textnoteContent, meta)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return note!!
    }

    private fun createTextNoteFile(path: Path?, noteString: String?): Long? {
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
                if (!noteFile.exists()) {
                    if (tempFile.renameTo(noteFile))
                        Log.d("New filename", noteFile.name)
                    else
                        removeFileFromMemory(tempFile.toPath())
                } else {//file already exists, since we use content-addressing, it means content already existed in another note.
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
        const val NOTE_EXT = "note"
        private const val DUMMY_FILENAME = "Note"
        private const val ARK_VERSIONS_DIR = ".ark/versions"
    }
}