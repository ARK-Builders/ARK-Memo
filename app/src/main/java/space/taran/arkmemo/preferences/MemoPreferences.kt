package space.taran.arkmemo.preferences

import java.nio.file.Path

interface MemoPreferences {
    fun storePath(path: String)

    fun getPath(): String?

    fun getNotesStorage(): Path
}