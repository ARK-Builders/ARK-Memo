package space.taran.arkmemo.preferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import java.io.File
import java.nio.file.Path

class MemoPreferences private constructor(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(NAME, MODE_PRIVATE)
    private val prefEditor = sharedPreferences.edit()

    fun storePath(path: String){
        prefEditor.apply{
            putString(CURRENT_NOTES_PATH, path)
            apply()
        }
    }

    fun getPathString() = sharedPreferences.getString(CURRENT_NOTES_PATH, null)

    fun getPath(): Path? {
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

    companion object{
        private const val NAME = "memo_prefs"
        private const val CURRENT_NOTES_PATH = "current_notes_path"
        private var preferences: MemoPreferences? = null

        fun getInstance(context: Context): MemoPreferences{
            if(preferences == null)
                preferences = MemoPreferences(context)
            return preferences!!
        }

        fun getInstance() = preferences!!
    }
}