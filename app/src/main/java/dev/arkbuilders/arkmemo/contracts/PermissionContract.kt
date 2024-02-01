package dev.arkbuilders.arkmemo.contracts

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import com.simplemobiletools.commons.helpers.PERMISSION_RECORD_AUDIO

class PermissionContract: ActivityResultContract<String, Boolean>() {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun createIntent(context: Context, input: String)
    = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse(input))

    @RequiresApi(Build.VERSION_CODES.R)
    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return Environment.isExternalStorageManager()
    }
}
