package dev.arkbuilders.arkmemo.preferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.Path


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

    override fun getPath(): String = sharedPreferences.getString(CURRENT_NOTES_PATH, "") ?: ""

    override fun getNotesStorage(): Path = Path(getPath())
}