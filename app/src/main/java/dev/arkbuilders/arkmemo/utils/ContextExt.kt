package dev.arkbuilders.arkmemo.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import dev.arkbuilders.logging.ALog

fun Context.openLink(url: String) {
    try {
        startActivity(
            Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)),
        )
    } catch (e: Exception) {
        ALog.e("openLink", " exception: " + e.message)
    }
}

fun Context.openAppSettings(activityLauncher: ActivityResultLauncher<Intent>? = null) {
    try {
        val settingIntent =
            Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", this@openAppSettings.packageName, null)
            }
        activityLauncher?.let {
            activityLauncher.launch(settingIntent)
        } ?: {
            this.startActivity(settingIntent)
        }
    } catch (e: Exception) {
        ALog.e("openAppSettings", " exception: " + e.message)
    }
}
