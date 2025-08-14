package dev.arkbuilders.arkmemo.utils

import android.content.Context
import androidx.core.content.edit

class Config(context: Context) {

    private val prefs = context.getSharedPreferences("memo_prefs", Context.MODE_PRIVATE)

    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var crashReport: Boolean
        get() = prefs.getBoolean(CRASH_REPORT_ENABLE, true)
        set(isEnable) = prefs.edit {
            putBoolean(CRASH_REPORT_ENABLE, isEnable)
        }
}