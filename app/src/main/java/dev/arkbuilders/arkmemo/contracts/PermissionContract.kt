package dev.arkbuilders.arkmemo.contracts

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi

class PermissionContract : ActivityResultContract<String, Boolean>() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun createIntent(
        context: Context,
        input: String,
    ) = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse(input)).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): Boolean {
        return Environment.isExternalStorageManager()
    }
}
