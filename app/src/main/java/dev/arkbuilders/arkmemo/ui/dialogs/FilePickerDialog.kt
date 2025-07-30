package dev.arkbuilders.arkmemo.ui.dialogs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import dev.arkbuilders.arkmemo.utils.Permission
import dev.arkbuilders.arkmemo.utils.PermissionManager
import dev.arkbuilders.components.filepicker.ArkFilePickerConfig
import dev.arkbuilders.components.filepicker.ArkFilePickerFragment
import dev.arkbuilders.components.filepicker.ArkFilePickerMode
import javax.inject.Inject

@AndroidEntryPoint
class FilePickerDialog : ArkFilePickerFragment() {
    @Inject lateinit var memoPreferences: MemoPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (memoPreferences.storageNotAvailable()) {
            isCancelable = false
        }
    }

    override fun dismiss() {
        super.dismiss()
        if (memoPreferences.storageNotAvailable()) {
            activity?.finish()
        }
    }

    companion object {
        private const val TAG = "file_picker"
        private lateinit var fragmentManager: FragmentManager
        var permissionManager: PermissionManager? = null

        fun show() {
            newInstance(getFilePickerConfig()).show(fragmentManager, TAG)
        }

        fun show(
            activity: AppCompatActivity,
            fragmentManager: FragmentManager,
        ) {
            Companion.fragmentManager = fragmentManager
            if (Permission.hasStoragePermission(activity)) {
                show()
            } else {
                permissionManager?.askForWriteStorage { granted ->
                    if (granted) {
                        show()
                    } else {
                        activity.finish()
                    }
                }
            }
        }

        private fun newInstance(config: ArkFilePickerConfig) =
            FilePickerDialog().apply {
                setConfig(config)
            }

        private fun getFilePickerConfig() =
            ArkFilePickerConfig(
                mode = ArkFilePickerMode.FOLDER,
                titleStringId = R.string.file_picker_title,
                pickButtonStringId = R.string.select,
                themeId = android.R.style.ThemeOverlay_Material_Dialog_Alert,
            )
    }
}
