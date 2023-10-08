package dev.arkbuilders.arkmemo.preferences

import android.content.Context
import android.content.Context.MODE_PRIVATE

class MemoPreferences private constructor(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(NAME, MODE_PRIVATE)
    private val prefEditor = sharedPreferences.edit()

    fun storePath(path: String){
        prefEditor.apply{
            putString(CURRENT_NOTES_PATH, path)
            apply()
        }
    }

    fun getPath() = sharedPreferences.getString(CURRENT_NOTES_PATH, null)

    companion object{
        private const val NAME = "memo_prefs"
        private const val CURRENT_NOTES_PATH = "current_notes_path"
        private var preferences: MemoPreferences? = null

        fun getInstance(context: Context): MemoPreferences{
            if(preferences == null)
                preferences = MemoPreferences(context)
            return preferences!!
        }
    }
}