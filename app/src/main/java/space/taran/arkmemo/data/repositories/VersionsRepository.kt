package space.taran.arkmemo.data.repositories

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.models.Version
import space.taran.arkmemo.preferences.MemoPreferences
import java.io.File
import java.nio.file.Path
import javax.inject.Inject

class VersionsRepository @Inject constructor() {
    @Inject
    @ApplicationContext
    lateinit var context: Context

    fun deleteNoteFromVersion(note: TextNote, rootResourceId: ResourceId): Int {
        val versionStorage = VersionStorage(getPath()!!)
        return versionStorage.removeVersion(note.meta!!.resourceId, rootResourceId)
    }

    fun getVersionContent(id: ResourceId): Version.Content {
        val versionStorage = VersionStorage(getPath()!!)
        return Version.Content(versionStorage.versions(id))
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
}