package dev.arkbuilders.arkmemo.preferences

import java.nio.file.Path

interface MemoPreferences {
    fun storePath(path: String)

    fun getPath(): String

    fun getNotesStorage(): Path

    fun storeCrashReportEnabled(enabled: Boolean)

    fun getCrashReportEnabled(): Boolean

    fun storageNotAvailable(): Boolean

    fun isLastLaunchSuccess(): Boolean

    fun setLastLaunchSuccess(success: Boolean)
}
