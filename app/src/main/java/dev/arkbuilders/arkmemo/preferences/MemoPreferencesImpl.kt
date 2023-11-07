package dev.arkbuilders.arkmemo.preferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.file.Path
import javax.inject.Inject


private const val NAME = "memo_prefs"
private const val CURRENT_NOTES_PATH = "current_notes_path"

class MemoPreferencesImpl @Inject constructor(@ApplicationContext context: Context) :
    MemoPreferences {
    private val sharedPreferences = context.getSharedPreferences(NAME, MODE_PRIVATE)
    private val prefEditor = sharedPreferences.edit()

    override fun storePath(path: String) {
        prefEditor.apply {
            putString(CURRENT_NOTES_PATH, path)
            apply()
        }
    }

    override fun getPathString() = sharedPreferences.getString(CURRENT_NOTES_PATH, null)

    override fun getPath(): Path? {
        val pathString = getPathString()
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