package dev.arkbuilders.arkmemo.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.contracts.PermissionContract
import dev.arkbuilders.arkmemo.databinding.ActivityMainBinding
import dev.arkbuilders.arkmemo.models.RootNotFound
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import dev.arkbuilders.arkmemo.ui.dialogs.CommonActionDialog
import dev.arkbuilders.arkmemo.ui.dialogs.FilePickerDialog
import dev.arkbuilders.arkmemo.ui.fragments.BaseFragment
import dev.arkbuilders.arkmemo.ui.fragments.EditTextNotesFragment
import dev.arkbuilders.arkmemo.ui.fragments.NotesFragment
import dev.arkbuilders.components.filepicker.onArkPathPicked
import javax.inject.Inject
import kotlin.io.path.exists

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val binding by viewBinding(ActivityMainBinding::bind)

    @Inject
    lateinit var memoPreferences: MemoPreferences

    @IdRes
    private val fragContainer = R.id.container

    var fragment: Fragment = NotesFragment()

    init {
        FilePickerDialog.readPermLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    FilePickerDialog.show()
                } else {
                    finish()
                }
            }

        FilePickerDialog.readPermLauncherSdkR =
            registerForActivityResult(PermissionContract()) { isGranted ->
                if (isGranted) {
                    FilePickerDialog.show()
                } else {
                    finish()
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setStatusBarColor(ContextCompat.getColor(this, R.color.white), true)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val storageFolderExisting = memoPreferences.getNotesStorage().exists()
        if (memoPreferences.storageNotAvailable()) {
            if (!storageFolderExisting) {
                showNoNoteStorageDialog(RootNotFound(rootPath = memoPreferences.getPath()))
            } else {
                FilePickerDialog.show(this, supportFragmentManager)
            }

            supportFragmentManager.onArkPathPicked(this) {
                showFragment(savedInstanceState, it.toString())
            }
        } else {
            showFragment(savedInstanceState, memoPreferences.getPath())
        }
    }

    private fun showFragment(
        savedInstanceState: Bundle?,
        storagePath: String,
    ) {
        val textDataFromIntent = intent?.getStringExtra(Intent.EXTRA_TEXT)
        if (textDataFromIntent != null) {
            fragment = EditTextNotesFragment.newInstance(textDataFromIntent)
            fragment.arguments =
                Bundle().apply {
                    putString(BUNDLE_KEY_STORAGE_PATH, storagePath)
                }
            supportFragmentManager.beginTransaction().apply {
                replace(fragContainer, fragment, EditTextNotesFragment.TAG)
                commit()
            }
        } else {
            fragment.arguments =
                Bundle().apply {
                    putString(BUNDLE_KEY_STORAGE_PATH, storagePath)
                }
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction().apply {
                    add(fragContainer, fragment, NotesFragment.TAG)
                    commit()
                }
            } else {
                supportFragmentManager.apply {
                    val tag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG)
                    findFragmentByTag(tag)?.let {
                        fragment = it
                        if (!fragment.isInLayout) {
                            resumeFragment(fragment)
                        }
                    }
                }
            }
        }
    }

    private fun showNoNoteStorageDialog(error: RootNotFound) {
        val loadFailDialog =
            CommonActionDialog(
                title = getString(R.string.error_load_notes_failed_title),
                message = getString(R.string.error_load_notes_failed_description, error.rootPath),
                positiveText = R.string.error_load_notes_failed_positive_action,
                negativeText = R.string.error_load_notes_failed_negative_action,
                isAlert = false,
                onPositiveClick = {
                    FilePickerDialog.show(this, supportFragmentManager)
                },
                onNegativeClicked = {
                    finish()
                },
                onCloseClicked = {
                    finish()
                },
            )
        loadFailDialog.show(supportFragmentManager, CommonActionDialog.TAG)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(CURRENT_FRAGMENT_TAG, fragment.tag)
        super.onSaveInstanceState(outState)
    }

    fun showProgressBar(show: Boolean) {
        binding.progressBar.isVisible = show
    }

    fun initEditUI() {
        title = getString(R.string.edit_note)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setStatusBarColor(
        color: Int,
        isLight: Boolean,
    ) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
    }

    override fun onBackPressed() {
        if (fragment is BaseFragment) {
            (fragment as BaseFragment).onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val CURRENT_FRAGMENT_TAG = "current fragment tag"
        const val BUNDLE_KEY_STORAGE_PATH = "bundle_key_storage_path"
    }
}

fun AppCompatActivity.resumeFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().apply {
        show(fragment)
        commit()
    }
}
