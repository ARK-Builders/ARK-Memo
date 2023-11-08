package space.taran.arkmemo.preferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.simplemobiletools.commons.extensions.getPaths
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
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

    override fun getPath() = sharedPreferences.getString(CURRENT_NOTES_PATH, null)

    override fun getNotesStorage() = Path(getPath()!!)
}