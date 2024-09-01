package dev.arkbuilders.arkmemo.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkfilepicker.ArkFilePickerConfig
import dev.arkbuilders.arkfilepicker.presentation.filepicker.ArkFilePickerFragment
import dev.arkbuilders.arkfilepicker.presentation.filepicker.ArkFilePickerMode
import dev.arkbuilders.arkmemo.BuildConfig
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import javax.inject.Inject

@AndroidEntryPoint
class FilePickerDialog : ArkFilePickerFragment() {
    @Inject lateinit var memoPreferences: MemoPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (storageNotAvailable()) {
            isCancelable = false
        }
    }

    override fun dismiss() {
        super.dismiss()
        if (storageNotAvailable()) {
            activity?.finish()
        }
    }

    private fun storageNotAvailable(): Boolean = memoPreferences.getPath().isEmpty()

    companion object {
        private const val TAG = "file_picker"
        private lateinit var fragmentManager: FragmentManager
        var readPermLauncher: ActivityResultLauncher<String>? = null
        var readPermLauncherSdkR: ActivityResultLauncher<String>? = null

        fun show() {
            newInstance(getFilePickerConfig()).show(fragmentManager, TAG)
        }

        fun show(
            activity: AppCompatActivity,
            fragmentManager: FragmentManager,
        ) {
            Companion.fragmentManager = fragmentManager
            if (isReadPermissionGranted(activity)) {
                show()
            } else {
                askForReadPermissions()
            }
        }

        private fun newInstance(config: ArkFilePickerConfig) =
            FilePickerDialog().apply {
                setConfig(config)
            }

        private fun isReadPermissionGranted(activity: AppCompatActivity): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
            }
        }

        private fun askForReadPermissions() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val packageUri = "package:" + BuildConfig.APPLICATION_ID
                readPermLauncherSdkR?.launch(packageUri)
            } else {
                readPermLauncher?.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        private fun getFilePickerConfig() =
            ArkFilePickerConfig(
                mode = ArkFilePickerMode.FOLDER,
                titleStringId = R.string.file_picker_title,
                pickButtonStringId = R.string.select,
            )
    }
}
