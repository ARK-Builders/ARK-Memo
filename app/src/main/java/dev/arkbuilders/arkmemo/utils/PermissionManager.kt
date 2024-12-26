package dev.arkbuilders.arkmemo.utils

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import dev.arkbuilders.arkmemo.BuildConfig
import dev.arkbuilders.arkmemo.contracts.PermissionContract

class PermissionManager(val activity: ComponentActivity) {
    private var permissionResultCallback: ((granted: Boolean) -> Unit)? = null
    private val permissionLauncher: ActivityResultLauncher<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.registerForActivityResult(PermissionContract()) { isGranted ->
                permissionResultCallback?.invoke(isGranted)
            }
        } else {
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                permissionResultCallback?.invoke(isGranted)
            }
        }

    fun askForWriteStorage(onResult: ((granted: Boolean) -> Unit)? = null) {
        if (Permission.hasStoragePermission(activity)) {
            onResult?.invoke(true)
            return
        }

        permissionResultCallback = onResult
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val packageUri = "package:" + BuildConfig.APPLICATION_ID
            permissionLauncher.launch(packageUri)
        } else {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}
