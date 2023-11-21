package dev.arkbuilders.arkmemo.ui.activities

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
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
import dev.arkbuilders.arkmemo.ui.fragments.EditTextNotesFragment
import dev.arkbuilders.arkmemo.ui.fragments.SettingsFragment
import dev.arkbuilders.arkmemo.ui.fragments.TextNotesFragment
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding by viewBinding(ActivityMainBinding::bind)

    @Inject
    lateinit var memoPreferences: MemoPreferences

    @IdRes
    private val fragContainer = R.id.container

    private var menu: Menu? = null

    var fragment: Fragment = TextNotesFragment()

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
                        add(fragContainer, fragment, TextNotesFragment.TAG)
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

        if (memoPreferences.getPath() == null) {
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
        this.menu = menu
        if(fragment.tag != TextNotesFragment.TAG)
            showSettingsButton(false)
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
        }
        return true
    }

    fun showSettingsButton(show: Boolean = true){
        if(menu != null) {
            val settingsItem = menu?.findItem(R.id.settings)
            settingsItem?.isVisible = show
        }
    }

    fun showProgressBar(show: Boolean) {
        binding.progressBar.isVisible = show
        if (!show) {
            Toast.makeText(this, getString(R.string.ark_memo_note_saved),
                Toast.LENGTH_SHORT)
                .show()
            onBackPressedDispatcher.onBackPressed()
        }
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
