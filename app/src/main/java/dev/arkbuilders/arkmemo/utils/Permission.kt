package dev.arkbuilders.arkmemo.utils

import android.content.Context
import android.content.pm.PackageManager

object Permission {
    fun hasPermission(context: Context, permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}