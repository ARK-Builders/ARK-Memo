package dev.arkbuilders.arkmemo.ui.activities

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkfilepicker.presentation.onArkPathPicked
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.contracts.PermissionContract
import dev.arkbuilders.arkmemo.databinding.ActivityMainBinding
import dev.arkbuilders.arkmemo.ui.dialogs.FilePickerDialog
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import dev.arkbuilders.arkmemo.ui.dialogs.NoteDeleteDialog
import dev.arkbuilders.arkmemo.ui.fragments.EditTextNotesFragment
import dev.arkbuilders.arkmemo.ui.fragments.NotesFragment
import dev.arkbuilders.arkmemo.ui.fragments.SettingsFragment
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding by viewBinding(ActivityMainBinding::bind)

    @Inject
    lateinit var memoPreferences: MemoPreferences

    @IdRes
    private val fragContainer = R.id.container

    private var menu: Menu? = null

    var fragment: Fragment = NotesFragment()

    private var shouldRecord = false
    private var mIsActionMode = false
    private val audioRecordingPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            shouldRecord = isGranted
        }

    init {
        FilePickerDialog.readPermLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) FilePickerDialog.show()
                else finish()
            }

        FilePickerDialog.readPermLauncher_SDK_R =
            registerForActivityResult(PermissionContract()) { isGranted ->
                if (isGranted) FilePickerDialog.show()
                else finish()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        audioRecordingPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        fun showFragment() {
            val textDataFromIntent = intent?.getStringExtra(Intent.EXTRA_TEXT)
            if (textDataFromIntent != null) {
                fragment = EditTextNotesFragment.newInstance(textDataFromIntent)
                supportFragmentManager.beginTransaction().apply {
                    replace(fragContainer, fragment, EditTextNotesFragment.TAG)
                    commit()
                }
            } else {
                if (savedInstanceState == null)
                    supportFragmentManager.beginTransaction().apply {
                        add(fragContainer, fragment, NotesFragment.TAG)
                        commit()
                    }
                else {
                    supportFragmentManager.apply {
                        val tag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG)!!
                        fragment = findFragmentByTag(tag)!!
                        if (!fragment.isInLayout)
                            resumeFragment(fragment)
                    }
                }
            }
        }

        if (memoPreferences.getPath().isEmpty()) {
            FilePickerDialog.show(this, supportFragmentManager)

            supportFragmentManager.onArkPathPicked(this) {
                memoPreferences.storePath(it.toString())
                showFragment()
            }
        }
        else showFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        Log.d("tuancoltech", "onCreateOptionsMenu")
        this.menu = menu
        if(fragment.tag != NotesFragment.TAG) {
            showSettingsButton(false)
            toggleBatchDeleteOption(false)
        }
        else {
            toggleBatchDeleteOption(show = true)
        }

        if (mIsActionMode) {
            menu.findItem(R.id.settings).setVisible(false)
            menu.findItem(R.id.cancel).setVisible(true)
            menu.findItem(R.id.delete).setIcon(R.drawable.ic_confirm_delete)
        } else {
            menu.findItem(R.id.settings).setVisible(true)
            menu.findItem(R.id.cancel).setVisible(false)
            menu.findItem(R.id.delete)
                .setIcon(ContextCompat.getDrawable(this@MainActivity, R.drawable.delete)
                    .apply {
                        this?.setTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this@MainActivity, R.color.white))
                        )
            })
        }

        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(CURRENT_FRAGMENT_TAG, fragment.tag)
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                fragment = SettingsFragment()
                replaceFragment(fragment, SettingsFragment.TAG)
            }

            R.id.delete -> {
                if (mIsActionMode) {
                    Log.d("tuancoltech", "Confirm delete item clicked!")
                    NoteDeleteDialog(
                        mCustomMessage = getString(R.string.ark_memo_delete_all_warn),
                        mPositiveAction = {
                        Log.d("tuancoltech", "on OK clicked")
                    }).show(supportFragmentManager, NoteDeleteDialog.TAG)

                } else {
                    Log.d("tuancoltech", "delete item clicked!")
                    showBatchDeleteMode()
                }
            }

            R.id.cancel -> {
//                toggleBatchDeleteOption()
                Log.d("tuancoltech", "Cancel action mode item clicked! mIsActionMode: " + mIsActionMode)
                showBatchDeleteMode()
            }
        }
        return true
    }

    private fun showBatchDeleteMode() {
        if (fragment.tag == NotesFragment.TAG) {
            (fragment as? NotesFragment)?.toggleActionMode()
            mIsActionMode = !mIsActionMode
            invalidateOptionsMenu()
        }
    }

    fun showSettingsButton(show: Boolean = true) {
        if(menu != null) {
            val settingsItem = menu?.findItem(R.id.settings)
            settingsItem?.isVisible = show
        }
    }

    fun toggleBatchDeleteOption(show: Boolean = false) {
        Log.d("tuancoltech", "showBatchDeleteOption show: " + show + ". menu: " + menu)
        if(menu != null) {
            val deleteItem = menu?.findItem(R.id.delete)
            deleteItem?.isVisible = show
        }
    }

    fun showProgressBar(show: Boolean) {
        binding.progressBar.isVisible = show
    }

    fun initEditUI() {
        title = getString(R.string.edit_note)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        showSettingsButton(false)
        toggleBatchDeleteOption(false)
    }

    companion object{
        private const val CURRENT_FRAGMENT_TAG = "current fragment tag"
    }
}

fun AppCompatActivity.replaceFragment(fragment: Fragment, tag: String) {
    supportFragmentManager.beginTransaction().apply {
        val backStackName = fragment.javaClass.name
        val popBackStack = supportFragmentManager.popBackStackImmediate(backStackName, 0)
        if (!popBackStack) {
            replace(R.id.container, fragment, tag)
            addToBackStack(backStackName)
        } else {
            show(fragment)
        }
        commit()
    }
}

fun AppCompatActivity.resumeFragment(fragment: Fragment){
    supportFragmentManager.beginTransaction().apply{
        show(fragment)
        commit()
    }
}

fun Context.getTextFromClipBoard(): String?{
    val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    return clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
}
