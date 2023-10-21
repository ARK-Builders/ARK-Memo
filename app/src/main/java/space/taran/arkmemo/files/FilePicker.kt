package space.taran.arkmemo.files

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import dev.arkbuilders.arkfilepicker.ArkFilePickerConfig
import dev.arkbuilders.arkfilepicker.presentation.filepicker.ArkFilePickerFragment
import dev.arkbuilders.arkfilepicker.presentation.filepicker.ArkFilePickerMode
import space.taran.arkmemo.BuildConfig
import space.taran.arkmemo.R

class FilePicker private constructor(){
    companion object{

        private const val TAG = "file_picker"
        private var fragmentManager: FragmentManager? = null
        var readPermLauncher: ActivityResultLauncher<String>? = null
        var readPermLauncher_SDK_R: ActivityResultLauncher<String>? = null

        fun show() {
            ArkFilePickerFragment.newInstance(getFilePickerConfig()).show(fragmentManager!!, TAG)
        }

        fun show(activity: AppCompatActivity, fragmentManager: FragmentManager){
            this.fragmentManager = fragmentManager
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

        fun permissionDeniedError(context: Context){
            Toast.makeText(context, context.getString(R.string.no_file_access), Toast.LENGTH_SHORT).show()
        }

        private fun getFilePickerConfig() = ArkFilePickerConfig(
            mode = ArkFilePickerMode.FOLDER,
            titleStringId = R.string.file_picker_title,
            pickButtonStringId = R.string.select
        )
    }
}
