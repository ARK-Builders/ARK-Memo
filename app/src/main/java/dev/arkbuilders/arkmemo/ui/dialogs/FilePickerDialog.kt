package dev.arkbuilders.arkmemo.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import dev.arkbuilders.arkfilepicker.ArkFilePickerConfig
import dev.arkbuilders.arkfilepicker.presentation.filepicker.ArkFilePickerFragment
import dev.arkbuilders.arkfilepicker.presentation.filepicker.ArkFilePickerMode
import dev.arkbuilders.arkmemo.BuildConfig
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.ui.activities.MainActivity

class FilePickerDialog: ArkFilePickerFragment() {

    override fun dismiss() {
        super.dismiss()
        val activity = (requireActivity() as MainActivity)
        if (activity.memoPreferences.getPathString() == null) activity.finish()
    }

    companion object{

        private const val TAG = "file_picker"
        private var fragmentManager: FragmentManager? = null
        var readPermLauncher: ActivityResultLauncher<String>? = null
        var readPermLauncher_SDK_R: ActivityResultLauncher<String>? = null

        fun newInstance(config: ArkFilePickerConfig) = FilePickerDialog().apply {
            setConfig(config)
        }

        fun show() {
            newInstance(getFilePickerConfig()).show(fragmentManager!!, TAG)
        }

        fun show(activity: AppCompatActivity, fragmentManager: FragmentManager){
            Companion.fragmentManager = fragmentManager
            if(isReadPermissionGranted(activity)){
                show()
            }
            else askForReadPermissions()
        }

        private fun isReadPermissionGranted(activity: AppCompatActivity): Boolean{
            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                Environment.isExternalStorageManager()
            else{
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED
            }
        }

        private fun askForReadPermissions() {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                val packageUri ="package:" + BuildConfig.APPLICATION_ID
                readPermLauncher_SDK_R?.launch(packageUri)
            }
            else{
                readPermLauncher?.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        private fun getFilePickerConfig() = ArkFilePickerConfig(
            mode = ArkFilePickerMode.FOLDER,
            titleStringId = R.string.file_picker_title,
            pickButtonStringId = R.string.select
        )
    }
}
