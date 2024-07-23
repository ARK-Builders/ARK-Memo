package dev.arkbuilders.arkmemo.preferences

import java.nio.file.Path

interface MemoPreferences {
    fun storePath(path: String)

    fun getPath(): String

    fun getNotesStorage(): Path

    fun storeCrashReportEnabled(bool: Boolean)

    fun getCrashReportEnabled(): Boolean
}