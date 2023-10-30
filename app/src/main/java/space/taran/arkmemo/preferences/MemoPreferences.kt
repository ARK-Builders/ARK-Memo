package space.taran.arkmemo.preferences

import java.nio.file.Path

interface MemoPreferences {
    fun storePath(path: String)

    fun getPathString(): String?

    fun getPath(): Path?
}